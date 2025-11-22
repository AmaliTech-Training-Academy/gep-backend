package com.example.payment_service.listener;

import com.example.payment_service.services.WebhookService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventListener {
    private final WebhookService webhookService;

    @SqsListener("${sqs.webhook-event-queue}")
    public void processWebhookEvent(String rawBody){
        log.info("Received webhook event: {}", rawBody);
        webhookService.handleWebhook(rawBody);
    }
}
