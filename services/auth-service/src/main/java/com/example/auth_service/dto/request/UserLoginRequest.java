package com.example.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        String email,

        @NotBlank(message ="Password is required")
        String password
) {
}
