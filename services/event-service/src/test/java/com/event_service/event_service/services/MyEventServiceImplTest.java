package com.event_service.event_service.services;

import com.event_service.event_service.client.UserServiceClient;
import com.event_service.event_service.dto.*;
import com.event_service.event_service.models.*;
import com.event_service.event_service.repositories.*;
import com.event_service.event_service.utils.SecurityUtils;
import com.example.common_libraries.dto.AppUser;
import com.example.common_libraries.dto.HostsResponse;
import com.example.common_libraries.exception.BadRequestException;
import com.example.common_libraries.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

    @Mock
    private EventInvitationRepository eventInvitationRepository;

    @Mock
    private EventOrganizerRepository eventOrganizerRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private MyEventServiceImpl myEventService;

    private AppUser currentUser;

    private final String accessToken = "dummy-token";

    @BeforeEach
    void setUp() {
        currentUser = mock(AppUser.class);
        lenient().when(currentUser.id()).thenReturn(1L);
        lenient().when(securityUtils.getCurrentUser()).thenReturn(currentUser);
    }

    // ----------------------------
    // getMyEvents tests
    // ----------------------------
    @Test
    void getMyEvents_shouldReturnMappedPage_whenUserHasEvents() {
        EventRegistration reg1 = new EventRegistration();
        EventRegistration reg2 = new EventRegistration();

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
        event1.setEventRegistrations(List.of(reg1, reg2));
        event1.setTicketTypes(List.of(paidTicket, freeTicket));

        Page<Event> eventsPage = new PageImpl<>(List.of(event1));

        when(eventRepository.findAllByUserId(eq(1L), any(Pageable.class))).thenReturn(eventsPage);

        Page<MyEventsListResponse> result = myEventService.getMyEvents(1);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        MyEventsListResponse response = result.getContent().getFirst();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Music Festival");
        assertThat(response.location()).isEqualTo("Accra");
        assertThat(response.flyerUrl()).isEqualTo("flyer.jpg");
        assertThat(response.attendeesCount()).isEqualTo(2L);
        assertThat(response.isPaid()).isTrue();
    }

    @Test
    void getMyEvents_shouldHandleNoEventsGracefully() {
        when(eventRepository.findAllByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<MyEventsListResponse> result = myEventService.getMyEvents(0);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    // ----------------------------
    // getMyEventsOverview tests
    // ----------------------------
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

    // ----------------------------
    // getMyEventDetailsById tests
    // ----------------------------
    @Test
    void getMyEventDetailsById_shouldReturnDetails_withHostsAndInvitedGuests() {
        EventRegistration reg1 = new EventRegistration();
        EventRegistration reg2 = new EventRegistration();

        Event event = new Event();
        event.setId(100L);
        event.setLocation("Accra Arena");
        event.setStartTime(Instant.parse("2025-11-11T10:00:00Z"));
        event.setCreatedBy("John Organizer");
        event.setEventRegistrations(List.of(reg1, reg2));

        // Properly initialize ticket type to avoid NPE
        TicketType ticketType = new TicketType();
        ticketType.setId(1L);
        ticketType.setType("VIP");
        ticketType.setIsPaid(true);
        ticketType.setQuantity(100L);
        ticketType.setSoldCount(30L);
        event.setTicketTypes(List.of(ticketType));

        // Mock total ticket sales
        when(ticketRepository.findTotalTicketSalesForEvent(event)).thenReturn(1500.75);
        EventInvitee invitee1 = EventInvitee.builder().inviteeName("guest1").build();
        EventInvitee invitee2 = EventInvitee.builder().inviteeName("guest2").build();
        EventInvitee invitee3 = EventInvitee.builder().inviteeName("guest3").build();

        // Mock invitations
        when(eventInvitationRepository.findAllByEvent(event)).thenReturn(List.of(
                new EventInvitation() {{ setInvitees(List.of(invitee1, invitee2)); }},
                new EventInvitation() {{ setInvitees(List.of(invitee3)); }}
        ));

        // Mock event organizers / hosts
        when(eventOrganizerRepository.findUserIdsByEventId(100L)).thenReturn(List.of(2L, 3L));
        when(userServiceClient.getEventHosts(List.of(2L, 3L), accessToken))
                .thenReturn(List.of(
                        HostsResponse.builder().id(2L).fullName("Alice").build(),
                        HostsResponse.builder().id(3L).fullName("Bob").build()
                ));

        when(eventRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(event));

        MyEventDetailResponse response = myEventService.getMyEventDetailsById(100L, accessToken);

        assertThat(response).isNotNull();
        assertThat(response.totalInvitedGuests()).isEqualTo(3L);
        assertThat(response.eventHosts()).hasSize(2);
        assertThat(response.eventHosts().getFirst().fullName()).isEqualTo("Alice");
        assertThat(response.eventStats().totalAttendees()).isEqualTo(2L);
        assertThat(response.eventStats().totalTicketSales()).isEqualTo(1500.75);
        assertThat(response.eventSummary().organizer()).isEqualTo("John Organizer");
    }

    @Test
    void getMyEventDetailsById_shouldThrowBadRequestException_whenIdIsNull() {
        assertThatThrownBy(() -> myEventService.getMyEventDetailsById(null, accessToken))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid event ID");
    }

    @Test
    void getMyEventDetailsById_shouldThrowResourceNotFoundException_whenEventMissing() {
        when(eventRepository.findByIdAndUserId(900L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> myEventService.getMyEventDetailsById(900L, accessToken))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found");
    }

    @Test
    void getMyEventDetailsById_shouldHandleNullTicketSalesGracefully() {
        Event event = new Event();
        event.setId(200L);
        event.setEventRegistrations(List.of());
        event.setCreatedBy("Jane Doe");
        event.setLocation("Lagos Stadium");
        event.setStartTime(Instant.parse("2025-12-01T15:00:00Z"));

        when(eventRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(event));
        when(ticketRepository.findTotalTicketSalesForEvent(event)).thenReturn(null);
        when(eventInvitationRepository.findAllByEvent(event)).thenReturn(List.of());
        when(eventOrganizerRepository.findUserIdsByEventId(200L)).thenReturn(List.of());
        when(userServiceClient.getEventHosts(List.of(), accessToken)).thenReturn(List.of());

        MyEventDetailResponse response = myEventService.getMyEventDetailsById(200L, accessToken);

        assertThat(response.eventStats().totalAttendees()).isZero();
        assertThat(response.eventStats().totalTicketSales()).isNull();
        assertThat(response.totalInvitedGuests()).isZero();
        assertThat(response.eventHosts()).isEmpty();
    }
}
