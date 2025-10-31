package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventRegistrationRequest;
import com.event_service.event_service.dto.EventRegistrationResponse;

public interface EventRegistrationService {
    EventRegistrationResponse registerEvent(Long eventId, EventRegistrationRequest registrationRequest);
}
