package com.event_service.event_service.dto;

public record InviteeRegistrationRequest(
        String fullName,
        String email,
        String password,
        String role
) {
}
