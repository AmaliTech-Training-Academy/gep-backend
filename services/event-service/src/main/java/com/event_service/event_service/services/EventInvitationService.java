package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventInvitationAcceptanceRequest;
import com.event_service.event_service.dto.EventInvitationDetailsResponse;
import com.event_service.event_service.dto.EventInvitationListResponse;
import com.event_service.event_service.dto.EventInvitationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface EventInvitationService {
    void sendEventInvitation(EventInvitationRequest request);
    void deleteEventInvitation(Long invitationId);
    void updateEventInvitation(Long invitationId, EventInvitationRequest request);
    EventInvitationDetailsResponse getEventInvitationDetail(Long invitationId);
    void acceptInvitation(EventInvitationAcceptanceRequest acceptanceRequest);
    void acceptInvitationForExistingUser(String token);
    void resendInvitation(Long invitationId);
    Page<EventInvitationListResponse> getInvitationList(Pageable pageable, String search);
    Page<EventInvitationListResponse> getSavedInvitations(Pageable pageable, String search);
}
