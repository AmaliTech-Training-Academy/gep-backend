package com.example.auth_service.controller;

import com.example.auth_service.dto.request.BulkUserInvitationRequest;
import com.example.auth_service.dto.request.InvitationAcceptanceRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.InviteeDetailsResponse;
import com.example.auth_service.service.UserInvitationService;
import com.example.common_libraries.dto.CustomApiResponse;
import com.example.common_libraries.dto.UserCreationResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-invitations")
public class UserInvitationController {

    private final UserInvitationService userInvitationService;

    @GetMapping("/{token}")
    public ResponseEntity<CustomApiResponse<Object>> getInvitationDetail(@PathVariable String token){
        InviteeDetailsResponse response = userInvitationService.inviteeDetails(token);
        return ResponseEntity.ok(CustomApiResponse.success("Invitation details", response));
    }

    @PostMapping("/accept-invitation")
    public ResponseEntity<CustomApiResponse<AuthResponse>> acceptInvitation(@Valid  @RequestBody InvitationAcceptanceRequest request, HttpServletResponse response){
        AuthResponse acceptanceResponse = userInvitationService.acceptInvitation(request,response);
        return ResponseEntity.ok(CustomApiResponse.success("Invitation accepted successfully", acceptanceResponse));
    }

    @PostMapping("/invite-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<UserCreationResponse>> inviteUser(@Valid @RequestBody BulkUserInvitationRequest invitationRequest){
        userInvitationService.inviteBulkUsers(invitationRequest);
        return ResponseEntity.ok(CustomApiResponse.success("Users invited successfully"));
    }
}
