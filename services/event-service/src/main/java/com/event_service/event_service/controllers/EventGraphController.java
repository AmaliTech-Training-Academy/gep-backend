package com.event_service.event_service.controllers;

import com.event_service.event_service.dto.GraphResponse;
import com.event_service.event_service.services.EventGraphService;
import com.example.common_libraries.dto.CustomApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/v1/event-graph")
@RestController
@RequiredArgsConstructor
public class EventGraphController {
    private final EventGraphService eventGraphService;

    @GetMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<GraphResponse>> getEventGraphData(){
        return ResponseEntity.ok(CustomApiResponse.success(eventGraphService.getEventGraphData()));
    }

    @GetMapping("/registrations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<GraphResponse>> getRegistrationGraphData(){
        return ResponseEntity.ok(CustomApiResponse.success(eventGraphService.getRegistrationGraphData()));
    }
}
