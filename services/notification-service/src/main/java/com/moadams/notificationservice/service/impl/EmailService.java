package com.moadams.notificationservice.service.impl;

import com.example.common_libraries.dto.queue_events.TicketPurchasedEvent;
import com.example.common_libraries.dto.queue_events.EventInvitationEvent;
import com.example.common_libraries.dto.TicketResponse;
import com.example.common_libraries.dto.queue_events.UserInvitedEvent;
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
            context.setVariable("invitationTitle", event.eventTitle());
            context.setVariable("invitationLink", event.inviteLink());
            context.setVariable("role", event.role());

            String template_name = determineTemplateForRole(event.role());
            String htmlContent = templateEngine.process(template_name, context);
            sendEmail(htmlContent, event.inviteeEmail(), "You have been invited");

        }catch (MessagingException | UnsupportedEncodingException e){
            log.error("Failed to send event invitation email");
        }
    }

    @Override
    public void sendUserInvitationEmail(UserInvitedEvent event) {
        try{
            log.info("Sending user invitation email to {}", event.email());
            String loginUrl = frontendBaseUrl + "/app/auth/login";
            Context context = new Context();
            context.setVariable("recipientName", event.fullName());
            context.setVariable("email", event.email());
            context.setVariable("role", event.role());
            context.setVariable("password", event.password());
            context.setVariable("loginUrl", loginUrl);

            String htmlContent = templateEngine.process("user-invitation", context);
            sendEmail(htmlContent, event.email(), "Welcome to Eventhub - Your Account Is Ready");
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
            Context context = new Context();
            context.setVariable("attendeeName", ticketPurchasedEvent.attendeeName());
            context.setVariable("attendeeEmail", ticketPurchasedEvent.attendeeEmail());
            context.setVariable("tickets", ticketPurchasedEvent.tickets());
            context.setVariable("eventDetails", ticketPurchasedEvent.eventDetails());
            context.setVariable("organizer", "Organizer Name");
            context.setVariable("duration", "N/A");

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