package com.event_service.event_service.services;

import com.event_service.event_service.dto.MyEventsListResponse;
import com.event_service.event_service.dto.MyEventsOverviewResponse;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventRegistration;
import com.event_service.event_service.models.TicketType;
import com.event_service.event_service.repositories.EventRegistrationRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.repositories.TicketRepository;
import com.example.common_libraries.dto.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import com.event_service.event_service.utils.SecurityUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MyEventServiceImplTest {

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventRegistrationRepository eventRegistrationRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private MyEventServiceImpl myEventService;

    @BeforeEach
    void setUp() {
        AppUser currentUser;

        currentUser = mock(AppUser.class);
        when(currentUser.id()).thenReturn(1L);
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
    }

    // ---------------------------------------------------------
    // getMyEvents tests
    // ---------------------------------------------------------

    @Test
    void getMyEvents_shouldReturnMappedPage_whenUserHasEvents() {
        // given
        EventRegistration registration1 = new EventRegistration();
        EventRegistration registration2 = new EventRegistration();

        TicketType paidTicket = new TicketType();
        paidTicket.setIsPaid(true);

        TicketType freeTicket = new TicketType();
        freeTicket.setIsPaid(false);

        Event event1 = new Event();
        event1.setId(10L);
        event1.setTitle("Music Festival");
        event1.setStartTime(Instant.now());
        event1.setLocation("Accra");
        event1.setFlyerUrl("flyer.jpg");
        event1.setEventRegistrations(List.of(registration1, registration2));
        event1.setTicketTypes(List.of(paidTicket, freeTicket));

        Page<Event> eventsPage = new PageImpl<>(List.of(event1));

        when(eventRepository.findAllByUserId(eq(1L), any(Pageable.class))).thenReturn(eventsPage);

        // when
        Page<MyEventsListResponse> result = myEventService.getMyEvents(1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        MyEventsListResponse response = result.getContent().getFirst();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Music Festival");
        assertThat(response.location()).isEqualTo("Accra");
        assertThat(response.flyerUrl()).isEqualTo("flyer.jpg");
        assertThat(response.attendeesCount()).isEqualTo(2L);
        assertThat(response.isPaid()).isTrue(); // because at least one ticket is paid

        // verify paging and sorting setup
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findAllByUserId(eq(1L), pageableCaptor.capture());
        Pageable usedPageable = pageableCaptor.getValue();
        assertThat(usedPageable.getPageNumber()).isEqualTo(1);
        assertThat(usedPageable.getPageSize()).isEqualTo(10);
        assertThat(Objects.requireNonNull(usedPageable.getSort().getOrderFor("createdAt")).getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getMyEvents_shouldHandleNoEventsGracefully() {
        when(eventRepository.findAllByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<MyEventsListResponse> result = myEventService.getMyEvents(0);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getMyEvents_shouldCoerceNegativePageToZero() {
        when(eventRepository.findAllByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty());

        myEventService.getMyEvents(-5);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findAllByUserId(eq(1L), pageableCaptor.capture());
        Pageable pageableUsed = pageableCaptor.getValue();
        assertThat(pageableUsed.getPageNumber()).isZero();
    }

    @Test
    void getMyEvents_shouldReturnIsPaidFalse_whenAllTicketsAreFree() {
        Event event = new Event();
        event.setId(11L);
        event.setTitle("Community Meetup");
        event.setEventRegistrations(List.of());
        TicketType free = new TicketType();
        free.setIsPaid(false);
        event.setTicketTypes(List.of(free));

        Page<Event> eventsPage = new PageImpl<>(List.of(event));
        when(eventRepository.findAllByUserId(eq(1L), any(Pageable.class))).thenReturn(eventsPage);

        Page<MyEventsListResponse> result = myEventService.getMyEvents(0);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().isPaid()).isFalse();
    }

    // ---------------------------------------------------------
    // getMyEventsOverview tests
    // ---------------------------------------------------------

    @Test
    void getMyEventsOverview_shouldReturnExpectedAggregates() {
        when(eventRepository.countByUserId(1L)).thenReturn(5L);
        when(eventRegistrationRepository.countByEventUserId(1L)).thenReturn(100L);
        when(ticketRepository.findTotalTicketSalesForUser(1L)).thenReturn(2500.50);

        MyEventsOverviewResponse response = myEventService.getMyEventsOverview();

        assertThat(response.totalEvents()).isEqualTo(5L);
        assertThat(response.totalAttendees()).isEqualTo(100L);
        assertThat(response.totalTicketSales()).isEqualTo(2500.50);
    }

    @Test
    void getMyEventsOverview_shouldHandleNullValuesGracefully() {
        // if repository returns nulls, service should not throw
        when(eventRepository.countByUserId(1L)).thenReturn(null);
        when(eventRegistrationRepository.countByEventUserId(1L)).thenReturn(null);
        when(ticketRepository.findTotalTicketSalesForUser(1L)).thenReturn(null);

        MyEventsOverviewResponse response = myEventService.getMyEventsOverview();

        // verify nulls are simply propagated
        assertThat(response.totalEvents()).isNull();
        assertThat(response.totalAttendees()).isNull();
        assertThat(response.totalTicketSales()).isNull();
    }
}
