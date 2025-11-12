package com.event_service.event_service.dto.projection;

import com.event_service.event_service.models.enums.EventStatus;

import java.time.Instant;

public interface EventManagementProjection {
    Long getId();
    String getTitle();
    String getOrganizer();
    Instant getStartTime();
    Instant getEndTime();
    Long getAttendeeCount();
    EventStatus getStatus();
}
