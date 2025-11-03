package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record ProcessPaymentEvent(
    Long eventRegistrationId,
    String attendeeEmail,
    String attendeeName,
    PaymentRequest paymentRequest
) { }
