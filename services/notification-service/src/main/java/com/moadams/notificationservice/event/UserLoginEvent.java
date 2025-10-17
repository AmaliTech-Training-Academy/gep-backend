package com.moadams.notificationservice.event;

public record UserLoginEvent(
        String email,
        String otp
) {
}
