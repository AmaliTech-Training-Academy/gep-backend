package com.event_service.event_service.strategies;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.models.*;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.utils.TimeZoneUtils;
import com.event_service.event_service.validations.EventValidator;
import com.example.common_libraries.dto.AppUser;
import com.example.common_libraries.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
    private final EventValidator eventValidator;


    @Override
    public Event createEvent(EventRequest eventRequest,
                             MultipartFile image,
                             List<MultipartFile> eventImages,
                             EventType eventType,
                             EventMeetingType eventMeetingType,
                             List<MultipartFile> sectionImages
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
        AppUser authenticatedUser = getCurrentUser();
        Instant eventInstant = zonedDateTime.toInstant();
        String message = "Event time must be now or in the future.";
        eventValidator.validateEventDayInstant(eventInstant,message);
                String uploadedFlyer = s3Service.uploadImage(image);
                Event event = Event.builder().eventOptions(eventOptions)
                        .eventMeetingType(eventMeetingType).eventType(eventType)
                        .eventType(eventType)
                        .title(eventRequest.title())
                        .description(eventRequest.description())
                        .eventTime(eventInstant)
                        .eventTimeZoneId(eventRequest.event_time_zone_id())
                        .flyerUrl(uploadedFlyer)
                        .createdBy(authenticatedUser.fullName())
                        .userId(authenticatedUser.id())
                        .zoomMeetingLink(eventRequest.zoomUrl())
                        .build();
                event.setEventOptions(eventOptions);

        createFreeTicket(eventRequest, event);

        createPaidTicket(eventRequest, event);

        return eventRepository.save(event);
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
                    .type("PAID")
                    .quantityPerAttendee(1)
                    .build();

            savedEvent.addTicketType(paidTicket);
        }
    }


    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AppUser) authentication.getPrincipal();
    }

}
