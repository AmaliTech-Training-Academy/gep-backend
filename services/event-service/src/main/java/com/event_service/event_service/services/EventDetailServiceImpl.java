package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventDetailResponse;
import com.event_service.event_service.dto.TicketTypeResponse;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.event_service.event_service.mappers.EventDetailMapper;
import com.event_service.event_service.mappers.TicketTypeMapper;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.TicketType;
import com.event_service.event_service.repositories.EventImagesRepository;
import com.event_service.event_service.repositories.EventOptionsRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.repositories.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventDetailServiceImpl implements EventDetailService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventImagesRepository eventImagesRepository;
    private final EventOptionsRepository eventOptionsRepository;

    @Override
    public EventDetailResponse getEventDetailById(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        List<TicketType> ticketTypes = ticketTypeRepository.findAllByEvent(event);
        List<String> eventImagesUrl = eventImagesRepository.findImageUrlsByEvent(event);
        List<TicketTypeResponse> ticketTypeResponses = ticketTypes.stream().map(TicketTypeMapper::toTicketTypeResponse).toList();
        Long capacity = eventOptionsRepository.findCapacityByEvent(event);

        return EventDetailMapper.toEventDetailResponse(event,eventImagesUrl,ticketTypeResponses,capacity);
    }
}
