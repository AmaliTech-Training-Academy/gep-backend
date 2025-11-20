package com.example.common_libraries.dto;

import lombok.Builder;

import java.time.Instant;
@Builder
public record TicketEventDetailResponse(
        Long id,
        String title,
        String organizerName,
        String description,
        Instant startTime,
        Instant endTime,
        String eventMeetingType,
        String zoneId
) { }