package com.example.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserMobileMoneyCreationRequest(
        @NotBlank(message = "Network operator is required")
        String networkOperator,
        @NotBlank(message = "Mobile money number is required")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Mobile money number must be 10-15 digits, optionally starting with +"
        )
        String mobileMoneyNumber,
        @NotBlank(message = "Account holder name is required")
        @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
        @Pattern(
                regexp = "^[\\p{L}]+([ '-][\\p{L}]+)*$",
                message = "Full name must start and end with a letter, and contain only letters, spaces, hyphens, or apostrophes"
        )
        String accountHolderName
) {
}
