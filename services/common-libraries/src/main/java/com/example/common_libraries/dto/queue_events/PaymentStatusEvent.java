package com.example.common_libraries.dto.queue_events;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record PaymentStatusEvent(
        String transactionId,
        String email,
        String fullName,
        String paymentMethod,
        String status,
        BigDecimal amount,
        Instant timestamp
) {
}
