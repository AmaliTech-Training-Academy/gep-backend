package com.event_service.event_service.controllers;


import com.event_service.event_service.dto.TimeZoneResponse;
import com.event_service.event_service.utils.TimeZoneUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/timezones")
@RequiredArgsConstructor
public class TimeZoneController {
    private final TimeZoneUtils timeZoneUtils;

    @GetMapping
    public ResponseEntity<List<TimeZoneResponse>> getAllTimeZones() {
        return ResponseEntity.ok(timeZoneUtils.getAllTimeZones());
    }
}
