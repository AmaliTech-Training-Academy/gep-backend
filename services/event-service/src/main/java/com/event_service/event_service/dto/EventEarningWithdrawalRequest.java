package com.event_service.event_service.dto;

import com.example.common_libraries.enums.WithdrawalMethod;
import jakarta.validation.constraints.*;

public record EventEarningWithdrawalRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Double amount,

        @NotNull(message = "Withdrawal method is required")
        WithdrawalMethod withdrawalMethod,

        @NotBlank(message = "Provider is required")
        String provider,

        @NotBlank(message = "Account number is required")
        @Pattern(regexp = "^[0-9]{0,16}$", message = "Account number must be between 0 and 16 digits")
        String accountNumber,

        @NotBlank(message = "Account name is required")
        @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
        String accountName
) {
}