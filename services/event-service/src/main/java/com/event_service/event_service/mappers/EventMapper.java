package com.event_service.event_service.mappers;

import com.event_service.event_service.dto.EventResponse;
import com.event_service.event_service.models.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "startTime", expression = "java(determineDisplayTime(event))")
    @Mapping(target = "meetingLocation", expression = "java(determineMeetingLocation(event))")
    @Mapping(target = "timeZoneOffSet", expression = "java(determineZoneId(event))")
    EventResponse toResponse(Event event);

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