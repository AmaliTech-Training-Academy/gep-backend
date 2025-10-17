package com.example.auth_service.event;

public record UserLoginEvent(
        String email,
        String otp
) {
}
