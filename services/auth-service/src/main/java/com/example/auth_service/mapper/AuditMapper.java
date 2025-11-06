package com.example.auth_service.mapper;

import com.example.auth_service.dto.response.AuditResponse;
import com.example.auth_service.model.AuditLogJSONB;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditMapper {
    @Mapping(target = "email", source = "auditLogDataJson.email")
    @Mapping(target = "ipAddress", source = "auditLogDataJson.ipAddress")
    @Mapping(target = "timestamp", source = "auditLogDataJson.timestamp")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    AuditResponse toAuditResponse(AuditLogJSONB auditLog);
}
