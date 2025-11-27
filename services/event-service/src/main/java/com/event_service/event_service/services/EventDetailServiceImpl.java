package com.event_service.event_service.services;

import com.event_service.event_service.dto.*;
import com.event_service.event_service.models.EventImages;
import com.event_service.event_service.models.EventOrganizer;
import com.event_service.event_service.utils.SecurityUtils;
import com.example.common_libraries.dto.AppUser;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.event_service.event_service.mappers.EventDetailMapper;
import com.event_service.event_service.mappers.TicketTypeMapper;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.TicketType;
import com.event_service.event_service.repositories.EventImagesRepository;
import com.event_service.event_service.repositories.EventOptionsRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.repositories.TicketTypeRepository;
import com.example.common_libraries.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventDetailServiceImpl implements EventDetailService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventImagesRepository eventImagesRepository;
    private final EventOptionsRepository eventOptionsRepository;
    private final EventDetailMapper eventDetailMapper;
    private final SecurityUtils securityUtils;

    @Override
    public EventDetailResponse getEventDetailById(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        List<TicketType> ticketTypes = ticketTypeRepository.findAllByEvent(event);
        List<String> eventImagesUrl = event.getEventImages().stream().map(EventImages::getImage).toList();
        log.info("Event Images: {}",event.getEventImages());
        log.info("Event Images Url: {}",eventImagesUrl);
        List<TicketTypeResponse> ticketTypeResponses = ticketTypes.stream().map(TicketTypeMapper::toTicketTypeResponse).toList();
        Long capacity = eventOptionsRepository.findCapacityByEvent(event);

        return eventDetailMapper.toEventDetailResponse(event,eventImagesUrl,ticketTypeResponses,capacity);
    }

    @Override
    public EventEditPageResponse getEventEditPageById(Long eventId) {
        AppUser currentUser = securityUtils.getCurrentUser();

        Event event;
        if(currentUser.role().equals("ORGANISER")){
            event = eventRepository.findByIdAndUserId(eventId, currentUser.id())
                    .orElseThrow(()-> new ResourceNotFoundException("Event not found"));
        }else if(currentUser.role().equals("CO_ORGANIZER")){
            event = eventRepository.findByEventIdAndCoOrganizerUserId(eventId,currentUser.id())
                    .orElseThrow(()-> new ResourceNotFoundException("Event not found"));
        }else{
            throw new UnauthorizedException("You are not authorized to edit this event");
        }

        return EventEditPageResponse
                .builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .flyerUrl(event.getFlyerUrl())
                .zoomMeetingUrl(event.getZoomMeetingLink())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .eventTime(event.getEventTime())
                .eventMeetingType(Optional.ofNullable(event.getEventMeetingType())
                        .map(type -> EventMeetingTypeResponse
                                .builder()
                                .id(type.getId())
                                .name(type.getName())
                                .build()
                        ).orElse(null))
                .eventType(Optional.ofNullable(event.getEventType())
                        .map(eventType -> EventTypeResponse
                                .builder()
                                .id(eventType.getId())
                                .name(eventType.getName())
                                .build()).orElse(null))
                .ticketTypes( Optional.ofNullable(event.getTicketTypes()).orElse(List.of())
                        .stream()
                        .map(type -> TicketTypeResponse
                                .builder()
                                .id(type.getId())
                                .type(type.getType())
                                .price(type.getPrice())
                                .quantity(type.getQuantity())
                                .description(type.getDescription())
                                .build()
                        ).toList())
                .build();

    }
}
