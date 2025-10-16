package com.moadams.notificationservice.listener;

import com.moadams.notificationservice.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredListener.class);

    @KafkaListener(topics = "user-registration-topic", groupId ="${spring.kafka.consumer.group-id}")
    public void listenUserRegistered(UserRegisteredEvent event){
        log.info("User registered: {}", event);
        sendWelcomeEmail(event);
    }

    private void sendWelcomeEmail(UserRegisteredEvent event){
        log.info("Sending welcome email to: {}", event.email());
    }

}
