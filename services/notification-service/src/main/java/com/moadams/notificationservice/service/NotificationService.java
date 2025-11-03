package com.moadams.notificationservice.service;

import com.moadams.notificationservice.event.TicketPurchasedEvent;

import com.moadams.notificationservice.event.EventInvitationEvent;

public interface NotificationService {
    void sendWelcomeEmail(String recipientEmail, String recipientName);
    void sendOtpEmail(String recipientEmail, String otpCode);
    void sendForgotPasswordEmail(String recipientEmail, String recipientName, String otpCode);
    void sendTicketPurchasedEmail(TicketPurchasedEvent ticketPurchasedEvent);
    void sendEventInvitationMail(EventInvitationEvent event);
}
