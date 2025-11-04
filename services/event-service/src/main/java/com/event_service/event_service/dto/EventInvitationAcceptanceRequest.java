package com.event_service.event_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EventInvitationAcceptanceRequest(
        @NotBlank(message = "Invitation code is required.")
    String invitationCode,
    @NotBlank(message = "Full name is required.")
    @Pattern(
            regexp = "^[\\p{L}]+([ '-][\\p{L}]+)*$",
            message = "Full name must start and end with a letter, and contain only letters, spaces, hyphens, or apostrophes"
    )
    String fullName,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
                message = "Password must contain at least one uppercase letter, one number, and one special character.")
    String password
) {
}
