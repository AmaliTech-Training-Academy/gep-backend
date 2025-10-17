package com.example.auth_service.service;

public interface OtpService {
    String generateOtp(String key);
    boolean verifyOtp(String key, String otp);
}
