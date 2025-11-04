package com.example.auth_service.dto.request;

import com.example.auth_service.enums.UserRole;
import jakarta.validation.constraints.*;

public record InviteeAccountCreationRequest(
        @NotBlank(message = "Fullname is required")
        @Pattern(
                regexp = "^[\\p{L}]+([ '-][\\p{L}]+)*$",
                message = "Full name must start and end with a letter, and contain only letters, spaces, hyphens, or apostrophes"
        )
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
                message = "Password must contain at least one uppercase letter, one number, and one special character.")
        String password,

        @NotNull(message = "Role is required")
        UserRole role
) {
}
