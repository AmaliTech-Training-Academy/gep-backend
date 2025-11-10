package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record EventStatisticsResponse(
        long totalEvents,
        long activeEvents,
        long canceledEvents,
        long completedEvents,
        long draftEvents
) {
}
