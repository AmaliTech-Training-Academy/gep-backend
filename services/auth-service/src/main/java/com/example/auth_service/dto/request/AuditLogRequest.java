package com.example.auth_service.dto.request;

import com.example.auth_service.enums.AuditStatus;

import java.time.Instant;

public record AuditLogRequest(
        String email,
        String ipAddress,
        Instant timestamp,
        AuditStatus auditStatus
) {
}

