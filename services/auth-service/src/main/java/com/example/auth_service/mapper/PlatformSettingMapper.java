package com.example.auth_service.mapper;

import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.PlatformNotificationSettingDto;
import com.example.auth_service.dto.response.PlatformSecuritySettingResponse;
import com.example.auth_service.model.PlatformNotificationSetting;
import com.example.auth_service.model.PlatformSecuritySetting;
import com.example.auth_service.model.User;

public class PlatformSettingMapper {
    public static PlatformSecuritySettingResponse toSecuritySettingResponse(PlatformSecuritySetting securitySetting){
        return new PlatformSecuritySettingResponse(
                securitySetting.getPlatformName(),
                securitySetting.getPlatformUrl(),
                securitySetting.getContactEmail(),
                securitySetting.getPlatformDescription(),
                securitySetting.getMaintenanceMode()
        );
    }

    public static PlatformNotificationSettingDto toNotificationSettingDto(PlatformNotificationSetting notificationSetting){
        return new PlatformNotificationSettingDto(
                notificationSetting.getEventCreation(),
                notificationSetting.getPaymentFailures(),
                notificationSetting.getPlatformErrors()
        );
    }

    public static AuthResponse toAuthResponse(User user){
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getProfile().getProfileImageUrl(),
                user.getRole()
        );
    }
}
