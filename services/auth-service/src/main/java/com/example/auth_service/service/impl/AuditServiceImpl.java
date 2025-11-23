package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.AuditLogData;
import com.example.auth_service.dto.request.AuditLogRequest;
import com.example.auth_service.dto.response.AuditResponse;
import com.example.auth_service.dto.response.EnrichedAuditResponse;
import com.example.auth_service.dto.response.PagedAuditResponse;
import com.example.auth_service.mapper.AuditMapper;
import com.example.auth_service.model.AuditLogJSONB;
import com.example.auth_service.repository.AuditLogJSONBRepository;
import com.example.auth_service.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    private final AuditLogJSONBRepository auditLogJSONBRepository;
    private final AuditMapper auditMapper;

    @Override
    public void save(AuditLogRequest auditLogRequest) {
        AuditLogData auditLogData = new AuditLogData(
                auditLogRequest.email(),
                auditLogRequest.ipAddress(),
                auditLogRequest.timestamp(),
                auditLogRequest.auditStatus()
        );
        AuditLogJSONB auditLogJSONB = AuditLogJSONB
                .builder()
                .auditLogDataJson(auditLogData)
                .build();
        auditLogJSONBRepository.save(auditLogJSONB);
    }

    @Override
    public PagedAuditResponse<EnrichedAuditResponse> getEnrichedAuditLogs(Pageable pageable) {

        Page<AuditLogJSONB> page = auditLogJSONBRepository.findAll(pageable);

        List<EnrichedAuditResponse> enrichedList = page.getContent().stream()
                .map(auditMapper::toEnrichedAuditResponse)
                .toList();

        return new PagedAuditResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                enrichedList
        );
    }
}
