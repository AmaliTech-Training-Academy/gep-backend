package com.example.auth_service.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserBankAccountUpdateRequest(
        String bankName,
        @Pattern(
                regexp = "^[0-9]{10,16}$",
                message = "Account number must be 10-16 digits"
        )
        String accountNumber,
        @Pattern(
                regexp = "^[\\p{L}]+([ '-][\\p{L}]+)*$",
                message = "Full name must start and end with a letter, and contain only letters, spaces, hyphens, or apostrophes"
        )
        @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
        String accountHolderName,
        Boolean isActive
) {
}
