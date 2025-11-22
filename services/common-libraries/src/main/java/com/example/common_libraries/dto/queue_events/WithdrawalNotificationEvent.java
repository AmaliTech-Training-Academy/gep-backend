package com.example.common_libraries.dto.queue_events;

import java.time.LocalDateTime;

public record WithdrawalNotificationEvent(
        String email,
        String fullName,
        Double amountWithdrawn,
        String paymentMethod,
        LocalDateTime withdrawalDate,
        String provider,
        String accountNumber
) {
}
