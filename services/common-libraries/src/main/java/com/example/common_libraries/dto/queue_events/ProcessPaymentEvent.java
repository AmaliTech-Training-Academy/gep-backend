package com.example.common_libraries.dto.queue_events;

import com.example.common_libraries.dto.PaymentRequest;
import lombok.Builder;

@Builder
public record ProcessPaymentEvent(
    Long eventRegistrationId,
    String attendeeEmail,
    String attendeeName,
    PaymentRequest paymentRequest
) { }
