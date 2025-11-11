package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventDashboardResponse;
import com.event_service.event_service.dto.EventManagementResponse;
import com.event_service.event_service.models.enums.EventStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EventOverviewService {
    EventDashboardResponse getEventOverview(String accessToken);
    Page<EventManagementResponse> getManagementEvents(String keyword, int page, EventStatus status);
}
