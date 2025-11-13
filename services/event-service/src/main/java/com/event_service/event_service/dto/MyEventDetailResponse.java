package com.event_service.event_service.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MyEventDetailResponse(
        MyEventsOverviewResponse eventStats,
        MyEventSummaryResponse eventSummary,
        List<MyEventTicketTypeStats> ticketTypes,
        Long totalInvitedGuests
) {
}
