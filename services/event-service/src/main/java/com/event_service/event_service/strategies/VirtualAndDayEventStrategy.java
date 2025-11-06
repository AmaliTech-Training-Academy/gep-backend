package com.event_service.event_service.strategies;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.models.*;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.services.S3Service;
import com.event_service.event_service.utils.TimeZoneUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VirtualAndDayEventStrategy implements EventStrategy {

    private final EventRepository eventRepository;
    private final S3Service s3Service;
    private final TimeZoneUtils timeZoneUtils;


    @Override
    public Event createEvent(EventRequest eventRequest,
                             MultipartFile image,
                             List<MultipartFile> eventImages,
                             EventType eventType,
                             EventMeetingType eventMeetingType
    ) {
        EventOptions eventOptions = EventOptions.builder()
                        .ticketPrice(eventRequest.eventOptionsRequest().ticketPrice())
                        .capacity(eventRequest.eventOptionsRequest().capacity())
                        .requiresApproval(eventRequest.eventOptionsRequest().requiresApproval())
                        .build();
        ZoneId zoneId = timeZoneUtils.createZoneId(eventRequest.event_time_zone_id());
        ZonedDateTime zonedDateTime = timeZoneUtils.createZonedTimeDate(
                eventRequest.event_date(),
                eventRequest.event_time(),
                zoneId
        );
        Instant eventInstant = zonedDateTime.toInstant();
                String uploadedFlyer = s3Service.uploadImage(image);
                Event event = Event.builder().eventOptions(eventOptions)
                        .eventMeetingType(eventMeetingType).eventType(eventType)
                        .eventType(eventType)
                        .title(eventRequest.title())
                        .description(eventRequest.description())
                        .eventTime(eventInstant)
                        .eventTimeZoneId(eventRequest.event_time_zone_id())
                        .flyerUrl(uploadedFlyer)
                        .zoomMeetingLink(eventRequest.zoomUrl())
                        .build();
                event.setEventOptions(eventOptions);
                return eventRepository.save(event);
    }

}
