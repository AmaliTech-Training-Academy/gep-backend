package com.event_service.event_service.controllers;

import com.event_service.event_service.dto.EventInvitationAcceptanceRequest;
import com.event_service.event_service.dto.EventInvitationListResponse;
import com.event_service.event_service.dto.EventInvitationRequest;
import com.event_service.event_service.services.EventInvitationService;
import com.example.common_libraries.dto.CustomApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<Page<EventInvitationListResponse>>> getInvitations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
    ){
        String[] sortParts = sort[0].split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Page<EventInvitationListResponse> invitations = eventInvitationService.getInvitationList(
                PageRequest.of(page, size, Sort.by(direction, sortParts[0]))
        );
        return ResponseEntity.ok(CustomApiResponse.success(invitations));
    }


    @PutMapping("/{id}/resend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<?>> resendInvitation(@PathVariable Long id){
        eventInvitationService.resendInvitation(id);
        return ResponseEntity.ok(CustomApiResponse.success("Event invitation resent successfully"));
    }

    @PostMapping("/accept-invitation")
    public ResponseEntity<CustomApiResponse<?>> acceptInvitation(
            @Valid @RequestBody EventInvitationAcceptanceRequest request
    ){
        eventInvitationService.acceptInvitation(request);
        return ResponseEntity.ok(CustomApiResponse.success("Event invitation accepted successfully"));
    }

}
