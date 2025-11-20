package com.event_service.event_service.dto;

import java.time.Instant;
import java.util.List;

public record EventUpdateResponse(
        Long id,
        String title,
        String description,
        Instant startTime,
        String location,
        String flyerUrl,
        String timeZoneOffSet,
        List<EventImageResponse> images
) {
}
