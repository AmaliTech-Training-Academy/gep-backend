package com.event_service.event_service.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record MyEventSummaryResponse(
        String title,
        String organizer,
        Instant startTime,
        String location,
        String eventStatus
) {
}
