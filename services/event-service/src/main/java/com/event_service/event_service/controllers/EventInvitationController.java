package com.event_service.event_service.controllers;

import com.event_service.event_service.dto.CustomApiResponse;
import com.event_service.event_service.dto.EventInvitationRequest;
import com.event_service.event_service.services.EventInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/event-invitations")
public class EventInvitationController {

    private final EventInvitationService eventInvitationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<?>> sendInvitation(
            @Valid @RequestBody EventInvitationRequest request
            ){
        eventInvitationService.sendEventInvitation(request);
        return ResponseEntity.ok(CustomApiResponse.success("Event invitation sent successfully"));
    }

}
