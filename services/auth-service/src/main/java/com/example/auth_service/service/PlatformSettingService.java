package com.example.auth_service.service;

import com.example.auth_service.dto.request.PlatformSecuritySettingRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.PlatformSecuritySettingResponse;
import com.example.common_libraries.dto.PlatformNotificationSettingDto;

import java.util.List;

public interface PlatformSettingService {
    PlatformSecuritySettingResponse getPlatformSecuritySettings();
    void updatePlatformSecuritySetting(PlatformSecuritySettingRequest request);
    PlatformNotificationSettingDto getPlatformNotificationSettings();
    void updatePlatformNotificationSetting(PlatformNotificationSettingDto request);
    List<AuthResponse> getTeamMembers();
}
