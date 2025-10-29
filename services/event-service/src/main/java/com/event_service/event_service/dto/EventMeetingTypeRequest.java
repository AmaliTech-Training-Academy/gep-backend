package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import jakarta.validation.constraints.NotNull;

public record EventMeetingTypeRequest(
        @NotNull(message = "Event meeting type is required")
        EventMeetingTypeEnum name
) {
}
