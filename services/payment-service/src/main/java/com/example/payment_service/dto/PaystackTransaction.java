package com.example.payment_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackTransaction(
        boolean status,
        String message,
        Data data
) {
    public record Data(
            @JsonProperty("authorization_url") String authorizationUrl,
            @JsonProperty("access_code") String accessCode,
            String reference
    ) {}
}
