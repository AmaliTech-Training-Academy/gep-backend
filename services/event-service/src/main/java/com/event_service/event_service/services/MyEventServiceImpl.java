package com.event_service.event_service.services;

import com.event_service.event_service.dto.MyEventsListResponse;
import com.event_service.event_service.dto.MyEventsOverviewResponse;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.TicketType;
import com.event_service.event_service.repositories.EventRegistrationRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.repositories.TicketRepository;
import com.event_service.event_service.utils.SecurityUtils;
import com.example.common_libraries.dto.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MyEventServiceImpl implements MyEventService {
    private final SecurityUtils securityUtils;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final TicketRepository ticketRepository;

    @Override
    public Page<MyEventsListResponse> getMyEvents(int page) {
        page = Math.max(0, page);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, 3, sort);
        AppUser currentUser = securityUtils.getCurrentUser();
        Page<Event> myEvents = eventRepository.findAllByUserId(currentUser.id(), pageable);

        return myEvents.map(
                event -> MyEventsListResponse
                        .builder()
                        .id(event.getId())
                        .title(event.getTitle())
                        .startTime(event.getStartTime())
                        .location(event.getLocation())
                        .flyerUrl(event.getFlyerUrl())
                        .attendeesCount((long) event.getEventRegistrations().size())
                        .isPaid(event.getTicketTypes().stream().anyMatch(TicketType::getIsPaid))
                        .build()
        );
    }

    @Override
    public MyEventsOverviewResponse getMyEventsOverview() {
        AppUser currentUser = securityUtils.getCurrentUser();
        Long totalEvents = eventRepository.countByUserId(currentUser.id());
        Long totalAttendees = eventRegistrationRepository.countByEventUserId(currentUser.id());
        Double totalTicketSales = ticketRepository.findTotalTicketSalesForUser(currentUser.id());

        return MyEventsOverviewResponse
                .builder()
                .totalEvents(totalEvents)
                .totalAttendees(totalAttendees)
                .totalTicketSales(totalTicketSales)
                .build();
    }
}
