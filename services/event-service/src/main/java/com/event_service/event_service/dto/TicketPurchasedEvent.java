package com.event_service.event_service.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TicketPurchasedEvent(
        String attendeeName,
        String attendeeEmail,
        List<TicketResponse> tickets,
        TicketEventDetailResponse eventDetails
) {
}
