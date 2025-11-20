package com.event_service.event_service.dto;

import com.example.common_libraries.dto.HostsResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record MyEventDetailResponse(
        MyEventsOverviewResponse eventStats,
        MyEventSummaryResponse eventSummary,
        List<MyEventTicketTypeStats> ticketTypes,
        Long totalInvitedGuests,
        List<HostsResponse> eventHosts
) {
}
