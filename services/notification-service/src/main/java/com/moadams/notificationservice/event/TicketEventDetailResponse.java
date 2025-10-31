package com.moadams.notificationservice.event;

import lombok.Builder;

import java.time.Instant;

@Builder
public record TicketEventDetailResponse(
        Long id,
        String title,
        String description,
        Instant startTime,
        String eventMeetingType
) {
}