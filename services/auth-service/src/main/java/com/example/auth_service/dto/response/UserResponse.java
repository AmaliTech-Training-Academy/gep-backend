package com.example.auth_service.dto.response;

import lombok.Builder;

@Builder
public record UserResponse(
        long userId,
        String fullName,
        String email,
        String phone,
        String address,
        String profileImageUrl,
        boolean status
) {
}
