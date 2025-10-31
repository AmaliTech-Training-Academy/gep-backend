package com.event_service.event_service.event;

public record EventInvitationEvent(
        String eventTitle,
        String inviteeName,
        String inviteeEmail,
        String inviteLink
) {
}
