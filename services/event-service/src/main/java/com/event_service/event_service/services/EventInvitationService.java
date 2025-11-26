package com.event_service.event_service.services;

import com.event_service.event_service.dto.*;
import com.event_service.event_service.models.enums.InviteeRole;
import com.example.common_libraries.dto.UserCreationResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;


public interface EventInvitationService {
    void sendEventInvitation(EventInvitationRequest request);
    void deleteEventInvitation(Long invitationId);
    void updateEventInvitation(Long invitationId, EventInvitationRequest request);
    EventInvitationDetailsResponse getEventInvitationDetail(Long invitationId);
    UserCreationResponse acceptInvitation(EventInvitationAcceptanceRequest acceptanceRequest, HttpServletResponse response);
    UserCreationResponse acceptInvitationForExistingUser(String token,  HttpServletResponse response);
    void resendInvitation(Long invitationId);
    Page<EventInvitationListResponse> getInvitationList(Pageable pageable, String search);
    Page<EventInvitationListResponse> getSavedInvitations(Pageable pageable, String search);
    Page<EventInviteeResponse> getInviteeList(Long eventId, int page, String keyword, InviteeRole role, LocalDate date);
}
