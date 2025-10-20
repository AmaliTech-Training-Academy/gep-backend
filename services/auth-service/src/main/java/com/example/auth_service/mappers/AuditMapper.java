package com.example.auth_service.mappers;

import com.example.auth_service.dto.response.AuditResponse;
import com.example.auth_service.model.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditMapper {
    AuditResponse toAuditResponse(AuditLog auditLog);
}
