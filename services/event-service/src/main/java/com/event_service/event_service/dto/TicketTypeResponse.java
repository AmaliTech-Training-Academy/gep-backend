package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record TicketTypeResponse(
        Long id,
        String type,
        String description,
        Double price,
        Boolean isActive,
        Long remainingTickets,
        Boolean isPaid
) {
}
