package com.event_service.event_service.dto;

import java.time.Instant;

public record EventResponse(
        Long id,
        String title,
        String description,
        Instant startTime,
        String location,
        String flyerUrl,
        String timeZoneOffSet,
        String createdBy
) {
}
