package com.event_service.event_service.controllers;

import com.event_service.event_service.dto.*;
import com.event_service.event_service.models.enums.EventStatus;
import com.event_service.event_service.services.*;
import com.example.common_libraries.dto.CustomApiResponse;
import com.example.common_libraries.dto.EventRegistrationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventDetailService eventDetailService;
    private final EventService eventService;
    private final MyEventService myEventService;
    private final EventRegistrationService eventRegistrationService;
    private final EventOverviewService eventOverviewService;
    private final ObjectMapper objectMapper;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> createEvent(
            @RequestPart("event") String eventRequestJSON,
            @RequestPart(value = "image") MultipartFile image,
            @RequestPart(value = "eventImages", required = false) List<MultipartFile> eventImages,
            @RequestPart(value = "sectionImages", required = false)
            List<MultipartFile> sectionImages
    ) throws JsonProcessingException {
        try {
            EventRequest eventRequest = objectMapper.readValue(eventRequestJSON, EventRequest.class);
            return ResponseEntity.ok(eventService.createEvent(eventRequest,image,eventImages,sectionImages));
        }catch (Exception e){
            log.error("Error parsing event request JSON {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping(value = "/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventUpdateResponse> updateEvent(
            @RequestPart("event") String eventRequestJSON,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "eventImages", required = false) List<MultipartFile> eventImages,
            @RequestPart(value = "imagesToUpdate", required = false) String imagesToUpdateJson,
            @PathVariable Long id
    ) throws JsonProcessingException {
        try {
            EventRequest eventRequest = objectMapper.readValue(eventRequestJSON, EventRequest.class);
            List<Long> imagesToUpdate = (imagesToUpdateJson != null && !imagesToUpdateJson.isEmpty())
                    ? objectMapper.readValue(imagesToUpdateJson, new TypeReference<List<Long>>() {})
                    : Collections.emptyList();
            return ResponseEntity.ok(eventService.updateEvent(id,eventRequest,image,eventImages,imagesToUpdate));
        }catch (Exception e){
            log.error("Error parsing event request JSON {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponse> getEventById(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventDetailService.getEventDetailById(eventId));
    }

    @GetMapping("/{eventId}/edit/details")
    @PreAuthorize("hasAnyRole('ORGANISER','CO_ORGANIZER')")
    public ResponseEntity<CustomApiResponse<EventEditPageResponse>> getEventForUpdate(@PathVariable Long eventId) {
        return ResponseEntity.ok().body(CustomApiResponse.success(eventDetailService.getEventEditPageById(eventId)));
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<EventRegistrationResponse> registerForEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRegistrationRequest eventRegistrationRequest){
        log.info("Registering for event with id: {}",eventId);
        return ResponseEntity.status(HttpStatus.OK).body(eventRegistrationService.registerEvent(eventId,eventRegistrationRequest));
    }

    @GetMapping("/{eventId}/registrations/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER','CO_ORGANIZER')")
    public ResponseEntity<CustomApiResponse<EventRegistrationPageResponse>> getEventRegistrationsOverview(@PathVariable("eventId") Long eventId) {
        return ResponseEntity.status(HttpStatus.OK).body(CustomApiResponse.success(eventRegistrationService.getEventRegistrationPageOverview(eventId)));
    }

    @GetMapping("/{eventId}/registrations/search")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANISER','CO_ORGANIZER')")
    public ResponseEntity<CustomApiResponse<Page<EventRegistrationsListResponse>>> getEventRegistrations(
            @PathVariable("eventId") Long eventId,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "keyword", defaultValue = "", required = false) String keyword,
            @RequestParam(value = "ticketType", defaultValue = "", required = false) String ticketType
    ){
        return ResponseEntity.status(HttpStatus.OK).body(CustomApiResponse.success(eventRegistrationService.getEventRegistrations(eventId,page, keyword,ticketType)));
    }

    @GetMapping("/explore")
    public ResponseEntity<PagedExploreEventResponse> getExploreEvents(
            @RequestParam(value = "sortBy", defaultValue = "location", required = false) String[] sortBy,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "5", required = false) int pageSize,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "hasTitle", required = false) String hasTitle,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "priceFilter", required = false) String priceFilter,
            @RequestParam(value = "past", required = false) Boolean past
    ) {
        return ResponseEntity.ok(
                eventService.listEvents(pageNumber, pageSize,hasTitle, sortBy, location, date, priceFilter, past)
        );
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

    @GetMapping("/my-events/overview")
    @PreAuthorize("hasAnyRole('ORGANISER','CO_ORGANIZER')")
    public ResponseEntity<CustomApiResponse<MyEventsOverviewResponse>> getMyEventsOverview() {
        return ResponseEntity.status(HttpStatus.OK).body(CustomApiResponse.success(myEventService.getMyEventsOverview()));
    }

    @GetMapping("/my-events/details/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANISER','CO_ORGANIZER')")
    public ResponseEntity<CustomApiResponse<MyEventDetailResponse>> getMyEventDetails(
            @CookieValue(name = "accessToken", required = false) String accessToken,
            @PathVariable Long eventId
    ){
        return ResponseEntity.status(HttpStatus.OK).body(CustomApiResponse.success(myEventService.getMyEventDetailsById(eventId, accessToken)));
    }

    @GetMapping("/my-events")
    @PreAuthorize("hasAnyRole('ORGANISER','CO_ORGANIZER')")
    public ResponseEntity<CustomApiResponse<Page<MyEventsListResponse>>> getMyEvents(
            @RequestParam(name = "page",
                    defaultValue = "0") int page
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(CustomApiResponse.success(myEventService.getMyEvents(page)));
    }
}
