package com.event_service.event_service.strategies.manage;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventMeetingType;
import com.event_service.event_service.models.EventOptions;
import com.event_service.event_service.models.EventType;
import com.event_service.event_service.utils.TimeZoneUtils;
import com.example.common_libraries.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class VirtualAndDayEventUpdateStrategy implements UpdateEventStrategy {
    private final S3Service s3Service;
    private final TimeZoneUtils timeZoneUtils;

    @Override
    public Event updateEvent(Long id, EventRequest eventRequest, MultipartFile image, List<MultipartFile> newEventImages, List<Long> imagesToRemove, Event event, EventType eventType, EventMeetingType eventMeetingType) {


        if (image != null && !image.isEmpty()) {
            String uploadedFlyer = s3Service.uploadImage(image);
            event.setFlyerUrl(uploadedFlyer);
        }

        EventOptions options = event.getEventOptions();
        options.setCapacity(eventRequest.eventOptionsRequest().capacity());
        options.setTicketPrice(eventRequest.eventOptionsRequest().ticketPrice());
        options.setRequiresApproval(eventRequest.eventOptionsRequest().requiresApproval());


        ZoneId zoneId = timeZoneUtils.createZoneId(eventRequest.event_time_zone_id());
        ZonedDateTime zonedDateTime = timeZoneUtils.createZonedTimeDate(
                eventRequest.event_date(),
                eventRequest.event_time(),
                zoneId
        );
        Instant eventInstant = zonedDateTime.toInstant();
        event.setEventTime(eventInstant);
        event.setEventTimeZoneId(eventRequest.event_time_zone_id());

        event.setEventType(eventType);
        event.setEventMeetingType(eventMeetingType);
        event.setTitle(eventRequest.title());
        event.setDescription(eventRequest.description());
        event.setLocation(eventRequest.location());
        event.setZoomMeetingLink(eventRequest.zoomUrl());
        event.setEventOptions(options);

        return event;
    }

}
