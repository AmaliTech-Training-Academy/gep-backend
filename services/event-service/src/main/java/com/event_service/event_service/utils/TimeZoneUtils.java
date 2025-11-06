package com.event_service.event_service.utils;

import com.event_service.event_service.dto.TimeZoneResponse;
import com.event_service.event_service.mappers.TimeZoneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TimeZoneUtils {

    private final TimeZoneMapper timeZoneMapper;

    @PreAuthorize("isAuthenticated()")
    public List<TimeZoneResponse> getAllTimeZones() {
        return timeZoneMapper.toResponseList(ZoneId.getAvailableZoneIds());
    }

    public ZoneId createZoneId(String Id) {
        return ZoneId.of(Id);
    }

    public ZonedDateTime createZonedTimeDate(LocalDate dateTime, LocalTime localTime, ZoneId zoneId) {
        return ZonedDateTime.of(dateTime,localTime,zoneId);
    }
}
