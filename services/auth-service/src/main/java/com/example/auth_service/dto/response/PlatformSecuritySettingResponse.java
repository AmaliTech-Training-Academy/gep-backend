package com.example.auth_service.dto.response;

public record PlatformSecuritySettingResponse(
    String platformName,
    String platformUrl,
    String contactEmail,
    String platformDescription,
    Boolean maintenanceMode
) {
}
