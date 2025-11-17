package com.example.common_libraries.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record EventRegistrationResponse(
        Long id,
        String eventTitle,
        String location,
        String organizer,
        Instant startDate,
        String authorizationUrl
) {
}
