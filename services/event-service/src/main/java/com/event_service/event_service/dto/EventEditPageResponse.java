package com.event_service.event_service.dto;

import com.event_service.event_service.models.EventMeetingType;
import com.event_service.event_service.models.EventOptions;
import com.event_service.event_service.models.EventType;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record EventEditPageResponse(
      Long eventId,
      String title,
      String description,
      String location,
      String flyerUrl,
      String zoomMeetingUrl,
      Instant startTime,
      Instant endTime,
      Instant eventTime,
      EventMeetingTypeResponse eventMeetingType,
      EventTypeResponse eventType,
      List<TicketTypeResponse> ticketTypes
){}
