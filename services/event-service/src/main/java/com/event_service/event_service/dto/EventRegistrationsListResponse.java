package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record EventRegistrationsListResponse (
        Long id,
        String name,
        String email,
        Long numberOfTickets,
        String ticketType
) {
}
