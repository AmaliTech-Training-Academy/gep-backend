package com.example.auth_service.service;

import com.example.auth_service.dto.request.AuditLogRequest;
import com.example.auth_service.dto.response.PagedAuditResponse;
import com.example.auth_service.model.AuditLog;

import java.util.List;

public interface AuditService {
    void save(AuditLogRequest auditLogRequest);
    PagedAuditResponse findAll(int pageNumber, int pageSize, String[] sortBy);
}
