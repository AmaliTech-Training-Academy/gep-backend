package com.moadams.notificationservice.listener;

import com.moadams.notificationservice.event.ForgotPasswordEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ForgotPasswordListener{

    private final EmailService emailService;

    @SqsListener("${sqs.password-reset-queue}")
    public void listenPasswordReset(ForgotPasswordEvent event){
        log.info("Received ForgotPasswordEvent for email: {}", event.email());
        emailService.sendForgotPasswordEmail(event.email(), event.fullName(), event.otp());
    }

}
