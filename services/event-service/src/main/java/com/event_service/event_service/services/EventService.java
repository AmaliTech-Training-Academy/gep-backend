package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.dto.EventResponse;
import com.event_service.event_service.dto.ExploreEventResponse;
import com.event_service.event_service.dto.PagedExploreEventResponse;
import com.event_service.event_service.models.Event;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface EventService {
    EventResponse createEvent(EventRequest eventRequest, MultipartFile image, List<MultipartFile> eventImages);
    PagedExploreEventResponse listEvents(
            int pageNumber,
            int pageSize,
            String hasTitle,
            String[] sortBy,
            String location,
            LocalDate date,
            Boolean paid,
            String priceFilter,
            Boolean past
    );
}
