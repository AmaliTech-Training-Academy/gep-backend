package com.event_service.event_service.mappers;

import com.event_service.event_service.dto.EventInvitationListResponse;
import com.event_service.event_service.models.EventInvitation;
import com.event_service.event_service.models.EventInvitee;
import org.springframework.stereotype.Component;

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
}
