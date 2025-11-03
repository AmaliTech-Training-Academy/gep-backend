package com.event_service.event_service.services;

import com.event_service.event_service.dto.TicketVerificationResponse;

public interface TicketService {
    TicketVerificationResponse verifyTicket(String ticketCode);
    Boolean isTicketCodeValid(String ticketCode);
    String getMeetingUrl(String ticketCode);
}
