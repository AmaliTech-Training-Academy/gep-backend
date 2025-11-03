package com.event_service.event_service.controllers;

import com.event_service.event_service.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @GetMapping("/verify/{ticketCode}")
    public ResponseEntity<?> verifyTicket(@PathVariable String ticketCode){
        return ResponseEntity.ok(ticketService.verifyTicket(ticketCode));
    }

    @GetMapping("/verifyVirtualTicket/join")
    public ResponseEntity<Void> verifyVirtualTickets(@RequestParam String ticketCode){
        if(ticketService.isTicketCodeValid(ticketCode)){
            URI redirectUri = URI.create(ticketService.getMeetingUrl(ticketCode));
            return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
