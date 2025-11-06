package com.moadams.notificationservice.listener;

import com.example.common_libraries.dto.queue_events.TicketPurchasedEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TicketPurchasedListener {
    private final EmailService emailService;

    @SqsListener("${sqs.ticket-purchased-event-queue-url}")
    public void listenTicketPurchased(TicketPurchasedEvent event){
        log.info("Ticket Purchased Event Received:");
        emailService.sendTicketPurchasedEmail(event);
    }
}
