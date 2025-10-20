package com.example.auth_service.dto.request;

import java.time.Instant;

public record AuditLogRequest(
        String email,
        String ipAddress,
        Instant timestamp
) {
}

