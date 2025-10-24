package com.example.auth_service.dto.response;

import com.example.auth_service.enums.UserRole;

public record AuthResponse(
        String email,
        UserRole role
) {
}
