package com.example.auth_service.service;

import com.example.auth_service.dto.request.AuditLogRequest;
import com.example.auth_service.dto.response.EnrichedAuditResponse;
import com.example.auth_service.dto.response.PagedAuditResponse;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void save(AuditLogRequest auditLogRequest);
    PagedAuditResponse<EnrichedAuditResponse> getEnrichedAuditLogs(Pageable pageable);

}
