package com.example.auth_service.service.impl;

import com.example.auth_service.service.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RequiredArgsConstructor
@Service
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

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(recipientEmail);
            helper.setSubject("Welcome to Our Eventhub");
            helper.setFrom(adminEmail);
            helper.setText(htmlContent, true);
            mailSender.send(message);

        }catch (MessagingException e){
            System.out.println("Failed to send welcome email");
        }
    }
}
