package com.example.auth_service.repository;

import com.example.auth_service.model.PlatformNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformNotificationSettingRepository extends JpaRepository<PlatformNotificationSetting, Long> {
}
