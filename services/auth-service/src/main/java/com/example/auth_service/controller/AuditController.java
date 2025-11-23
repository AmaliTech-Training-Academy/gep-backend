package com.example.auth_service.controller;

import com.example.auth_service.dto.response.EnrichedAuditResponse;
import com.example.auth_service.dto.response.PagedAuditResponse;
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
    public PagedAuditResponse<EnrichedAuditResponse> getAuditLogs(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return auditService.getEnrichedAuditLogs(pageable);
    }
}

