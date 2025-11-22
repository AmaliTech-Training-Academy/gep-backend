package com.moadams.notificationservice.service.impl;

import com.example.common_libraries.dto.queue_events.*;
import com.example.common_libraries.dto.TicketResponse;
import com.moadams.notificationservice.service.NotificationService;
import com.moadams.notificationservice.utils.ICSGenerator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements NotificationService {

    @Value("${spring.mail.username}")
    private String adminEmail;

    @Value("${events.virtual.ticket.verification.url}")
    private String virtualTicketVerificationUrl;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ICSGenerator icsGenerator;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

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
            context.setVariable("invitationTitle", event.eventName());
            context.setVariable("invitationLink", event.inviteLink());
            context.setVariable("role", event.role());

            String template_name = determineTemplateForRole(event.role());
            String htmlContent = templateEngine.process(template_name, context);
            sendEmail(htmlContent, event.inviteeEmail(), event.eventTitle());

        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send event invitation email");
        }
    }

    @Override
    public void sendEventCreationNotificationMail(EventCreationNotificationMessage event) {
        try{
            Context context = new Context();
            context.setVariable("eventName", event.eventTitle());
            context.setVariable("createdBy", event.createdBy());


            String htmlContent = templateEngine.process("event-created", context);
            sendEmail(htmlContent, event.adminEmail(), "New Event Created");

        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send event creation email");
        }
    }

    @Override
    public void sendPaymentStatusNotificationMail(PaymentStatusEvent statusEvent) {
        try{
            log.info("Sending payment status email to {}", statusEvent.email());

            BigDecimal amount = statusEvent.amount();
            String formattedAmount = String.format("%.2f", amount);
            String m = "Your payment of ";

            String message = switch (statusEvent.status()) {
                case "SUCCESS" -> m + formattedAmount + " has been successful.";
                case "FAILED"  -> m + formattedAmount + " has failed.";
                case "PENDING" -> m + formattedAmount + " is initiated.";
                default        -> m + formattedAmount + " has been cancelled.";
            };


            Context context = new Context();
            context.setVariable("transactionId", statusEvent.transactionId());
            context.setVariable("fullName", statusEvent.fullName());
            context.setVariable("paymentMethod", statusEvent.paymentMethod());
            context.setVariable("status", statusEvent.status());
            context.setVariable("amount", statusEvent.amount());
            context.setVariable("timestamp", statusEvent.timestamp());
            context.setVariable("message", message);

            String htmlContent = templateEngine.process("payment-status", context);
            sendEmail(htmlContent, statusEvent.email(), "Payment Status");
        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send payment status email: {}", e.getMessage());
        }
    }

    @Override
    public void sendUserInvitationEmail(UserInvitedEvent event) {
        try{
            log.info("Sending user invitation email to {}", event.email());
            String invitationUrl = frontendBaseUrl + "/auth/invitation/accept?token=" + event.invitationToken();
            Context context = new Context();
            context.setVariable("fullName", event.fullName());
            context.setVariable("message", event.message());
            context.setVariable("role", event.role());
            context.setVariable("invitationUrl", invitationUrl);

            String htmlContent = templateEngine.process("user-invitation", context);
            sendEmail(htmlContent, event.email(), "You have been invited");
        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send invitation email");
        }
    }

    private String determineTemplateForRole(String role){
        return switch (role.toUpperCase()) {
            case "ORGANISER", "CO_ORGANIZER" -> "event-invitation-organiser";
            default -> "event-invitation-attendee";
        };
    }

    @Override
    public void sendOtpEmail(String recipientEmail, String otpCode) {
        try{
            Context context = new Context();
            context.setVariable("otpCode", otpCode);

            String htmlContent = templateEngine.process("verify-otp", context);

            sendEmail(htmlContent, recipientEmail, "Verify OTP");
            log.info("OTP Email Sent to {}", recipientEmail);
        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send otp email");
        }
    }


    @Override
    public void sendTicketPurchasedEmail(TicketPurchasedEvent ticketPurchasedEvent) {
        try{
            Instant startTime = ticketPurchasedEvent.eventDetails().startTime();
            Instant endTime = ticketPurchasedEvent.eventDetails().endTime();
            Long durationInHours = Duration.between(startTime, endTime).toHours();

            Context context = new Context();
            context.setVariable("attendeeName", ticketPurchasedEvent.attendeeName());
            context.setVariable("attendeeEmail", ticketPurchasedEvent.attendeeEmail());
            context.setVariable("tickets", ticketPurchasedEvent.tickets());
            context.setVariable("eventDetails", ticketPurchasedEvent.eventDetails());
            context.setVariable("organizer", ticketPurchasedEvent.eventDetails().organizerName());
            context.setVariable("duration", durationInHours+" Hours");

            if(ticketPurchasedEvent.eventDetails().eventMeetingType().equals("VIRTUAL")){
                String verificationLink = String.format(
                        "%s?ticketCode=%s",
                        virtualTicketVerificationUrl,
                        ticketPurchasedEvent.tickets().getFirst().ticketCode()
                );

                context.setVariable("verificationLink", verificationLink);
                // send email for virtual event with ICS
                String htmlContent = templateEngine.process("virtual-tickets-purchased", context);
                sendICSEmailForVirtualEvent(htmlContent, ticketPurchasedEvent);
            }else{

                // Send email with embedded QR codes
                String htmlContent = templateEngine.process("tickets-purchased", context);
                sendEmailWithQRCodes(htmlContent, ticketPurchasedEvent.attendeeEmail(),
                        "Your Event Tickets", ticketPurchasedEvent.tickets());
            }

            log.info("Ticket Purchased Email Sent to {}", ticketPurchasedEvent.attendeeEmail());
        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send ticket purchase email {}", e.getMessage());
        } catch (Exception e) {
            log.error("An Error occurred while sending ticket purchased email {}", e.getMessage());
        }
    }

    public void sendWithdrawalNotification(WithdrawalNotificationEvent event) {
        try{
            Context context = new Context();
            context.setVariable("fullName", event.fullName());
            context.setVariable("amount", event.amountWithdrawn());
            context.setVariable("paymentMethod", event.paymentMethod());
            context.setVariable("completedDate", formatLocalDateTime(event.withdrawalDate()));
            context.setVariable("provider", event.provider());
            context.setVariable("accountNumber", event.accountNumber());

            String htmlContent = templateEngine.process("withdrawal-notification", context);
            sendEmail(htmlContent, event.email(), "Withdrawal Completed");

        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send event creation email");
        }
    }

    private String formatLocalDateTime(LocalDateTime dateTime){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return dateTime.format(formatter);
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


    public void sendICSEmailForVirtualEvent(String htmlContent,TicketPurchasedEvent ticketPurchasedEvent) throws MessagingException, UnsupportedEncodingException{
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(adminEmail, "EventHub");
        helper.setTo(ticketPurchasedEvent.attendeeEmail());
        helper.setSubject("Your Virtual Event");
        helper.setText(htmlContent, true);

        //generate and attach ICS file
        ByteArrayResource icsFile = icsGenerator.generateICS(ticketPurchasedEvent);
        if(icsFile.getFilename() != null){
            helper.addAttachment(icsFile.getFilename(), icsFile, "text/calendar; charset=UTF-8; method=REQUEST");
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