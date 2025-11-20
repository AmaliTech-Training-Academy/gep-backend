package com.example.common_libraries.dto;

public record PaystackResponse(
        String authorizationUrl,
        String reference
) {
}