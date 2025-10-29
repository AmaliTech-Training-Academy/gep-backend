package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventTypeRequest;
import com.event_service.event_service.dto.EventTypeResponse;
import com.event_service.event_service.models.EventType;

import java.util.List;

public interface EventTypeService {
    void  save(EventTypeRequest eventTypeRequest);
    EventType findById(Long id);
    void update(Long id, EventTypeRequest eventTypeRequest);
    List<EventTypeResponse> findAll();
}
