package com.event_service.event_service.controllers;

import com.event_service.event_service.dto.*;
import com.event_service.event_service.dto.EventInvitationAcceptanceRequest;
import com.event_service.event_service.dto.EventInvitationListResponse;
import com.event_service.event_service.dto.EventInvitationRequest;
import com.event_service.event_service.models.enums.InviteeRole;
import com.event_service.event_service.services.EventInvitationService;
import com.example.common_libraries.dto.CustomApiResponse;
import com.example.common_libraries.dto.UserCreationResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/event-invitations")
public class EventInvitationController {

    private final EventInvitationService eventInvitationService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<Object>> sendInvitation(
            @Valid @RequestBody EventInvitationRequest request
            ){
        eventInvitationService.sendEventInvitation(request);
        return ResponseEntity.ok(CustomApiResponse.success("Event invitation sent successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<Page<EventInvitationListResponse>>> getInvitations(
            @RequestParam(name = "page",
                    defaultValue = "0") int page,
            @RequestParam(name = "size",
                    defaultValue = "10") int size,
            @RequestParam(name = "sort",
                    defaultValue = "id,asc") String[] sort,
            @RequestParam(name = "search", required = false) String search
    ){
        String[] sortParts = sort[0].split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Page<EventInvitationListResponse> invitations = eventInvitationService.getInvitationList(
                PageRequest.of(page, size, Sort.by(direction, sortParts[0])), search
        );
        return ResponseEntity.ok(CustomApiResponse.success(invitations));
    }


    @PutMapping("/{id}/resend")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<Object>> resendInvitation(@PathVariable Long id){
        eventInvitationService.resendInvitation(id);
        return ResponseEntity.ok(CustomApiResponse.success("Event invitation resent successfully"));
    }

    @PostMapping("/accept-invitation/")
    public ResponseEntity<CustomApiResponse<UserCreationResponse>> acceptInvitation(
            @Valid @RequestBody EventInvitationAcceptanceRequest request, HttpServletResponse response
    ){
        UserCreationResponse acceptedInvite = eventInvitationService.acceptInvitation(request, response);
        return ResponseEntity.ok(CustomApiResponse.success("Event invitation accepted successfully", acceptedInvite));
    }

    @PostMapping("/accept-invitation/existing-user/{token}/")
    public ResponseEntity<CustomApiResponse<UserCreationResponse>> acceptInvitationForExistingUser(
            @PathVariable String token, HttpServletResponse response){
        UserCreationResponse acceptedInvite = eventInvitationService.acceptInvitationForExistingUser(token, response);
        return ResponseEntity.ok(CustomApiResponse.success("Event invitation accepted successfully",acceptedInvite));
    }

    @GetMapping("/saved-invites")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<Page<EventInvitationListResponse>>> getSavedInvitations(
            @RequestParam(name = "page",
                    defaultValue = "0") int page,
            @RequestParam(name = "size",
                    defaultValue = "10") int size,
            @RequestParam(name = "sort",
                    defaultValue = "id,asc") String[] sort,
            @RequestParam(name = "search", required = false) String search
    ){
        String[] sortParts = sort[0].split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Page<EventInvitationListResponse> invitations = eventInvitationService.getSavedInvitations(
                PageRequest.of(page, size, Sort.by(direction, sortParts[0])),
                search
        );

        return ResponseEntity.ok(CustomApiResponse.success(invitations));
    }

    @DeleteMapping("/invite/{id}")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<Object>> deleteInvitation(@PathVariable Long id){
        eventInvitationService.deleteEventInvitation(id);
        return ResponseEntity.ok(CustomApiResponse.success("Event invitation deleted successfully"));
    }

    @GetMapping("/invite/{id}")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<EventInvitationDetailsResponse>> getInvitationDetails(@PathVariable Long id){
        EventInvitationDetailsResponse details = eventInvitationService.getEventInvitationDetail(id);
        return ResponseEntity.ok(CustomApiResponse.success(details));
    }

    @GetMapping("/{eventId}/invitees")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
    public ResponseEntity<CustomApiResponse<Page<EventInviteeResponse>>> getEventInvitees(
            @PathVariable("eventId") Long eventId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(defaultValue = "",required = false) String keyword,
            @RequestParam(required = false)InviteeRole role,
            @RequestParam(required = false) LocalDate date
            ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CustomApiResponse
                                .success(eventInvitationService.getInviteeList(eventId,page,keyword,role,date))
                );
    }

}
