package com.event_service.event_service.dto;

import java.math.BigDecimal;

public record EventOptionsRequest(
        BigDecimal ticketPrice,
        boolean requiresApproval,
        Long capacity
) {
}
