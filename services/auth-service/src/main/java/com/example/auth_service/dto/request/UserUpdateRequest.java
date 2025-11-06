package com.example.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank(message = "Full name is required")
        String fullName,
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Phone is required")
        String phone,
        @NotBlank(message = "Address is required")
        String address,
        boolean status
) {
}
