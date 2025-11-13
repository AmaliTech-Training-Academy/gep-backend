package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record MyEventsOverviewResponse(
        Long totalEvents,
        Long totalAttendees,
        Double totalTicketSales
) {
}
