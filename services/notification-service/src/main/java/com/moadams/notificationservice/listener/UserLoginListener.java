package com.moadams.notificationservice.listener;



import com.example.common_libraries.dto.queue_events.UserLoginEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserLoginListener {

    private final EmailService emailService;

    @SqsListener("${sqs.user-login-queue}")
    public void listenUserLogin(UserLoginEvent event){
        log.info("Received UserLoginEvent for email: {}", event.email());
        emailService.sendOtpEmail(event.email(), event.otp());
    }
}
