package com.event_service.event_service.services;

import com.event_service.event_service.dto.*;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.TicketType;
import com.event_service.event_service.repositories.EventInvitationRepository;
import com.event_service.event_service.repositories.EventRegistrationRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.repositories.TicketRepository;
import com.event_service.event_service.utils.SecurityUtils;
import com.example.common_libraries.dto.AppUser;
import com.example.common_libraries.exception.BadRequestException;
import com.example.common_libraries.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MyEventServiceImpl implements MyEventService {
    private final SecurityUtils securityUtils;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final TicketRepository ticketRepository;
    private final EventInvitationRepository eventInvitationRepository;

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

    @Override
    public MyEventDetailResponse getMyEventDetailsById(Long eventId) {
        if(eventId == null){
            throw new BadRequestException("Invalid event ID");
        }
        AppUser currentUser = securityUtils.getCurrentUser();

        Event event = eventRepository.findByIdAndUserId(eventId, currentUser.id())
                .orElseThrow(()-> new ResourceNotFoundException("Event not found"));

        Long totalAttendees = (long) event.getEventRegistrations().size();
        Double totalTicketSales = ticketRepository.findTotalTicketSalesForEvent(event);

        MyEventsOverviewResponse eventStat = MyEventsOverviewResponse
                .builder()
                .totalAttendees(totalAttendees)
                .totalTicketSales(totalTicketSales)
                .build();

        MyEventSummaryResponse eventSummary = MyEventSummaryResponse
                .builder()
                .organizer(event.getCreatedBy())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .build();

        List<MyEventTicketTypeStats> ticketTypes = Optional.ofNullable(event.getTicketTypes()).orElse(List.of())
                .stream()
                .map(type -> MyEventTicketTypeStats
                        .builder()
                        .id(type.getId())
                        .name(type.getType())
                        .remainingTickets(type.getQuantity() - type.getSoldCount())
                        .soldTickets(type.getSoldCount())
                        .build()
                ).toList();

        Long totalInvitedGuests = eventInvitationRepository.findAllByEvent(event)
                .stream()
                .mapToLong(invitation -> invitation.getInvitees().size())
                .sum();

        return MyEventDetailResponse
                .builder()
                .eventStats(eventStat)
                .eventSummary(eventSummary)
                .ticketTypes(ticketTypes)
                .totalInvitedGuests(totalInvitedGuests)
                .build();
    }
}
