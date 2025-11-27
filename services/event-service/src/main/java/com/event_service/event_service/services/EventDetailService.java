package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventDetailResponse;
import com.event_service.event_service.dto.EventEditPageResponse;

public interface EventDetailService {
    EventDetailResponse getEventDetailById(Long id);
    EventEditPageResponse getEventEditPageById(Long id);
}
