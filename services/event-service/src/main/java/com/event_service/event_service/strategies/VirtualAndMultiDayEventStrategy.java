package com.event_service.event_service.strategies;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventMeetingType;
import com.event_service.event_service.models.EventOptions;
import com.event_service.event_service.models.EventType;
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
public class VirtualAndMultiDayEventStrategy implements  EventStrategy {

    private final S3Service s3Service;
    private final EventRepository eventRepository;
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
        String uploadedFlyer = s3Service.uploadImage(image);
        ZoneId startTimeZoneId = timeZoneUtils.createZoneId(eventRequest.event_start_time_zone_id());
        ZonedDateTime startTimeZonedDateTime = timeZoneUtils.createZonedTimeDate(
                eventRequest.event_start_time_date(),
                eventRequest.event_start_time(),
                startTimeZoneId
        );
        Instant eventStartTimeInstant = startTimeZonedDateTime.toInstant();

        ZoneId endTimeZoneId = timeZoneUtils.createZoneId(eventRequest.event_end_time_zone_id());
        ZonedDateTime endTimeZonedDateTime = timeZoneUtils.createZonedTimeDate(
                eventRequest.event_end_time_date(),
                eventRequest.event_end_time(),
                endTimeZoneId
        );
        Instant eventEndTimeInstant = endTimeZonedDateTime.toInstant();
        Event event = Event.builder()
                .eventOptions(eventOptions)
                .eventMeetingType(eventMeetingType)
                .eventType(eventType)
                .title(eventRequest.title())
                .description(eventRequest.description())
                .startTime(eventStartTimeInstant)
                .startTimeZoneId(eventRequest.event_start_time_zone_id())
                .endTime(eventEndTimeInstant)
                .endTimeZoneId(eventRequest.event_end_time_zone_id())
                .flyerUrl(uploadedFlyer)
                .zoomMeetingLink(eventRequest.zoomUrl())
                .build();
        event.setEventOptions(eventOptions);
        return eventRepository.save(event);
    }
}
