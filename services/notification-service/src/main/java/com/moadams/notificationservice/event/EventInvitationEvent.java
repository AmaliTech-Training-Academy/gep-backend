package com.moadams.notificationservice.event;

public record EventInvitationEvent(
        String eventTitle,
        String inviteeEmail,
        String inviteLink
) {
}
