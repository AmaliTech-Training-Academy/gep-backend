package com.example.common_libraries.dto.queue_events;

public record ResetPasswordEvent(
        String email,
        String fullName,
        String otp
) {
}
