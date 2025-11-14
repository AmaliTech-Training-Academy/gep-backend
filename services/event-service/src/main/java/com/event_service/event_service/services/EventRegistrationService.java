package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventRegistrationPageResponse;
import com.event_service.event_service.dto.EventRegistrationRequest;
import com.event_service.event_service.dto.EventRegistrationResponse;
import com.event_service.event_service.dto.EventRegistrationsListResponse;
import org.springframework.data.domain.Page;

public interface EventRegistrationService {
    EventRegistrationResponse registerEvent(Long eventId, EventRegistrationRequest registrationRequest);
    Page<EventRegistrationsListResponse> getEventRegistrations(Long eventId, int page, String keyword, String ticketType);
    EventRegistrationPageResponse getEventRegistrationPageOverview(Long eventId);
}
