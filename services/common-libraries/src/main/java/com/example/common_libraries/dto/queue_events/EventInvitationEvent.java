package com.example.common_libraries.dto.queue_events;

public record EventInvitationEvent(
        String eventTitle,
        String eventName,
        String inviteeName,
        String inviteeEmail,
        String inviteLink,
        String role
) {
}
