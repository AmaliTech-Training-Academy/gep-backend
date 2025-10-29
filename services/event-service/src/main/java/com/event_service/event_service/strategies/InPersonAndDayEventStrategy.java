package com.event_service.event_service.strategies;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.models.*;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.services.S3Service;
import com.event_service.event_service.utilities.TimeZoneUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InPersonAndDayEventStrategy implements EventStrategy {

    private final S3Service s3Service;
    private final EventRepository eventRepository;
    private final TimeZoneUtils timeZoneUtils;

    @Override
    public Event createEvent(EventRequest eventRequest,
                             MultipartFile image,
                             List<MultipartFile> eventImages,
                             EventType eventType,
                             EventMeetingType eventMeetingType
    ){
        EventOptions eventOptions = EventOptions.builder()
                .ticketPrice(eventRequest.eventOptionsRequest().ticketPrice())
                .capacity(eventRequest.eventOptionsRequest().capacity())
                .requiresApproval(eventRequest.eventOptionsRequest().requiresApproval())
                .build();
        String uploadedFlyer = s3Service.uploadImage(image);
        ZoneId zoneId = timeZoneUtils.createZoneId(eventRequest.event_time_zone_id());
        ZonedDateTime zonedDateTime = timeZoneUtils.createZonedTimeDate( eventRequest.event_date(),
                eventRequest.event_time(),
                zoneId);
        Instant eventInstant = zonedDateTime.toInstant();
        Event event = Event.builder().eventOptions(eventOptions)
                .eventMeetingType(eventMeetingType).eventType(eventType)
                .eventType(eventType)
                .title(eventRequest.title())
                .description(eventRequest.description())
                .eventTime(eventInstant)
                .eventTimeZoneId(eventRequest.event_time_zone_id())
                .flyerUrl(uploadedFlyer)
                .location(eventRequest.location())
                .build();
        Event eventWithImages = attachEventImages(eventImages,event);
        eventWithImages.setEventOptions(eventOptions);
        return eventRepository.save(event);
    }

    public Event attachEventImages(List<MultipartFile> eventImages, Event event){
        if(!CollectionUtils.isEmpty(eventImages)) {
            List<String> uploadedImages = s3Service.uploadImages(eventImages);
            for(String uploadEventImage : uploadedImages) {
                EventImages eventImage = EventImages.builder()
                        .image(uploadEventImage)
                        .build();
                event.addImage(eventImage);
            }
        }
        return event;
    }
}
