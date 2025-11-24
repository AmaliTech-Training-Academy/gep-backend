package com.example.auth_service.mapper;

import com.example.auth_service.dto.response.EnrichedAuditResponse;
import com.example.auth_service.model.AuditLogJSONB;
import com.example.auth_service.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    private final UserRepository userRepository;

    public AuditMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public EnrichedAuditResponse toEnrichedAuditResponse(AuditLogJSONB auditLog) {
        var data = auditLog.getAuditLogDataJson();

        EnrichedAuditResponse.EnrichedAuditResponseBuilder builder = EnrichedAuditResponse.builder()
                .id(auditLog.getId())
                .email(data.getEmail())
                .ipAddress(data.getIpAddress())
                .timestamp(data.getTimestamp())
                .createdAt(auditLog.getCreatedAt())
                .updatedAt(auditLog.getUpdatedAt())
                .auditStatus(data.getAuditStatus());

        var email = data.getEmail();
        userRepository.findByEmail(email).ifPresent(user -> {
            builder.fullName(user.getFullName());
            if (user.getProfile() != null) {
                builder.profileImageUrl(user.getProfile().getProfileImageUrl());
            }
        });

        return builder.build();
    }
}
