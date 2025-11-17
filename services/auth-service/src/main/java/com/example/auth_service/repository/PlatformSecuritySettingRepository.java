package com.example.auth_service.repository;

import com.example.auth_service.model.PlatformSecuritySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformSecuritySettingRepository extends JpaRepository<PlatformSecuritySetting, Long> {
}
