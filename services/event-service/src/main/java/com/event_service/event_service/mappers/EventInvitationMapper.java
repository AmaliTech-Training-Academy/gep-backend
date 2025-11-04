package com.event_service.event_service.mappers;

import com.event_service.event_service.dto.EventInvitationListResponse;
import com.event_service.event_service.models.EventInvitation;
import org.springframework.stereotype.Component;

@Component
public class EventInvitationMapper {

    public EventInvitationListResponse toEventInvitationList(EventInvitation eventInvitation){
        return new EventInvitationListResponse(
                eventInvitation.getId(),
                eventInvitation.getInvitationTitle(),
                eventInvitation.getInviteeName(),
                eventInvitation.getInvitationToken()
        );
    }
}
