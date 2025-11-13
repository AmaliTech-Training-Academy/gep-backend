package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record EventStatisticsResponse(
        Long totalEvents,
        Long activeEvents,
        Long canceledEvents,
        Long completedEvents,
        Long draftEvents
) {
}
