package com.event_service.event_service.dto;

import com.event_service.event_service.models.enums.InvitationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EventInvitationRequest(
        @NotBlank(message = "Invitation title is required.")
        String invitationTitle,
        @NotNull(message = "Event ID is required.")
        Long event,
        @NotNull(message = "Invitees list cannot be null.")
        List<InviteeRequest> invitees,
        String message,
        @NotNull(message = "Invitation Status is required")
        InvitationStatus status
) {
}
