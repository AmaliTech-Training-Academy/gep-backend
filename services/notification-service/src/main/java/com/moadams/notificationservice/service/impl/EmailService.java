package com.moadams.notificationservice.service.impl;

import com.moadams.notificationservice.event.TicketPurchasedEvent;
import com.moadams.notificationservice.event.TicketResponse;
import com.moadams.notificationservice.event.EventInvitationEvent;
import com.moadams.notificationservice.service.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements NotificationService {

    @Value("${spring.mail.username}")
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
            context.setVariable("inviteeName", event.inviteeName());
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


    @Override
    public void sendTicketPurchasedEmail(TicketPurchasedEvent ticketPurchasedEvent) {
        try{
            Context context = new Context();
            context.setVariable("attendeeName", ticketPurchasedEvent.attendeeName());
            context.setVariable("attendeeEmail", ticketPurchasedEvent.attendeeEmail());
            context.setVariable("tickets", ticketPurchasedEvent.tickets());
            context.setVariable("eventDetails", ticketPurchasedEvent.eventDetails());

            String htmlContent = templateEngine.process("tickets-purchased", context);

            // Send email with embedded QR codes
            sendEmailWithQRCodes(htmlContent, ticketPurchasedEvent.attendeeEmail(),
                    "Your Event Tickets", ticketPurchasedEvent.tickets());

            log.info("Ticket Purchased Email Sent to {}", ticketPurchasedEvent.attendeeEmail());
        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send ticket purchase email {}", e.getMessage());
        } catch (Exception e) {
            log.error("An Error occurred while sending ticket purchased email {}", e.getMessage());
        }
    }


    private void sendEmailWithQRCodes(String htmlContent, String recipientEmail,
                                      String subject, List<TicketResponse> tickets)
            throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(adminEmail, "EventHub");
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        // Embed QR codes as inline images
        for (TicketResponse ticket : tickets) {
            if (ticket.qrCodeUrl() != null && ticket.qrCodeUrl().startsWith("data:image/png;base64,")) {
                // Extract base64 data
                String base64Data = ticket.qrCodeUrl().substring("data:image/png;base64,".length());
                byte[] qrCodeBytes = Base64.getDecoder().decode(base64Data);

                // Create data source
                ByteArrayDataSource dataSource = new ByteArrayDataSource(qrCodeBytes, "image/png");

                // Add as inline resource with CID
                String cid = "qrcode-" + ticket.id();
                helper.addInline(cid, dataSource);
            }
        }

        mailSender.send(message);
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