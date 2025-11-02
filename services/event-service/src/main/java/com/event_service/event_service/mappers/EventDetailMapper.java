package com.event_service.event_service.mappers;

import com.event_service.event_service.dto.EventDetailResponse;
import com.event_service.event_service.dto.TicketEventDetailResponse;
import com.event_service.event_service.dto.TicketTypeResponse;
import com.event_service.event_service.models.Event;

import java.util.List;

public class EventDetailMapper {
    private EventDetailMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static EventDetailResponse toEventDetailResponse(Event event, List<String> eventImagesUrl, List<TicketTypeResponse> ticketTypeResponses, Long capacity) {
        return EventDetailResponse
                .builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .flyerUrl(event.getFlyerUrl())
                .capacity(capacity)
                .eventImagesUrl(eventImagesUrl)
                .ticketTypes(ticketTypeResponses)
                .startTime(event.getStartTime())
                .build();
    }

    public static TicketEventDetailResponse toTicketEventDetails(Event event) {
        return TicketEventDetailResponse
                .builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .eventMeetingType(event.getEventMeetingType().getName().toString())
                .zoneId(event.getStartTimeZoneId())
                .build();
    }
}
