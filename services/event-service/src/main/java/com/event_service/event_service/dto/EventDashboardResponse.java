package com.event_service.event_service.dto;

import com.example.common_libraries.dto.TopOrganizerResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record EventDashboardResponse(
        EventStatisticsResponse eventStats,
        List<TopOrganizerResponse> topOrganizers,
        List<UpcomingEventResponse> upcomingEvents,
        List<EventManagementResponse> eventManagement
) {
}
