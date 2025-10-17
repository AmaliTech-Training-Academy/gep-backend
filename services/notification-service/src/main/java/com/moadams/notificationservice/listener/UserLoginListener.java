package com.moadams.notificationservice.listener;


import com.moadams.notificationservice.event.UserLoginEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserLoginListener {

    private final EmailService emailService;

    @KafkaListener(topics = "user-login-topic", groupId ="${spring.kafka.consumer.group-id}")
    public void listenUserLogin(UserLoginEvent event){
        emailService.sendOtpEmail(event.email(), event.otp());
    }
}
