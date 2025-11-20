package com.example.payment_service.dto;

public record PaystackResponse(
        String authorizationUrl,
        String reference
) {
}