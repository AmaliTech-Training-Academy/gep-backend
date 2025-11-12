package com.example.common_libraries.dto.queue_events;

public record UserInvitedEvent(
        String fullName,
        String email,
        String password,
        String role
) {
}
