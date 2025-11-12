package com.example.auth_service.dto.request;

import com.example.common_libraries.enums.InvitationStatus;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkUserInvitationRequest(
        @NotNull(message = "Invitees list is required")
        List<UserInvitationRequest> invitees,
        String message,
        @NotNull(message ="Invitation status is required")
        InvitationStatus status

) {
}
