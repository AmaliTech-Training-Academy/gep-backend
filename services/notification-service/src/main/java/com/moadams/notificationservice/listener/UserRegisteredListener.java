package com.moadams.notificationservice.listener;

import com.moadams.notificationservice.event.UserRegisteredEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class UserRegisteredListener {

    private final EmailService emailService;

    @SqsListener("user-registration-queue")
    public void listenUserRegistered(UserRegisteredEvent  event){
        emailService.sendWelcomeEmail(event.email(), event.fullName());
    }

}
