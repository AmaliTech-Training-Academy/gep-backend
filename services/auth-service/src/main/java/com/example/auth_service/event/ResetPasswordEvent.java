package com.example.auth_service.event;

public record ResetPasswordEvent(
        String email,
        String fullName,
        String otp
) {
}
