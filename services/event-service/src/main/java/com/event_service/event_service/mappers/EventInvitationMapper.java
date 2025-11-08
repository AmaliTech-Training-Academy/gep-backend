package com.event_service.event_service.mappers;

import com.event_service.event_service.dto.EventInvitationDetailsResponse;
import com.event_service.event_service.dto.EventInvitationListResponse;
import com.event_service.event_service.dto.EventInviteeResponse;
import com.event_service.event_service.models.EventInvitation;
import com.event_service.event_service.models.EventInvitee;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventInvitationMapper {

    public EventInvitationListResponse toEventInvitationList(EventInvitation eventInvitation){

        return new EventInvitationListResponse(
                eventInvitation.getId(),
                eventInvitation.getInvitationTitle(),
                eventInvitation.getEvent().getId(),
                eventInvitation.getEvent().getTitle(),
                eventInvitation.getInviterName(),
                eventInvitation.getUpdatedAt().toLocalDate(),
                eventInvitation.getInvitees().size()
        );
    }

    public EventInvitationDetailsResponse mapToDetailsResponse(EventInvitation invitation){
        List<EventInviteeResponse> inviteeResponses = invitation.getInvitees().stream()
                .map(this::mapToInviteeResponse)
                .toList();


        return new EventInvitationDetailsResponse(
                invitation.getId(),
                invitation.getInvitationTitle(),
                invitation.getEvent().getId(),
                invitation.getMessage(),
                invitation.getStatus(),
                inviteeResponses
        );
    }

    private EventInviteeResponse mapToInviteeResponse(EventInvitee invitee){
        return new EventInviteeResponse(
                invitee.getId(),
                invitee.getInviteeName(),
                invitee.getInviteeEmail(),
                invitee.getRole()
        );
    }
}
