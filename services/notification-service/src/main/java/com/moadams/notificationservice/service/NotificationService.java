package com.moadams.notificationservice.service;


import com.example.common_libraries.dto.queue_events.*;

public interface NotificationService {
    void sendWelcomeEmail(String recipientEmail, String recipientName);
    void sendOtpEmail(String recipientEmail, String otpCode);
    void sendForgotPasswordEmail(String recipientEmail, String recipientName, String otpCode);
    void sendTicketPurchasedEmail(TicketPurchasedEvent ticketPurchasedEvent);
    void sendEventInvitationMail(EventInvitationEvent event);
    void sendUserInvitationEmail(UserInvitedEvent event);
    void sendEventCreationNotificationMail(EventCreationNotificationMessage event);
    void sendPaymentStatusNotificationMail(PaymentStatusEvent statusEvent);
}
