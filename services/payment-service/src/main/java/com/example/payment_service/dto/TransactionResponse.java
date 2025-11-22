package com.example.payment_service.dto;

import com.example.payment_service.models.TransactionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record TransactionResponse(
        String transactionId,
        String eventName,
        String eventOrganizer,
        String attendeeEmail,
        BigDecimal amount,
        String paymentMethod,
        TransactionStatus status,
        Instant transactionTime
) {}
