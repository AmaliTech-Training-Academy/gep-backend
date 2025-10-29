package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.dto.EventResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {
    EventResponse createEvent(EventRequest eventRequest, MultipartFile image, List<MultipartFile> eventImages);
}
