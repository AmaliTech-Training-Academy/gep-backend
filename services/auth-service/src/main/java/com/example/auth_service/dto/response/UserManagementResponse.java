package com.example.auth_service.dto.response;

import lombok.Builder;

@Builder
public record UserManagementResponse(
    Long userId,
    String fullName,
    String email,
    String role,
    boolean status,
    String profileImageUrl,
    int eventsOrganized,
    int eventsAttended
){}
