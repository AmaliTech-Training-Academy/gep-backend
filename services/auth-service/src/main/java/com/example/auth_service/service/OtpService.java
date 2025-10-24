package com.example.auth_service.service;

public interface OtpService {
    void requestLoginOtp(String email);
    boolean verifyOtp(String key, String otp);
    void requestResetPasswordOtp(String email, String fullName);
}
