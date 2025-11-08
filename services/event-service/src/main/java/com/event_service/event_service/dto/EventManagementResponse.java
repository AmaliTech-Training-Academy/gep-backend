package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.EventStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record EventManagementResponse(
        Long id,
        String title,
        String organizer,
        Instant startTime,
        Instant endTime,
        Long attendeeCount,
        EventStatus status
) { }

