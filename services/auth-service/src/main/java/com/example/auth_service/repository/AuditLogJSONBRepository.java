package com.example.auth_service.repository;


import com.example.auth_service.model.AuditLogJSONB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogJSONBRepository
        extends JpaRepository<AuditLogJSONB, Long>, JpaSpecificationExecutor<AuditLogJSONB> {
}

