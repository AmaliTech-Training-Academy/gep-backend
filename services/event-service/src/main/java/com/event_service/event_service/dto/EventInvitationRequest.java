package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.InviteeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EventInvitationRequest(
        @NotBlank(message = "Invitation title is required.")
        String invitationTitle,
        @NotBlank(message = "Invitee name is required.")
        @Pattern(
                regexp = "^[\\p{L}]+([ '-][\\p{L}]+)*$",
                message = "Name must start and end with a letter, and contain only letters, spaces, hyphens, or apostrophes"
        )
        String inviteeName,
        @NotBlank(message = "Invitee email is required.")
        @Email(message = "Invalid email address.")
        String inviteeEmail,
        @NotNull(message = "Role is required.")
        InviteeRole role,
        @NotNull(message = "Event ID is required.")
        Long event,
        String message
) {
}
