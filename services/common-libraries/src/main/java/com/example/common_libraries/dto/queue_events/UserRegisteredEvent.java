package com.example.common_libraries.dto.queue_events;

public record UserRegisteredEvent(
        Long userId,
        String fullName,
        String email
) {
}
