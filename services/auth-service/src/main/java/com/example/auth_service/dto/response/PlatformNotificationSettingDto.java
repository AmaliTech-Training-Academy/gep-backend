package com.example.auth_service.dto.response;

public record PlatformNotificationSettingDto(
    Boolean eventCreation,
    Boolean paymentFailures,
    Boolean platformErrors
) {
}
