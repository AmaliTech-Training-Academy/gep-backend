package com.example.auth_service.model;


import com.example.auth_service.dto.request.AuditLogData;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;


@Setter
@Getter
@Builder
@NoArgsConstructor
@Entity
@Table(name = "audit_logs_jsonb")
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuditLogJSONB {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private AuditLogData auditLogDataJson;

    @CreatedDate
    @Column(name = "created_at", nullable =false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant  updatedAt;
}
