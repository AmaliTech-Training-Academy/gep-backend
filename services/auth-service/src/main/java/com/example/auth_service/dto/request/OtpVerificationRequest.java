package com.example.auth_service.dto.request;

public record OtpVerificationRequest(
        String email,
        String otp
) {
}
