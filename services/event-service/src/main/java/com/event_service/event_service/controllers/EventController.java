package com.event_service.event_service.controllers;

import com.event_service.event_service.dto.*;
import com.event_service.event_service.models.enums.EventStatus;
import com.event_service.event_service.services.EventDetailService;
import com.event_service.event_service.services.EventOverviewService;
import com.event_service.event_service.services.EventRegistrationService;
import com.event_service.event_service.services.EventService;
import com.example.common_libraries.dto.CustomApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventDetailService eventDetailService;
    private final EventService eventService;
    private final EventRegistrationService eventRegistrationService;
    private final EventOverviewService eventOverviewService;

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
        log.info("Registering for event with id: {}",eventId);
        return ResponseEntity.status(HttpStatus.OK).body(eventRegistrationService.registerEvent(eventId,eventRegistrationRequest));
    }

    @GetMapping("/event-management")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<EventDashboardResponse>> getEventManagementDashboard(
            @CookieValue(name = "accessToken", required = false) String accessToken
    ){
        return ResponseEntity.status(HttpStatus.OK).body(CustomApiResponse.success(eventOverviewService.getEventOverview(accessToken)));
    }

    @GetMapping("/event-management/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<Page<EventManagementResponse>>> searchEvents(
            @RequestParam(name = "page",
                    defaultValue = "0") int page,
            @RequestParam(name = "status", required = false) EventStatus status,
            @RequestParam(name = "keyword", required = false) String keyword
    ){
        return ResponseEntity.status(HttpStatus.OK).body(CustomApiResponse.success(eventOverviewService.getManagementEvents(keyword,page,status)));
    }
}
