package com.moadams.notificationservice.listener;

import com.example.common_libraries.dto.queue_events.EventCreationNotificationMessage;
import com.moadams.notificationservice.service.impl.EmailService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EventCreationListener {
    private final EmailService emailService;

    @SqsListener("${sqs.event-creation-queue-name}")
    public void listenEventCreation(EventCreationNotificationMessage event){
        emailService.sendEventCreationNotificationMail(event);
    }
}
