package com.example.auth_service.dto.response;

public record InviteeDetailsResponse(
        String fullName,
        String email,
        String invitationToken
) {
}
