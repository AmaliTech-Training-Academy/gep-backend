package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.AuditLogRequest;
import com.example.auth_service.dto.response.AuditResponse;
import com.example.auth_service.dto.response.PagedAuditResponse;
import com.example.auth_service.mappers.AuditMapper;
import com.example.auth_service.model.AuditLog;
import com.example.auth_service.repository.AuditLogRepository;
import com.example.auth_service.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditMapper auditMapper;

    @Override
    public void save(AuditLogRequest auditLogRequest) {
        AuditLog auditLog = AuditLog
                .builder()
                .email(auditLogRequest.email())
                .ipAddress(auditLogRequest.ipAddress())
                .timestamp(auditLogRequest.timestamp())
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PagedAuditResponse findAll(int pageNumber, int pageSize, String[] sortBy) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        List<AuditResponse> auditResponseList = auditLogRepository.findAll(pageable).stream()
                .map(auditMapper::toAuditResponse)
                .collect(Collectors.toList());
        return  new PagedAuditResponse(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                auditResponseList
        );
    }
}
