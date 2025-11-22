package com.event_service.event_service.mappers;

import com.event_service.event_service.dto.EventDetailResponse;
import com.event_service.event_service.dto.TicketTypeResponse;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.TicketType;
import com.example.common_libraries.dto.TicketEventDetailResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventDetailMapper {
    // Mapper is annotated as a spring bean, so it can be used in sqs listener without throwing errors

    public EventDetailResponse toEventDetailResponse(Event event, List<String> eventImagesUrl, List<TicketTypeResponse> ticketTypeResponses, Long capacity) {
        return EventDetailResponse
                .builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .flyerUrl(event.getFlyerUrl())
                .capacity(capacity)
                .location(event.getLocation())
                .isPaid(event.getTicketTypes().stream().anyMatch(TicketType::getIsPaid))
                .eventImagesUrl(eventImagesUrl)
                .totalAttendees((long) event.getEventRegistrations().size())
                .ticketTypes(ticketTypeResponses)
                .startTime(event.getStartTime())
                .build();
    }

    public TicketEventDetailResponse toTicketEventDetails(Event event) {
        return TicketEventDetailResponse
                .builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .organizerName(event.getCreatedBy())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .eventMeetingType(event.getEventMeetingType().getName().toString())
                .zoneId(event.getStartTimeZoneId())
                .build();
    }
}
