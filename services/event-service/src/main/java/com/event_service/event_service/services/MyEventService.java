package com.event_service.event_service.services;

import com.event_service.event_service.dto.MyEventsListResponse;
import com.event_service.event_service.dto.MyEventsOverviewResponse;
import org.springframework.data.domain.Page;

public interface MyEventService {
    Page<MyEventsListResponse> getMyEvents(int page);
    MyEventsOverviewResponse getMyEventsOverview();
}
