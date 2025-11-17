package com.example.auth_service.mapper;

import com.example.auth_service.dto.response.PlatformSecuritySettingResponse;
import com.example.auth_service.model.PlatformSecuritySetting;

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
}
