package com.event_service.event_service.controllers;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.dto.EventResponse;
import com.event_service.event_service.exceptions.ValidationException;
import com.event_service.event_service.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestPart("event") EventRequest eventRequest,
            @RequestPart(value = "image") MultipartFile image,
            @RequestPart(value = "eventImages", required = false) List<MultipartFile> eventImages
    ) {
        return ResponseEntity.ok(eventService.createEvent(eventRequest,image,eventImages));
    }

}
