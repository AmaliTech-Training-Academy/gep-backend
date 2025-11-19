package com.example.common_libraries.dto;

public record UserInfoResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}
