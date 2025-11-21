package com.event_service.event_service.dto;

import java.math.BigDecimal;

public record EventSectionRequest(
        String name,
        Long capacity,
        BigDecimal price,
        String description,
        String color
) {
}
