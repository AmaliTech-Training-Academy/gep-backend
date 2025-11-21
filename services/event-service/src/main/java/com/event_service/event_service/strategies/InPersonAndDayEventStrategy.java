package com.event_service.event_service.strategies;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.dto.EventSectionRequest;
import com.event_service.event_service.models.*;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.utils.TimeZoneUtils;
import com.example.common_libraries.dto.AppUser;
import com.example.common_libraries.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
                             EventMeetingType eventMeetingType,
                             List<MultipartFile> sectionImages
                             ) {

        List<EventSectionRequest> sections =
                eventRequest.eventSectionRequest() != null
                        ? eventRequest.eventSectionRequest()
                        : List.of();

        List<MultipartFile> sectionImagesList =
                sectionImages != null
                        ? sectionImages
                        : List.of();

        if (sections.size() != sectionImagesList.size()) {
            throw new IllegalArgumentException("Section count and image count must match.");
        }

        EventOptions eventOptions = EventOptions.builder()
                .ticketPrice(eventRequest.eventOptionsRequest().ticketPrice())
                .capacity(eventRequest.eventOptionsRequest().capacity())
                .requiresApproval(eventRequest.eventOptionsRequest().requiresApproval())
                .build();

        String uploadedFlyer = s3Service.uploadImage(image);

        ZoneId zoneId = timeZoneUtils.createZoneId(eventRequest.event_time_zone_id());
        ZonedDateTime zonedDateTime = timeZoneUtils.createZonedTimeDate(
                eventRequest.event_date(),
                eventRequest.event_time(),
                zoneId
        );
        Instant eventInstant = zonedDateTime.toInstant();

        AppUser authenticatedUser = getCurrentUser();

        Event event = Event.builder()
                .title(eventRequest.title())
                .description(eventRequest.description())
                .eventTime(eventInstant)
                .eventTimeZoneId(eventRequest.event_time_zone_id())
                .location(eventRequest.location())
                .flyerUrl(uploadedFlyer)
                .eventType(eventType)
                .eventMeetingType(eventMeetingType)
                .eventOptions(eventOptions)
                .createdBy(authenticatedUser.fullName())
                .userId(authenticatedUser.id())
                .build();

        createVenueSections(sections, sectionImagesList, event);

        attachEventImages(eventImages, event);

        createFreeTicket(eventRequest, event);

        createPaidTicket(eventRequest, event);


        return eventRepository.save(event);
    }


    private void createVenueSections(List<EventSectionRequest> sections, List<MultipartFile> sectionImagesList, Event event) {
        for (int i = 0; i < sections.size(); i++) {
            EventSectionRequest req = sections.get(i);
            MultipartFile img = sectionImagesList.get(i);

            String imageUrl = s3Service.uploadImage(img);

            EventSection section = EventSection.builder()
                    .name(req.name())
                    .capacity(req.capacity().intValue())
                    .price(req.price())
                    .description(req.description())
                    .color(req.color())
                    .imageUrl(imageUrl)
                    .event(event)
                    .build();

            event.addSection(section);
        }
    }

    private static void createFreeTicket(EventRequest eventRequest, Event savedEvent) {
        if (eventRequest.eventOptionsRequest().ticketPrice().compareTo(BigDecimal.ZERO) == 0) {

            TicketType freeTicket = TicketType.builder()
                    .event(savedEvent)
                    .description("Free Ticket")
                    .price(0.0)
                    .quantity(eventRequest.eventOptionsRequest().capacity())
                    .soldCount(0L)
                    .isActive(true)
                    .isPaid(false)
                    .type("FREE")
                    .quantityPerAttendee(1)
                    .build();
            savedEvent.addTicketType(freeTicket);
        }
    }

    private static void createPaidTicket(EventRequest eventRequest, Event savedEvent) {
        if (eventRequest.eventOptionsRequest().ticketPrice().compareTo(BigDecimal.ZERO) > 0) {
            TicketType paidTicket = TicketType.builder()
                    .description("General Admission")
                    .price(eventRequest.eventOptionsRequest().ticketPrice().doubleValue())
                    .quantity(eventRequest.eventOptionsRequest().capacity())
                    .soldCount(0L)
                    .isActive(true)
                    .isPaid(true)
                    .type("REGULAR")
                    .quantityPerAttendee(1)
                    .build();

            savedEvent.addTicketType(paidTicket);
        }
    }

    public void attachEventImages(List<MultipartFile> eventImages, Event event){
        if(!CollectionUtils.isEmpty(eventImages)) {
            List<String> uploadedImages = s3Service.uploadImages(eventImages);
            for(String uploadEventImage : uploadedImages) {
                EventImages eventImage = EventImages.builder()
                        .image(uploadEventImage)
                        .build();
                event.addImage(eventImage);
            }
        }
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AppUser) authentication.getPrincipal();
    }
}
