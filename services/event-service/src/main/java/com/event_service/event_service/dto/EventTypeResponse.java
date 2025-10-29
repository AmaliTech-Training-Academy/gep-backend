package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.EventTypeEnum;

public record EventTypeResponse(
        Long id,
        EventTypeEnum name
) {
}
