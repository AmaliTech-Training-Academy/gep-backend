package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventRegistrationRequest;

public interface EventRegistrationService {
    String registerEvent(Long eventId, EventRegistrationRequest registrationRequest);
}
