package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.AuditLogData;
import com.example.auth_service.dto.request.AuditLogRequest;
import com.example.auth_service.dto.response.EnrichedAuditResponse;
import com.example.auth_service.dto.response.PagedAuditResponse;
import com.example.auth_service.enums.AuditStatus;
import com.example.auth_service.mapper.AuditMapper;
import com.example.auth_service.model.AuditLogJSONB;
import com.example.auth_service.repository.AuditLogJSONBRepository;
import com.example.auth_service.service.AuditService;
import com.example.auth_service.specifications.AuditLogSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public PagedAuditResponse<EnrichedAuditResponse> getEnrichedAuditLogs(
            Integer page,
            Integer size,
            String fullName,
            AuditStatus status,
            String sortBy,
            String direction
    ) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<AuditLogJSONB> spec = Specification.unrestricted();
        spec = spec.and(AuditLogSpecification.hasFullName(fullName));
        spec = spec.and(AuditLogSpecification.hasStatus(status));

        Page<AuditLogJSONB> page = auditLogJSONBRepository.findAll(pageable);

        Page<AuditLogJSONB> auditPage =
                auditLogJSONBRepository.findAll(spec, pageable);

        List<EnrichedAuditResponse> enrichedList = auditPage.getContent().stream()
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
