package com.example.auth_service.dto.response;

import java.util.List;

public record PagedAuditResponse(
        Integer pageNumber,
        Integer pageSize,
        List<AuditResponse> auditListResponse
) {
}