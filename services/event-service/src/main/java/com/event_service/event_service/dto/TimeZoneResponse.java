package com.event_service.event_service.dto;

public record TimeZoneResponse(
        String zoneId,
        String gmtOffset,
        String displayName
) {
}
