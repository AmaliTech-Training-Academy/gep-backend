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

    // Computed status based on start/end times
    default EventStatus getStatus() {
        Instant now = Instant.now();
        Instant start = getStartTime();
        Instant end = getEndTime();

        if (start.isBefore(now) && end.isAfter(now)) {
            return EventStatus.ACTIVE;
        } else if (end.isBefore(now)) {
            return EventStatus.COMPLETED;
        } else if(start.isAfter(now)) {
            return EventStatus.DRAFT;
        } else {
            return EventStatus.PENDING;
        }
    }
}
