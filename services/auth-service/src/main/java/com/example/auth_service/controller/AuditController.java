package com.example.auth_service.controller;

import com.example.auth_service.dto.response.PagedAuditResponse;
import com.example.auth_service.service.AuditService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<PagedAuditResponse> getAuditLogs(
            @RequestParam(value = "sortBy", defaultValue = "createdAt",required = false) String[] sortBy,
            @RequestParam(value = "pageNumber",defaultValue = "0", required = false) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "5", required = false) int pageSize
    ) {
        return ResponseEntity.ok(auditService.findAll(pageNumber, pageSize,sortBy));
    }
}

