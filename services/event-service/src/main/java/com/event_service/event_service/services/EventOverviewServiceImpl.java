package com.event_service.event_service.services;

import com.event_service.event_service.client.UserServiceClient;
import com.event_service.event_service.dto.EventDashboardResponse;
import com.event_service.event_service.dto.EventManagementResponse;
import com.event_service.event_service.dto.EventStatisticsResponse;
import com.event_service.event_service.dto.UpcomingEventResponse;
import com.event_service.event_service.dto.projection.EventManagementProjection;
import com.event_service.event_service.dto.projection.EventStatProjection;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.repositories.EventRepository;
import com.example.common_libraries.dto.TopOrganizerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventOverviewServiceImpl implements EventOverviewService{
    private final EventRepository eventRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public EventDashboardResponse getEventOverview(String accessToken) {
        // get event stats
        EventStatProjection eventStatProjection = eventRepository.getEventStats(Instant.now());
        EventStatisticsResponse statisticsResponse = EventStatisticsResponse
                .builder()
                .totalEvents(eventStatProjection.getTotalEvents())
                .activeEvents(eventStatProjection.getActiveEvents())
                .canceledEvents(0L)
                .completedEvents(eventStatProjection.getCompletedEvents())
                .draftEvents(eventStatProjection.getDraftEvents())
                .build();

        // get upcoming events
        List<Event> events = eventRepository.findUpcomingEvents(
                Instant.now(),
                PageRequest.of(0, 3));

        List<UpcomingEventResponse> upcomingEventResponses = events.stream()
                .map(event -> UpcomingEventResponse.builder()
                        .eventTitle(event.getTitle())
                        .startTime(event.getStartTime())
                        .attendeeCount(event.getEventRegistrations().size())
                        .build()).toList();
        // get event management
        List<EventManagementProjection> eventManagementProjections = eventRepository.getEventManagement(PageRequest.of(0, 10));

        List<EventManagementResponse> eventManagementResponses = eventManagementProjections.stream()
                .map(emgt -> EventManagementResponse
                        .builder()
                        .id(emgt.getId())
                        .title(emgt.getTitle())
                        .organizer(emgt.getOrganizer())
                        .startTime(emgt.getStartTime())
                        .endTime(emgt.getEndTime())
                        .attendeeCount(emgt.getAttendeeCount())
                        .status(emgt.getStatus())
                        .build()
                ).toList();

        // get top organizers via rest call to user-service
        List<TopOrganizerResponse> topOrganizerResponses = userServiceClient.getTopOrganizers(accessToken);

        return EventDashboardResponse
                .builder()
                .eventStats(statisticsResponse)
                .topOrganizers(topOrganizerResponses)
                .upcomingEvents(upcomingEventResponses)
                .eventManagement(eventManagementResponses)
                .build();
    }
}
