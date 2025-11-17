package com.example.auth_service.service;

import com.example.auth_service.dto.request.PlatformSecuritySettingRequest;
import com.example.auth_service.dto.response.PlatformNotificationSettingDto;
import com.example.auth_service.dto.response.PlatformSecuritySettingResponse;

public interface PlatformSettingService {
    PlatformSecuritySettingResponse getPlatformSecuritySettings();
    void updatePlatformSecuritySetting(PlatformSecuritySettingRequest request);
    PlatformNotificationSettingDto getPlatformNotificationSettings();
    void updatePlatformNotificationSetting(PlatformNotificationSettingDto request);
}
