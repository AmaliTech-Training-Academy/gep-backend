package com.moadams.notificationservice.listener;


import com.example.common_libraries.dto.queue_events.UserRegisteredEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class UserRegisteredListener {

    private final EmailService emailService;

    @SqsListener("${sqs.user-registration-queue}")
    public void listenUserRegistered(UserRegisteredEvent event){
        emailService.sendWelcomeEmail(event.email(), event.fullName());
    }

}
