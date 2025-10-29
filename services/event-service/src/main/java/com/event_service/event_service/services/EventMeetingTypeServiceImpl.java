package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventMeetingTypeRequest;
import com.event_service.event_service.dto.EventMeetingTypeResponse;
import com.event_service.event_service.exceptions.ResourceNotFound;
import com.event_service.event_service.mappers.EventMeetingMapper;
import com.event_service.event_service.models.EventMeetingType;
import com.event_service.event_service.repositories.EventMeetingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventMeetingTypeServiceImpl implements EventMeetingTypeService {

    private final EventMeetingTypeRepository eventMeetingTypeRepository;
    private final  EventMeetingMapper eventMeetingMapper;

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void createEventMeetingType(EventMeetingTypeRequest eventMeetingTypeRequest) {
        EventMeetingType eventMeetingType = EventMeetingType.builder().name(eventMeetingTypeRequest.name()).build();
        eventMeetingTypeRepository.save(eventMeetingType);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public EventMeetingType findEventMeetingTypeById(Long id) {
        return eventMeetingTypeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFound("EventMeetingType not found"));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<EventMeetingTypeResponse> findAllEventMeetingTypes() {
        return eventMeetingTypeRepository.findAll()
                .stream().map(eventMeetingMapper::toEventMeetingTypeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateEventMeetingType(Long id, EventMeetingTypeRequest eventMeetingTypeRequest) {
        EventMeetingType eventMeetingType = findEventMeetingTypeById(id);
        eventMeetingType.setName(eventMeetingTypeRequest.name());
        eventMeetingTypeRepository.save(eventMeetingType);
    }
}
