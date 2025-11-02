package com.event_service.event_service.dto;

import lombok.Builder;

import java.time.Instant;
@Builder
public record TicketEventDetailResponse(
        Long id,
        String title,
        String description,
        Instant startTime,
        String eventMeetingType,
        String zoneId
) {
}