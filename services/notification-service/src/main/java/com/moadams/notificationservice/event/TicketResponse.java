package com.moadams.notificationservice.event;

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
