package com.event_service.event_service.mappers;


import com.event_service.event_service.dto.EventMeetingTypeResponse;
import com.event_service.event_service.models.EventMeetingType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMeetingMapper {
    EventMeetingTypeResponse toEventMeetingTypeResponse(EventMeetingType eventMeetingType);
}
