package com.event_service.event_service.dto;

public record EventInvitationListResponse(
        Long id,
        String invitationTitle,
        String inviteeName,
        String invitationCode
) {
}
