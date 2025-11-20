package com.event_service.event_service.services;

import com.event_service.event_service.dto.*;
import com.event_service.event_service.exceptions.NotFoundException;
import com.event_service.event_service.repositories.EventImagesRepository;
import com.event_service.event_service.strategies.manage.*;
import com.event_service.event_service.utils.TimeZoneUtils;
import com.event_service.event_service.client.UserServiceClient;
import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.dto.EventResponse;
import com.event_service.event_service.dto.ExploreEventResponse;
import com.event_service.event_service.dto.PagedExploreEventResponse;
import com.event_service.event_service.repositories.TicketRepository;
import com.event_service.event_service.repositories.TicketTypeRepository;
import com.event_service.event_service.validations.FileValidator;
import com.example.common_libraries.dto.AppUser;
import com.example.common_libraries.dto.PlatformNotificationSettingDto;
import com.example.common_libraries.dto.UserInfoResponse;
import com.example.common_libraries.dto.queue_events.EventCreationNotificationMessage;
import com.example.common_libraries.exception.ValidationException;
import com.event_service.event_service.mappers.EventMapper;
import com.event_service.event_service.models.*;
import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import com.event_service.event_service.models.enums.EventTypeEnum;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.strategies.*;
import com.event_service.event_service.validations.EventValidator;
import com.example.common_libraries.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import com.event_service.event_service.specifications.EventSpecification;
import software.amazon.awssdk.services.sqs.SqsClient;


import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventTypeService eventTypeService;
    private final EventMeetingTypeService eventMeetingTypeService;
    private final EventStrategyContext eventStrategyContext = new EventStrategyContext();
    private final InPersonAndDayEventStrategy inPersonAndDayEventStrategy;
    private final InPersonAndMultiDayEventStrategy inPersonAndMultiDayEventStrategy;
    private final VirtualAndDayEventStrategy virtualAndDayEventStrategy;
    private final VirtualAndMultiDayEventStrategy virtualAndMultiDayEventStrategy;
    private final EventValidator eventValidator;
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final S3Service s3Service;
    private final EventImagesRepository eventImagesRepository;
    private final EventUpdateStrategyContext eventUpdateStrategyContext;
    private final InPersonAndDayEventUpdateStrategy inPersonAndDayEventUpdateStrategy;
    private final VirtualAndDayEventUpdateStrategy virtualAndDayEventUpdateStrategy;
    private final VirtualAndMultiDayEventUpdateStrategy virtualAndMultiDayEventUpdateStrategy;
    private final InPersonAndMultiDayUpdateEventStrategy inPersonAndMultiDayEventUpdateStrategy;
    private final FileValidator fileValidator;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserServiceClient userServiceClient;
    private final HttpServletRequest request;

    @Value("${sqs.event-stat-queue-url}")
    private String eventStatQueueUrl;

    @Value("${sqs.event-creation-queue-url}")
    private String eventCreationQueueUrl;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ORGANISER')")
    public EventResponse createEvent(EventRequest eventRequest, MultipartFile image, List<MultipartFile> eventImages) {
        eventValidator.validateRequiredGroup(eventRequest);
        fileValidator.validate(image);

        if(!CollectionUtils.isEmpty(eventImages)){
            fileValidator.validate(eventImages);
        }

        if (!CollectionUtils.isEmpty(eventImages) && eventImages.size() > 5) {
            throw new ValidationException(List.of("You can upload a maximum of 5 images per event."));
        }
        AppUser authenticatedUser = getCurrentUser();
        log.info(authenticatedUser.id().toString());
        EventType eventType = eventTypeService.findById(eventRequest.event_type_id());
        EventMeetingType eventMeetingType = eventMeetingTypeService
                .findEventMeetingTypeById(eventRequest.event_meeting_type_id());

        Event event = null;

        if(eventType.getName().name().equals(EventTypeEnum.DAY_EVENT.name())
                && eventMeetingType.getName().name().equals(EventMeetingTypeEnum.IN_PERSON.name())) {
            eventValidator.validateInPersonSingleDayGroup(eventRequest);
            eventStrategyContext.setEventStrategy(inPersonAndDayEventStrategy);
            event = eventStrategyContext.executeStrategy(eventRequest, image, eventImages,eventType, eventMeetingType);
        }

        if(eventType.getName().name().equals(EventTypeEnum.MULTI_DAY_EVENT.name())
                && eventMeetingType.getName().name().equals(EventMeetingTypeEnum.IN_PERSON.name())) {
            eventValidator.validateInPersonMultiDayGroup(eventRequest);
            eventStrategyContext.setEventStrategy(inPersonAndMultiDayEventStrategy);
            event = eventStrategyContext.executeStrategy(eventRequest, image, eventImages,eventType, eventMeetingType);
        }

        if(eventType.getName().name().equals(EventTypeEnum.DAY_EVENT.name())
                && eventMeetingType.getName().name().equals(EventMeetingTypeEnum.VIRTUAL.name())) {
            eventValidator.validateVirtualSingleDayGroup(eventRequest);
            eventStrategyContext.setEventStrategy(virtualAndDayEventStrategy);
            event = eventStrategyContext.executeStrategy(eventRequest, image, eventImages,eventType, eventMeetingType);
        }

        if(eventType.getName().name().equals(EventTypeEnum.MULTI_DAY_EVENT.name())
                && eventMeetingType.getName().name().equals(EventMeetingTypeEnum.VIRTUAL.name())) {
            eventValidator.validateVirtualMultiDayGroup(eventRequest);
            eventStrategyContext.setEventStrategy(virtualAndMultiDayEventStrategy);
            event = eventStrategyContext.executeStrategy(eventRequest, image, eventImages,eventType, eventMeetingType);
        }

        publishEventToEventStatQueue(authenticatedUser.id());
        publishEventCreationNotificationToQueue(event);

        return eventMapper.toResponse(event);
    }


    @Override
    @Cacheable(
            value = "events",
            key = "{#pageNumber, #pageSize, #hasTitle, #sortBy, #location, #date, #priceFilter, #past}"
    )
    public PagedExploreEventResponse listEvents(
            int pageNumber,
            int pageSize,
            String hasTitle,
            String[] sortBy,
            String location,
            LocalDate date,
            String priceFilter,
            Boolean past
    ) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, sortBy)
        );

        Specification<Event> spec = Specification.unrestricted();
            spec = spec.and(EventSpecification.hasTitle(hasTitle));
            spec = spec.and(EventSpecification.byLocation(location));
            spec = spec.and(EventSpecification.isOn(date));
            spec = spec.and(EventSpecification.byPriceFilter(priceFilter));

        if (past != null) {
            spec = past
                    ? spec.and(EventSpecification.isPast())
                    : spec.and(EventSpecification.isUpcoming());
        }
        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
        List<ExploreEventResponse> eventsToExplore = eventPage
                .stream()
                .map(eventMapper::toExploreEventResponse)
                .toList();

        return new PagedExploreEventResponse(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                eventPage.getTotalPages(),
                eventsToExplore
        );
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ORGANISER') and @eventOwnerShipService.checkOwnerShip(#id, authentication.principal.id)")
    public EventUpdateResponse updateEvent(Long id,
                                           EventRequest eventRequest,
                                           MultipartFile image,
                                           List<MultipartFile> newEventImages,
                                           List<Long> imagesToRemove) {
        eventValidator.validateRequiredGroup(eventRequest);

        Event event = getEventById(id);

        EventType eventType = eventTypeService.findById(eventRequest.event_type_id());
        EventMeetingType eventMeetingType =
                eventMeetingTypeService.findEventMeetingTypeById(eventRequest.event_meeting_type_id());


        if(eventType.getName().name().equals(EventTypeEnum.DAY_EVENT.name())
                && eventMeetingType.getName().name().equals(EventMeetingTypeEnum.IN_PERSON.name())) {
            eventValidator.validateInPersonSingleDayGroup(eventRequest);
            eventUpdateStrategyContext.setUpdateEventStrategy(inPersonAndDayEventUpdateStrategy);
           event = eventUpdateStrategyContext.executeStrategy(id, eventRequest, image, newEventImages, imagesToRemove,event, eventType, eventMeetingType);
        }

        if(eventType.getName().name().equals(EventTypeEnum.MULTI_DAY_EVENT.name())
                && eventMeetingType.getName().name().equals(EventMeetingTypeEnum.IN_PERSON.name())) {
            eventValidator.validateInPersonMultiDayGroup(eventRequest);
            eventUpdateStrategyContext.setUpdateEventStrategy(inPersonAndMultiDayEventUpdateStrategy);
            event = eventUpdateStrategyContext.executeStrategy(id, eventRequest, image, newEventImages, imagesToRemove,event, eventType, eventMeetingType);
        }

        if(eventType.getName().name().equals(EventTypeEnum.DAY_EVENT.name())
                && eventMeetingType.getName().name().equals(EventMeetingTypeEnum.VIRTUAL.name())) {
            eventValidator.validateVirtualSingleDayGroup(eventRequest);
            eventUpdateStrategyContext.setUpdateEventStrategy(virtualAndDayEventUpdateStrategy);
            event = eventUpdateStrategyContext.executeStrategy(id, eventRequest, image, newEventImages, imagesToRemove,event, eventType, eventMeetingType);
        }

        if(eventType.getName().name().equals(EventTypeEnum.MULTI_DAY_EVENT.name())
                && eventMeetingType.getName().name().equals(EventMeetingTypeEnum.VIRTUAL.name())) {
            eventValidator.validateVirtualMultiDayGroup(eventRequest);
            eventUpdateStrategyContext.setUpdateEventStrategy(virtualAndMultiDayEventUpdateStrategy);
            event = eventUpdateStrategyContext.executeStrategy(id, eventRequest, image, newEventImages, imagesToRemove,event, eventType, eventMeetingType);
        }

        Event updated = eventRepository.save(event);
        return eventMapper.toEventUpdateResponse(updated);
    }

    private void publishEventCreationNotificationToQueue(Event event){
        String accessToken = getCookieValue("accessToken");
        PlatformNotificationSettingDto platformNotification = userServiceClient.getNotificationSetting(accessToken);

        if(platformNotification.eventCreation()){
            List<UserInfoResponse> adminUsers = userServiceClient.getActiveAdmins(accessToken);
            for(UserInfoResponse admin : adminUsers){
                try{
                    String messageBody = objectMapper.writeValueAsString(
                            new EventCreationNotificationMessage(
                                    admin.email(),
                                    event.getCreatedBy(),
                                    event.getTitle()
                            )
                    );
                    sqsClient.sendMessage(builder -> builder
                            .queueUrl(eventCreationQueueUrl)
                            .messageBody(messageBody)
                    );
                    log.info("Event creation notification sent to SQS queue for admin id: {}", admin.id());
                }catch (Exception e){
                    log.error("Error sending event creation notification to SQS queue for admin id: {}: {}", admin.id(), e.getMessage());
                }
            }
        }
    }

    public void publishEventToEventStatQueue(Long organizerId) {
        // Implementation for publishing event to eventStat queue
        try{
            String messageBody = objectMapper.writeValueAsString(organizerId);
            sqsClient.sendMessage(builder -> builder.queueUrl(eventStatQueueUrl).messageBody(messageBody));
            log.info("Message sent to event stat SQS queue");
        }catch (Exception e){
            log.error("Error sending message to event stat SQS queue: {}", e.getMessage());
        }
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AppUser) authentication.getPrincipal();
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(()-> new NotFoundException("Event not found"));
    }

    private String getCookieValue(String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}