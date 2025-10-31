package com.moadams.notificationservice.event;

public record EventInvitationEvent(
        String eventTitle,
        String inviteeName,
        String inviteeEmail,
        String inviteLink
) {
}
