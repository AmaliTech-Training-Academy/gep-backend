package com.example.auth_service.event;

public record UserRegisteredEvent(
        Long userId,
        String fullName,
        String email
) {
}
