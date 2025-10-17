package com.moadams.notificationservice.listener;

import com.moadams.notificationservice.event.UserRegisteredEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserRegisteredListener {

    private final EmailService emailService;

    @KafkaListener(topics = "user-registration-topic", groupId ="${spring.kafka.consumer.group-id}")
    public void listenUserRegistered(UserRegisteredEvent event){
        emailService.sendWelcomeEmail(event.email(), event.fullName());
    }

}
