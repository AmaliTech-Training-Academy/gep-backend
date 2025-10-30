package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record TicketResponse(
        Long id,
        String ticketType,
        String ticketCode,
        String qrCodeUrl,
        String status
) {
}
