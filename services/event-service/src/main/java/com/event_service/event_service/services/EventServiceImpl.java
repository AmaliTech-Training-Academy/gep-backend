package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.dto.EventResponse;
import com.example.common_libraries.dto.queue_events.UserRegisteredEvent;
import com.example.common_libraries.exception.ValidationException;
import com.event_service.event_service.mappers.EventMapper;
import com.event_service.event_service.models.*;
import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import com.event_service.event_service.models.enums.EventTypeEnum;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.strategies.*;
import com.event_service.event_service.validations.EventValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.sqs.SqsClient;

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

    @Value("${sqs.event-stat-queue-url}")
    private String eventStatQueueUrl;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ORGANISER')")
    public EventResponse createEvent(EventRequest eventRequest, MultipartFile image, List<MultipartFile> eventImages) {
        eventValidator.validateRequiredGroup(eventRequest);

        if (!CollectionUtils.isEmpty(eventImages) && eventImages.size() > 5) {
            throw new ValidationException(List.of("You can upload a maximum of 5 images per event."));
        }
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

        // publish to eventStat queue to increment user's event count'
        Long organizerId = 0L;
        publishEventToEventStatQueue(organizerId);

        return eventMapper.toResponse(event);
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
}