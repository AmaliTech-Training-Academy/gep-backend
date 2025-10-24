package com.example.auth_service.dto.response;

import com.example.auth_service.enums.UserRole;

public record AuthResponse(
        long id,
        String email,
        UserRole role
) {
}
