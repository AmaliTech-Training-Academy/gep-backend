package com.event_service.event_service.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UpcomingEventResponse(
        String eventTitle,
        Instant startTime,
        int attendeeCount
) {
}
