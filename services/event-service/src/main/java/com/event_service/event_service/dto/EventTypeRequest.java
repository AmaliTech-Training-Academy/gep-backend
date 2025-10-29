package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.EventTypeEnum;
import jakarta.validation.constraints.NotNull;

public record EventTypeRequest(
        @NotNull(message = "Event type is required")
        EventTypeEnum name
) {
}
