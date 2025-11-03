package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record TicketVerificationResponse(
        String code,
        String message,
        String ticketType,
        Double price
) {
}
