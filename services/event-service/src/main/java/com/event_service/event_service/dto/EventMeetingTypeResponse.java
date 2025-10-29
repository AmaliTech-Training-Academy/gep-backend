package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.EventMeetingTypeEnum;

public record EventMeetingTypeResponse(
        Long id,
        EventMeetingTypeEnum name
) {
}
