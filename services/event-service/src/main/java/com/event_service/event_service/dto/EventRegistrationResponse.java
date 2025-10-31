package com.event_service.event_service.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record EventRegistrationResponse(
        String eventTitle,
        String location,
        String organizer,
        Instant startDate
) {
}
