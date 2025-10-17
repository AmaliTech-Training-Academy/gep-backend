package com.example.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshAccessTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
