package com.example.auth_service.specifications;

import com.example.auth_service.enums.AuditStatus;
import com.example.auth_service.model.AuditLogJSONB;
import org.springframework.data.jpa.domain.Specification;

public class AuditLogSpecification {

    private AuditLogSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<AuditLogJSONB> hasFullName(String fullName) {
        return (root, query, cb) -> {
            if (fullName == null || fullName.isBlank()) {
                return cb.conjunction();
            }
            String likePattern = "%" + fullName.toLowerCase() + "%";
            return cb.like(
                    cb.lower(cb.function("jsonb_extract_path_text", String.class,
                            root.get("auditLogDataJson"), cb.literal("email"))),
                    likePattern
            );
        };
    }

    public static Specification<AuditLogJSONB> hasStatus(AuditStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(
                    cb.function("jsonb_extract_path_text", String.class,
                            root.get("auditLogDataJson"), cb.literal("auditStatus")),
                    status.name()
            );
        };
    }
}