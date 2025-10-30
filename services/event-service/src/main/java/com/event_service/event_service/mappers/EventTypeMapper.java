package com.event_service.event_service.mappers;

import com.event_service.event_service.dto.EventTypeResponse;
import com.event_service.event_service.models.EventType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventTypeMapper {
    EventTypeResponse toEventTypeResponse(EventType eventType);
}
