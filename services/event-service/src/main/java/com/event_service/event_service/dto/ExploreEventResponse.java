package com.event_service.event_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ExploreEventResponse(
        Long id,
        String title,
        String description,
        Instant startTime,
        String location,
        String flyerUrl,
        BigDecimal ticketPrice,
        Long invitationCount
) {}
