package com.event_service.event_service.controllers;

import com.event_service.event_service.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @GetMapping("/verify/{ticketCode}")
    public ResponseEntity<?> verifyTicket(@PathVariable String ticketCode){
        return ResponseEntity.ok(ticketService.verifyTicket(ticketCode));
    }
}
