package com.event_service.event_service.dto.projection;

public interface EventStatProjection {
    Long getTotalEvents();
    Long getActiveEvents();
    Long getCompletedEvents();
    Long getCanceledEvents();
    Long getDraftEvents();
}
