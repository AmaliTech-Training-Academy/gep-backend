package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventInvitationRequest;
import com.event_service.event_service.models.AppUser;

public interface EventInvitationService {
    void sendEventInvitation(EventInvitationRequest request);
}
