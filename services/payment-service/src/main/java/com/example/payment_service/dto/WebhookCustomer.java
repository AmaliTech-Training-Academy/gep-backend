package com.example.payment_service.dto;

public record WebhookCustomer(
        long id,
        String first_name,
        String last_name,
        String email,
        String customer_code,
        String phone,
        String metadata,
        String risk_action,
        String international_format_phone
) {}


