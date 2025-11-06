package com.example.common_libraries.dto.queue_events;

public record UserLoginEvent(
        String email,
        String otp
) {
}
