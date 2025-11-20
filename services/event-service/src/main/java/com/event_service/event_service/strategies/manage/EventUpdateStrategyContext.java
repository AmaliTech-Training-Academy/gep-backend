package com.event_service.event_service.strategies.manage;


import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventMeetingType;
import com.event_service.event_service.models.EventType;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@Setter
public class EventUpdateStrategyContext {

    private UpdateEventStrategy updateEventStrategy;

    public Event executeStrategy(
            Long id,
            EventRequest eventRequest,
            MultipartFile image,
            List<MultipartFile> newEventImages,
            List<Long> imagesToRemove,
            Event event,
             EventType eventType,
            EventMeetingType eventMeetingType
    ) {
        return updateEventStrategy.updateEvent(
                id,
                eventRequest,
                image,
                newEventImages,
                imagesToRemove,
                event,
                eventType,
                eventMeetingType
        );
    }
}
