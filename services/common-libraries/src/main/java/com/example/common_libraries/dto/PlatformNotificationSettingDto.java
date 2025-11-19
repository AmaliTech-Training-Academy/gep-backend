package com.example.common_libraries.dto;

public record PlatformNotificationSettingDto(
        Boolean eventCreation,
        Boolean paymentFailures,
        Boolean platformErrors
) {
}