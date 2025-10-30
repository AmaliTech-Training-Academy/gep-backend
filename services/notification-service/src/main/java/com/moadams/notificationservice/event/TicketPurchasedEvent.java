package com.moadams.notificationservice.event;

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
