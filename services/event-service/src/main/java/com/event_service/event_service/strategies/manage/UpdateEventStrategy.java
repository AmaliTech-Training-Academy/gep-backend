package com.event_service.event_service.strategies.manage;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.dto.EventUpdateResponse;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventMeetingType;
import com.event_service.event_service.models.EventType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UpdateEventStrategy {
    Event updateEvent(Long id,
                      EventRequest eventRequest,
                      MultipartFile image,
                      List<MultipartFile> newEventImages,
                      List<Long> imagesToRemove,
                      Event event,
                      EventType eventType,
                      EventMeetingType eventMeetingType
                      );
}
