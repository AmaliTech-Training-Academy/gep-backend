package com.event_service.event_service.strategies;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventMeetingType;
import com.event_service.event_service.models.EventType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventStrategy {
    Event createEvent(EventRequest eventRequest,
                              MultipartFile image,
                              List<MultipartFile> eventImages,
                              EventType eventType,
                              EventMeetingType eventMeetingType
    );

}
