package com.event_service.event_service.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record EventDetailResponse(
        Long id,
        String title,
        String description,
        Instant startTime,
        String flyerUrl,
        Long capacity,
        String location,
        Long totalAttendees,
        Boolean isPaid,
        List<String> eventImagesUrl,
        List<TicketTypeResponse> ticketTypes
        //Todo Add venue sections
) {
}
