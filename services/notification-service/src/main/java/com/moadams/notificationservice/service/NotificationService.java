package com.moadams.notificationservice.service;


import com.example.common_libraries.dto.queue_events.EventInvitationEvent;
import com.example.common_libraries.dto.queue_events.TicketPurchasedEvent;

public interface NotificationService {
    void sendWelcomeEmail(String recipientEmail, String recipientName);
    void sendOtpEmail(String recipientEmail, String otpCode);
    void sendForgotPasswordEmail(String recipientEmail, String recipientName, String otpCode);
    void sendTicketPurchasedEmail(TicketPurchasedEvent ticketPurchasedEvent);
    void sendEventInvitationMail(EventInvitationEvent event);
}
