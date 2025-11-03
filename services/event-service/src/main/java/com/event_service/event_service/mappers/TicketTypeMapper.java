package com.event_service.event_service.mappers;

import com.event_service.event_service.dto.TicketTypeResponse;
import com.event_service.event_service.models.TicketType;

public class TicketTypeMapper {
    private TicketTypeMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static TicketTypeResponse toTicketTypeResponse(TicketType ticketType) {
        return TicketTypeResponse
                .builder()
                .id(ticketType.getId())
                .type(ticketType.getType())
                .description(ticketType.getDescription())
                .price(ticketType.getPrice())
                .remainingTickets(ticketType.getQuantity()- ticketType.getSoldCount())
                .isPaid(ticketType.getIsPaid())
                .isActive(ticketType.getIsActive())
                .build();
    }
}
