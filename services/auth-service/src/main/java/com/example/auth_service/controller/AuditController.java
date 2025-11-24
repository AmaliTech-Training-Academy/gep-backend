package com.example.auth_service.controller;

import com.example.auth_service.dto.response.EnrichedAuditResponse;
import com.example.auth_service.dto.response.PagedAuditResponse;
import com.example.auth_service.enums.AuditStatus;
import com.example.auth_service.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/audit_logs")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<PagedAuditResponse<EnrichedAuditResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) AuditStatus status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        return ResponseEntity.ok(auditService.getEnrichedAuditLogs(
                page, size, fullName, status, sortBy, direction
        ));
    }
}

