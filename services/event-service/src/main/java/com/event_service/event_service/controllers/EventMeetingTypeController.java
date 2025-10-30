package com.event_service.event_service.controllers;


import com.event_service.event_service.dto.EventMeetingTypeRequest;
import com.event_service.event_service.dto.EventMeetingTypeResponse;
import com.event_service.event_service.services.EventMeetingTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/event_meeting_types")
@RequiredArgsConstructor
public class EventMeetingTypeController {

    private final EventMeetingTypeService eventMeetingTypeService;

    @GetMapping
    public ResponseEntity<List<EventMeetingTypeResponse>> getEventMeetingTypes() {
        return ResponseEntity.ok(eventMeetingTypeService.findAllEventMeetingTypes());
    }

    @PostMapping
    public ResponseEntity<Void> createEventMeetingType(@Valid @RequestBody EventMeetingTypeRequest eventMeetingTypeRequest) {
        eventMeetingTypeService.createEventMeetingType(eventMeetingTypeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateEventMeetingType( @PathVariable Long id,@Valid @RequestBody EventMeetingTypeRequest eventMeetingTypeRequest) {
        eventMeetingTypeService.updateEventMeetingType(id, eventMeetingTypeRequest);
        return ResponseEntity.noContent().build();
    }
}
