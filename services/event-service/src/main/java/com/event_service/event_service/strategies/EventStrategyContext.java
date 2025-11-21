package com.event_service.event_service.strategies;


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
public class EventStrategyContext {

    private EventStrategy eventStrategy;

    public Event executeStrategy(
            EventRequest eventRequest,
            MultipartFile image,
            List<MultipartFile> eventImages,
            EventType eventType,
            EventMeetingType eventMeetingType,
            List<MultipartFile> sectionImages

            ) {
        return eventStrategy.createEvent(eventRequest,
                image,
                eventImages,
                eventType,
                eventMeetingType,
                sectionImages
        );
    }
}
