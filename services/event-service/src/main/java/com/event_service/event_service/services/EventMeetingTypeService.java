package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventMeetingTypeRequest;
import com.event_service.event_service.dto.EventMeetingTypeResponse;
import com.event_service.event_service.models.EventMeetingType;

import java.util.List;

public interface EventMeetingTypeService {
    void  createEventMeetingType(EventMeetingTypeRequest eventMeetingTypeRequest);
    EventMeetingType findEventMeetingTypeById(Long id);
    List<EventMeetingTypeResponse> findAllEventMeetingTypes();
    void updateEventMeetingType(Long id, EventMeetingTypeRequest eventMeetingTypeRequest);
}
