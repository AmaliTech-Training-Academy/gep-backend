package com.example.payment_service.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransactionRequest(
        @Email(message = "Email name is required")
        String email,
        @NotNull(message = "Total is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Total must be greater than 0")
        BigDecimal price
) {
}

