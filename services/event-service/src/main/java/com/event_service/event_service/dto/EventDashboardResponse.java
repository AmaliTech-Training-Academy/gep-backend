package com.event_service.event_service.dto;

import com.example.common_libraries.dto.TopOrganizerResponse;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record EventDashboardResponse(
        EventStatisticsResponse eventStats,
        List<TopOrganizerResponse> topOrganizers,
        List<UpcomingEventResponse> upcomingEvents,
        Page<EventManagementResponse> eventManagement
) {
}
