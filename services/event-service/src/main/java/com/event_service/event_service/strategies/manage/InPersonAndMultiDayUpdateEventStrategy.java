package com.event_service.event_service.strategies.manage;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.models.*;
import com.event_service.event_service.utils.TimeZoneUtils;
import com.example.common_libraries.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InPersonAndMultiDayUpdateEventStrategy implements UpdateEventStrategy {
    private final S3Service s3Service;
    private final TimeZoneUtils timeZoneUtils;

    @Override
    public Event updateEvent(Long id, EventRequest eventRequest, MultipartFile image, List<MultipartFile> newEventImages, List<Long> imagesToRemove, Event event, EventType eventType, EventMeetingType eventMeetingType) {
        EventOptions options = event.getEventOptions();
        options.setCapacity(eventRequest.eventOptionsRequest().capacity());
        options.setTicketPrice(eventRequest.eventOptionsRequest().ticketPrice());
        options.setRequiresApproval(eventRequest.eventOptionsRequest().requiresApproval());

        if (image != null && !image.isEmpty()) {
            String uploadedFlyer = s3Service.uploadImage(image);
            event.setFlyerUrl(uploadedFlyer);
        }

        ZoneId startZone = timeZoneUtils.createZoneId(eventRequest.event_start_time_zone_id());
        ZonedDateTime startZoned = timeZoneUtils.createZonedTimeDate(
                eventRequest.event_start_time_date(),
                eventRequest.event_start_time(),
                startZone
        );
        event.setStartTime(startZoned.toInstant());
        event.setStartTimeZoneId(eventRequest.event_start_time_zone_id());

        ZoneId endZone = timeZoneUtils.createZoneId(eventRequest.event_end_time_zone_id());
        ZonedDateTime endZoned = timeZoneUtils.createZonedTimeDate(
                eventRequest.event_end_time_date(),
                eventRequest.event_end_time(),
                endZone
        );
        event.setEndTime(endZoned.toInstant());
        event.setEndTimeZoneId(eventRequest.event_end_time_zone_id());

        event.setEventType(eventType);
        event.setEventMeetingType(eventMeetingType);
        event.setTitle(eventRequest.title());
        event.setDescription(eventRequest.description());
        event.setLocation(eventRequest.location());
        event.setEventOptions(options);

        if (imagesToRemove != null && !imagesToRemove.isEmpty()) {
            event.getEventImages().removeIf(img -> {
                boolean shouldRemove = imagesToRemove.contains(img.getId());
                if (shouldRemove) img.setEvent(null);
                return shouldRemove;
            });
        }

        if (newEventImages != null && newEventImages.size() > 1) {
            if (event.getEventImages().size() + newEventImages.size() > 5) {
                throw new IllegalArgumentException("Max 5 images allowed per event");
            }

            List<String> uploadedImages = s3Service.uploadImages(newEventImages);
            uploadedImages.forEach(url -> {
                EventImages img = EventImages.builder()
                        .image(url)
                        .event(event)
                        .build();
                event.addImage(img);
            });
        }

        return event;
    }
}
