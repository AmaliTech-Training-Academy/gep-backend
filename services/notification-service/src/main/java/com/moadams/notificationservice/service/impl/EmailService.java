package com.moadams.notificationservice.service.impl;

import com.moadams.notificationservice.event.EventInvitationEvent;
import com.moadams.notificationservice.service.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements NotificationService {

    @Value("${GOOGLE_USER}")
    private String adminEmail;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendWelcomeEmail(String recipientEmail, String recipientName) {
        try{
            Context context = new Context();
            context.setVariable("recipientName", recipientName);

            String htmlContent = templateEngine.process("welcome-email", context);
            sendEmail(htmlContent, recipientEmail, "Welcome to Our Eventhub");
        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send welcome email");
        }
    }

    @Override
    public void sendForgotPasswordEmail(String recipientEmail, String recipientName, String otpCode) {
        try{
            Context context = new Context();
            context.setVariable("recipientName", recipientName);
            context.setVariable("otpCode", otpCode);

            String htmlContent = templateEngine.process("forgot-password", context);
            sendEmail(htmlContent, recipientEmail, "Password Reset Request");

        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send forgot password email");
        }
    }

    @Override
    public void sendEventInvitationMail(EventInvitationEvent event) {
        try{
            Context context = new Context();
            context.setVariable("invitationTitle", event.eventTitle());
            context.setVariable("invitationLink", event.inviteLink());

            String htmlContent = templateEngine.process("event-invitation", context);
            sendEmail(htmlContent, event.inviteeEmail(), "You have been invited");

        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send event invitation email");
        }
    }

    @Override
    public void sendOtpEmail(String recipientEmail, String otpCode) {
        try{
            Context context = new Context();
            context.setVariable("otpCode", otpCode);

            String htmlContent = templateEngine.process("verify-otp", context);

            sendEmail(htmlContent, recipientEmail, "Verify OTP");

        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send otp email");
        }
    }

    private void sendEmail( String htmlContent, String recipientEmail, String subject) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setFrom(adminEmail, "EventHub");
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}