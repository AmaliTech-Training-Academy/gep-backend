package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.EventTypeEnum;
import lombok.Builder;

@Builder
public record EventTypeResponse(
        Long id,
        EventTypeEnum name
) {
}
