package com.example.common_libraries.dto;


import lombok.Builder;

@Builder
public record PaymentRequest(

       Double amount,

       Long ticketTypeId,

       Long numberOfTickets,

       String fullName,

       String email,

       EventRegistrationResponse eventRegistrationResponse
) { }
