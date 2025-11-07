package com.example.common_libraries.dto.queue_events;

public record EventInvitationEvent(
        String eventTitle,
        String inviteeName,
        String inviteeEmail,
        String inviteLink,
        String role
) {
}
