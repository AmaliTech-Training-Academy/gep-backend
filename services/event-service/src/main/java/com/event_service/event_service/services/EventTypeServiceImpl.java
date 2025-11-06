package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventTypeRequest;
import com.event_service.event_service.dto.EventTypeResponse;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.event_service.event_service.mappers.EventTypeMapper;
import com.event_service.event_service.models.EventType;
import com.event_service.event_service.repositories.EventTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventTypeServiceImpl implements EventTypeService {

    private final EventTypeRepository eventTypeRepository;
    private final EventTypeMapper eventTypeMapper;

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void save(EventTypeRequest eventTypeRequest) {
        EventType eventType = EventType.builder().name(eventTypeRequest.name()).build();
        eventTypeRepository.save(eventType);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public EventType findById(Long id) {
        return eventTypeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Event Type Not Found"));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void update(Long id, EventTypeRequest eventTypeRequest) {
        EventType eventType = findById(id);
        eventType.setName(eventTypeRequest.name());
        eventTypeRepository.save(eventType);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<EventTypeResponse> findAll() {
        return eventTypeRepository.findAll().stream()
                .map(eventTypeMapper::toEventTypeResponse)
                .collect(Collectors.toList());
    }
}
