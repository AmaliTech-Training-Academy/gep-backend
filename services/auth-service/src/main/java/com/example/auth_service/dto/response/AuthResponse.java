package com.example.auth_service.dto.response;

import com.example.auth_service.enums.UserRole;

public record AuthResponse(
        long id,
        String email,
        String fullName,
        String profilePicture,
        UserRole role
) {
}
