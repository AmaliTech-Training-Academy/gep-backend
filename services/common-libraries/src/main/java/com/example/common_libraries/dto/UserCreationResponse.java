package com.example.common_libraries.dto;

public record UserCreationResponse(
        Long id,
        String fullName,
        String role,
        String email,
        String profilePicture
) {
}
