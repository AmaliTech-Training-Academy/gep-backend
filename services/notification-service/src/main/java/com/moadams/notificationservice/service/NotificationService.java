package com.moadams.notificationservice.service;

public interface NotificationService {
    void sendWelcomeEmail(String recipientEmail, String recipientName);
    void sendOtpEmail(String recipientEmail, String otpCode);
    void sendForgotPasswordEmail(String recipientEmail, String recipientName, String otpCode);
}
