package com.moadams.notificationservice.listener;

import com.example.common_libraries.dto.queue_events.EventCreationNotificationMessage;
import com.example.common_libraries.dto.queue_events.WithdrawalNotificationEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WithdrawalRequestListener {

    private final EmailService emailService;

    @SqsListener("${sqs.withdrawal-notification-queue}")
    public void listenWithdrawalRequest(WithdrawalNotificationEvent event){
        emailService.sendWithdrawalNotification(event);
    }
}
