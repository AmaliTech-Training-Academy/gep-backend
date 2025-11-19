package com.example.common_libraries.dto.queue_events;

public record EventCreationNotificationMessage(
        String adminEmail,
        String createdBy,
        String eventTitle
) {
}
