package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.InvitationStatus;

import java.util.List;

public record EventInvitationDetailsResponse(
        Long id,
        String invitationTitle,
        Long event,
        String message,
        InvitationStatus status,
        List<EventInviteeResponse> invitees
) {
}
