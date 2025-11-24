package com.example.auth_service.service;

import com.example.auth_service.dto.request.AuditLogRequest;
import com.example.auth_service.dto.response.EnrichedAuditResponse;
import com.example.auth_service.dto.response.PagedAuditResponse;
import com.example.auth_service.enums.AuditStatus;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void save(AuditLogRequest auditLogRequest);
    PagedAuditResponse<EnrichedAuditResponse> getEnrichedAuditLogs(
            Integer page,
            Integer size,
            String fullName,
            AuditStatus status,
            String sortBy,
            String direction
            );

}
