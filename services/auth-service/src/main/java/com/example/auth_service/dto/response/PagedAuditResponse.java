package com.example.auth_service.dto.response;

import java.util.List;

public record PagedAuditResponse<T>(
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        List<T> data
) {}