package com.moadams.notificationservice.listener;

import com.moadams.notificationservice.event.EventInvitationEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EventInvitationListener {

    private final EmailService emailService;

    @SqsListener("${sqs.event-invitation-queue-name}")
    public void listenEventInvitation(EventInvitationEvent event){
        emailService.sendEventInvitationMail(event);
    }
}
