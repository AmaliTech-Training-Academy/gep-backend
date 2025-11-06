package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.InviteeRole;

public record EventInviteeResponse(
        Long id,
        String inviteeName,
        String inviteeEmail,
        InviteeRole role
) {
}
