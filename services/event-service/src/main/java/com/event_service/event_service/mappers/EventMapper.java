package com.event_service.event_service.mappers;

import com.event_service.event_service.dto.*;
import com.event_service.event_service.models.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;



@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "startTime", expression = "java(determineDisplayTime(event))")
    @Mapping(target = "location", expression = "java(determineMeetingLocation(event))")
    @Mapping(target = "timeZoneOffSet", expression = "java(determineZoneId(event))")
    EventResponse toResponse(Event event);

    @Mapping(target = "startTime", expression = "java(determineDisplayTime(event))")
    @Mapping(target = "ticketPrice", expression = "java(extractTicketPrice(event))")
    @Mapping(target = "attendeeCount", expression = "java(extractInvitationCount(event))")
    ExploreEventResponse toExploreEventResponse(Event event);

    default Long extractInvitationCount(Event event) {
        if (event.getInvitations() == null) return 0L;
        return (long) event.getEventRegistrations().size();
    }


    default java.math.BigDecimal extractTicketPrice(Event event) {
        return (event.getEventOptions() != null)
                ? event.getEventOptions().getTicketPrice()
                : null;
    }

    default java.time.Instant determineDisplayTime(Event event) {
        if (event.getEventType() == null || event.getEventType().getName() == null) {
            return null;
        }

        return switch (event.getEventType().getName()) {
            case DAY_EVENT -> event.getEventTime();
            case MULTI_DAY_EVENT -> event.getStartTime();
        };
    }

    default String determineZoneId(Event event) {
        if (event.getEventType() == null || event.getEventType().getName() == null) {
            return null;
        }

        return switch (event.getEventType().getName()){
            case DAY_EVENT -> event.getEventTimeZoneId();
            case MULTI_DAY_EVENT -> event.getStartTimeZoneId();
        };
    }

    default String determineMeetingLocation(Event event) {
        if (event.getEventMeetingType() == null || event.getEventMeetingType().getName() == null) {
            return null;
        }

        return switch (event.getEventMeetingType().getName()) {
            case IN_PERSON -> event.getLocation();
            case VIRTUAL -> event.getZoomMeetingLink();
        };
    }
}