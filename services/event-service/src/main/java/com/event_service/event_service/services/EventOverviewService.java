package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventDashboardResponse;

public interface EventOverviewService {
    EventDashboardResponse getEventOverview(String accessToken);
}
