package com.example.auth_service.dto.response;

public record UserListResponse(
        Long id,
        String fullName,
        String email,
        String role,
        Boolean status,
        String profileImageUrl
) {
}
