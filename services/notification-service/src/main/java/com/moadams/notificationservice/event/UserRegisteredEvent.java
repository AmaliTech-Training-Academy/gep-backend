package com.moadams.notificationservice.event;

public record UserRegisteredEvent(
        String email,
        String fullName
) {
}
