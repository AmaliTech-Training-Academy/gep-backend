package com.moadams.notificationservice.listener;

import com.example.common_libraries.dto.queue_events.PaymentStatusEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusListener {
    private final EmailService emailService;

    @SqsListener("${sqs.payment-status-queue}")
    public void listenPaymentStatus(PaymentStatusEvent statusEvent){
        log.info("Payment Status Event Received: {}", statusEvent);
        emailService.sendPaymentStatusNotificationMail(statusEvent);
    }
}
