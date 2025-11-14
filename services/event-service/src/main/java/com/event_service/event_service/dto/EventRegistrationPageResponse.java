package com.event_service.event_service.dto;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record EventRegistrationPageResponse(
        Page<EventRegistrationsListResponse> eventRegistrations,
        List<TicketTypeResponse> ticketTypes,
        List<String> filters,
        Long capacity
) {
}
