package com.event_service.event_service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventInvitationListResponse(
        Long id,
        String invitationTitle,
        Long eventId,
        String event,
        String createdBy,
        LocalDate lastEdited,
        Integer inviteeCount
) {
}
