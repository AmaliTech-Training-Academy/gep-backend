package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventInvitationAcceptanceRequest;
import com.event_service.event_service.dto.EventInvitationDetailsResponse;
import com.event_service.event_service.dto.EventInvitationListResponse;
import com.event_service.event_service.dto.EventInvitationRequest;
import com.event_service.event_service.models.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventInvitationService {
    void sendEventInvitation(EventInvitationRequest request);
    void deleteEventInvitation(Long invitationId);
    void updateEventInvitation(Long invitationId, EventInvitationRequest request);
    EventInvitationDetailsResponse getEventInvitationDetail(Long invitationId);
    void acceptInvitation(EventInvitationAcceptanceRequest acceptanceRequest);
    void resendInvitation(Long invitationId);
    Page<EventInvitationListResponse> getInvitationList(Pageable pageable);
    Page<EventInvitationListResponse> getSavedInvitations(Pageable pageable);
}
