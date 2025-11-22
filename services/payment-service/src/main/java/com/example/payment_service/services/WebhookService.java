package com.example.payment_service.services;

import com.example.payment_service.dto.PaystackWebhook;

public interface WebhookService {
    void handleWebhook(String rawBody);
}
