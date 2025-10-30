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
                .description(ticketType.getDescription())
                .price(ticketType.getPrice())
                .isActive(ticketType.getIsActive())
                .build();
    }
}
