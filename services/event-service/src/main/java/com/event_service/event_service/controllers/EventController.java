package com.event_service.event_service.controllers;

import com.event_service.event_service.dto.*;
import com.event_service.event_service.services.EventDetailService;
import com.event_service.event_service.services.EventRegistrationService;
import com.event_service.event_service.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventDetailService eventDetailService;
    private final EventService eventService;
    private final EventRegistrationService eventRegistrationService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestPart("event") EventRequest eventRequest,
            @RequestPart(value = "image") MultipartFile image,
            @RequestPart(value = "eventImages", required = false) List<MultipartFile> eventImages
    ) {
        return ResponseEntity.ok(eventService.createEvent(eventRequest,image,eventImages));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponse> getEventById(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventDetailService.getEventDetailById(eventId));
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<EventRegistrationResponse> registerForEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRegistrationRequest eventRegistrationRequest){

        return ResponseEntity.status(HttpStatus.OK).body(eventRegistrationService.registerEvent(eventId,eventRegistrationRequest));
    }
}
