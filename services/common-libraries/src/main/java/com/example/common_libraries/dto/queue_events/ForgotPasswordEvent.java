package com.example.common_libraries.dto.queue_events;

public record ForgotPasswordEvent(
        String email,
        String fullName,
        String otp) {
}