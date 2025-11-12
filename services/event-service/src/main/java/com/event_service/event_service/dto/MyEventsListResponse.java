package com.event_service.event_service.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record MyEventsListResponse(
        Long id,
        String title,
        Instant startTime,
        String location,
        String flyerUrl,
        Long attendeesCount,
        Boolean isPaid
) {
}
