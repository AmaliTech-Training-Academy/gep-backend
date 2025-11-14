package com.example.common_libraries.dto.queue_events;

import com.example.common_libraries.dto.EventRegistrationResponse;
import com.example.common_libraries.dto.PaymentRequest;
import lombok.Builder;

@Builder
public record ProcessPaymentEvent(
        Double amount,

        Long ticketTypeId,

        Long numberOfTickets,

        String fullName,

        String email,

        EventRegistrationResponse eventRegistrationResponse
) { }
