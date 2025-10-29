package com.event_service.event_service.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record EventOptionsRequest(
        @NotNull(message = "Ticket price is required")
        @PositiveOrZero(message = "Ticket price must be non-negative number and greater than or equal to 0")
        BigDecimal ticketPrice,
        @NotNull(message = "Event approval is required")
        boolean requiresApproval,
        @NotNull(message = "Event capacity is required")
        @Positive(message = "Event capacity must be a positive value")
        Long capacity
) {
}
