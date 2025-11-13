package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record MyEventTicketTypeStats(
        Long id,
        String name,
        Long remainingTickets,
        Long soldTickets
) {
}
