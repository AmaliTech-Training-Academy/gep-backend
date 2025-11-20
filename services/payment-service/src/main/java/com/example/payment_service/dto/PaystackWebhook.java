package com.example.payment_service.dto;

public record PaystackWebhook(
        String event,
        WebhookData data
) {}
