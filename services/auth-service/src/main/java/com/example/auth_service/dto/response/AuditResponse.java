package com.example.auth_service.dto.response;

import java.time.Instant;

public record AuditResponse(
        String id,
        String email,
        String ipAddress,
        Instant timestamp,
        Instant createdAt,
        Instant updatedAt
) {
}
