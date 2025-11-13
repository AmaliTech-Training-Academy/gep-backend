package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record MyEventDetailResponse(
        MyEventsOverviewResponse eventStats,
        MyEventSummaryResponse eventSummary
) {
}
