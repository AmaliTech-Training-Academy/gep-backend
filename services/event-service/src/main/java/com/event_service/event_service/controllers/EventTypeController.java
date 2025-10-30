package com.event_service.event_service.controllers;


import com.event_service.event_service.dto.EventMeetingTypeResponse;
import com.event_service.event_service.dto.EventTypeRequest;
import com.event_service.event_service.dto.EventTypeResponse;
import com.event_service.event_service.services.EventTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/event_types")
@RequiredArgsConstructor
public class EventTypeController {

    private final EventTypeService eventTypeService;

    @GetMapping
    public ResponseEntity<List<EventTypeResponse>> getEventMeetingTypes() {
        return ResponseEntity.ok(eventTypeService.findAll());
    }

    @PostMapping
    public ResponseEntity<Void> createEventType(@Valid @RequestBody EventTypeRequest eventTypeRequest) {
        eventTypeService.save(eventTypeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void>  updateEventType(@PathVariable Long id, @Valid @RequestBody EventTypeRequest eventTypeRequest) {
        eventTypeService.update(id, eventTypeRequest);
        return ResponseEntity.noContent().build();
    }
}
