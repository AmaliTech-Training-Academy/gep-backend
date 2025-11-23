package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.dto.EventResponse;
import com.event_service.event_service.dto.EventUpdateResponse;
import com.event_service.event_service.dto.PagedExploreEventResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface EventService {
    EventResponse createEvent(EventRequest eventRequest, MultipartFile image, List<MultipartFile> eventImages,        List<MultipartFile> sectionImages
    );
    PagedExploreEventResponse listEvents(
            int pageNumber,
            int pageSize,
            String hasTitle,
            String[] sortBy,
            String location,
            LocalDate date,
            String priceFilter,
            Boolean past
    );

    EventUpdateResponse updateEvent(Long id, EventRequest eventRequest, MultipartFile image, List<MultipartFile> eventImages, List<Long> imagesToUpdate);
}
