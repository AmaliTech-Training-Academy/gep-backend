package com.example.auth_service.dto.request;

import com.example.auth_service.dto.base.UserRegistrationBase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InvitationAcceptanceRequest(
        @NotBlank(message = "InvitationToken is required")
        String invitationToken,
        @Pattern(
                regexp = "^[\\p{L}]+([ '-][\\p{L}]+)*$",
                message = "Full name must start and end with a letter, and contain only letters, spaces, hyphens, or apostrophes"
        )
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
                message = "Password must contain at least one uppercase letter, one number, and one special character.")
        String password,
        @NotBlank(message = "Confirm Password is required")
        String confirmPassword
) implements UserRegistrationBase {
        @Override
        public String email() {
                return null;
        }
}
