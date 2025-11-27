package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import lombok.Builder;

@Builder
public record EventMeetingTypeResponse(
        Long id,
        EventMeetingTypeEnum name
) {
}
