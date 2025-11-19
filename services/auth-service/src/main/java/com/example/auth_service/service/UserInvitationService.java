package com.example.auth_service.service;

import com.example.auth_service.dto.request.BulkUserInvitationRequest;
import com.example.auth_service.dto.request.InvitationAcceptanceRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.InviteeDetailsResponse;
import com.example.common_libraries.dto.UserCreationResponse;

public interface UserInvitationService {
    void inviteBulkUsers(BulkUserInvitationRequest invitationRequest);
    InviteeDetailsResponse inviteeDetails(String token);
    AuthResponse acceptInvitation(InvitationAcceptanceRequest request);
}
