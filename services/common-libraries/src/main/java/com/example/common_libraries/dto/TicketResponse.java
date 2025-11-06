package com.example.common_libraries.dto;

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
