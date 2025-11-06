package com.event_service.event_service.mappers;

import com.event_service.event_service.models.Ticket;
import com.example.common_libraries.dto.TicketEventDetailResponse;
import com.example.common_libraries.dto.TicketResponse;
import com.example.common_libraries.dto.queue_events.TicketPurchasedEvent;


import java.util.List;

public class TicketPurchasedEventMapper {
    private TicketPurchasedEventMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static TicketPurchasedEvent toTicketPurchasedEvent(String attendeeName, String attendeeEmail, List<TicketResponse> tickets, TicketEventDetailResponse eventDetails){
        return TicketPurchasedEvent
                .builder()
                .attendeeName(attendeeName)
                .attendeeEmail(attendeeEmail)
                .tickets(tickets)
                .eventDetails(eventDetails)
                .build();
    }

    public static TicketResponse toTicketResponse(Ticket ticket){
        return TicketResponse
                .builder()
                .id(ticket.getId())
                .ticketType(ticket.getTicketType().getType())
                .ticketCode(ticket.getTicketCode())
                .qrCodeUrl(ticket.getQrCodeUrl())
                .status(ticket.getStatus().name())
                .build();
    }
}
