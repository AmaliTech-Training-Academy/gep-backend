package com.example.common_libraries.dto.queue_events;

import com.example.common_libraries.dto.TicketEventDetailResponse;
import com.example.common_libraries.dto.TicketResponse;
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
