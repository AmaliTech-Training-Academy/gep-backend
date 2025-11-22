package com.example.payment_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookData(
        long id,
        String domain,
        String status,
        String reference,
        int amount,
        String message,
        String gateway_response,
        String paid_at,
        String created_at,
        String channel,
        String currency,
        String ip_address,
        String metadata,
        Integer fees,
        Object fees_breakdown,
        WebhookAuthorization authorization,
        WebhookCustomer customer,
        int requested_amount
) {}