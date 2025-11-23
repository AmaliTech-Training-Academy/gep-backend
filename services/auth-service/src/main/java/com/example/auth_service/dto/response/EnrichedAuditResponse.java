package com.example.auth_service.dto.response;

import com.example.auth_service.enums.AuditStatus;
import lombok.Builder;

import java.time.Instant;


@Builder
public record EnrichedAuditResponse(
        Long id,
        String fullName,
        String email,
        String profileImageUrl,
        String ipAddress,
        Instant timestamp,
        Instant createdAt,
        Instant updatedAt,
        AuditStatus auditStatus
) {}