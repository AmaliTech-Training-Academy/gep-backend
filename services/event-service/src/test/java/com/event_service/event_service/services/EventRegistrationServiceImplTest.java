package com.event_service.event_service.services;

import com.event_service.event_service.dto.*;
import com.example.common_libraries.exception.BadRequestException;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.event_service.event_service.mappers.EventMapper;
import com.event_service.event_service.models.*;
import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import com.event_service.event_service.models.enums.EventRegistrationStatusEnum;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.common_libraries.enums.PaymentMethod;
import com.event_service.event_service.repositories.*;
import com.example.common_libraries.dto.PaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventRegistrationServiceImplTest {

    @Mock private TicketTypeRepository ticketTypeRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private EventRepository eventRepository;
    @Mock private EventRegistrationRepository eventRegistrationRepository;
    @Mock private SqsClient sqsClient;
    @Mock private ObjectMapper objectMapper;
    @Mock private EventMapper eventMapper;

    @InjectMocks
    private EventRegistrationServiceImpl eventRegistrationService;

    private Event event;
    private EventMeetingType meetingType;
    private TicketType freeTicketType;
    private TicketType paidTicketType;
    private EventRegistrationRequest freeRequest;
    private EventRegistrationRequest paidRequest;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // simulate @Value injection
        ReflectionTestUtils.setField(eventRegistrationService, "albUrl", "https://test.alb");
        ReflectionTestUtils.setField(eventRegistrationService, "ticketPurchasedEventQueueUrl", "https://sqs.fake/ticket-purchased");
        ReflectionTestUtils.setField(eventRegistrationService, "processPaymentQueueUrl", "https://sqs.fake/payment-processing");

        // Event meeting type
        meetingType = new EventMeetingType();
        meetingType.setName(EventMeetingTypeEnum.VIRTUAL);

        // Event
        event = new Event();
        event.setId(1L);
        event.setTitle("Tech Summit");
        event.setEventMeetingType(meetingType);
        event.setLocation("Accra");
        event.setStartTime(Instant.now());

        // Free ticket type
        freeTicketType = new TicketType();
        freeTicketType.setId(1L);
        freeTicketType.setType("FREE");
        freeTicketType.setQuantity(10L);
        freeTicketType.setSoldCount(0L);
        freeTicketType.setIsPaid(false);
        freeTicketType.setIsActive(true);

        // Paid ticket type
        paidTicketType = new TicketType();
        paidTicketType.setId(2L);
        paidTicketType.setType("VIP");
        paidTicketType.setQuantity(20L);
        paidTicketType.setSoldCount(0L);
        paidTicketType.setIsPaid(true);
        paidTicketType.setIsActive(true);

        // PaymentRequest
        paymentRequest = new PaymentRequest(
                PaymentMethod.CARD,
                100.0,
                null, // no momo
                null,
                "1234567812345678",
                "12/25",
                "123",
                "John Doe"
        );

        // Registration requests
        freeRequest = new EventRegistrationRequest(
                1L, 1L, "Alice Smith", "alice@example.com", null
        );

        paidRequest = new EventRegistrationRequest(
                2L, 2L, "Bob Marley", "bob@example.com", paymentRequest
        );
    }

    // -------------------------------------------------------------------------
    // TESTS
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully register a user for a free event and generate tickets")
    void registerEvent_FreeTicket_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(freeTicketType));
        when(eventRegistrationRepository.save(any())).thenAnswer(invocation -> {
            EventRegistration reg = invocation.getArgument(0);
            reg.setId(10L);
            return reg;
        });
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventMapper.toResponse(event)).thenReturn(
                new EventResponse(event.getId(), event.getTitle(), "desc", event.getStartTime(),
                        "Accra", "flyer.jpg", "+0")
        );

        EventRegistrationResponse response = eventRegistrationService.registerEvent(1L, freeRequest);

        assertNotNull(response);
        assertEquals("Tech Summit", response.eventTitle());
        verify(ticketRepository, times(1)).saveAll(any());
        verify(sqsClient, times(1)).sendMessage(any(Consumer.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when event ID does not exist")
    void registerEvent_EventNotFound_ThrowsException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> eventRegistrationService.registerEvent(99L, freeRequest));
        assertEquals("Event not found", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ticket type does not exist")
    void registerEvent_TicketTypeNotFound_ThrowsException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> eventRegistrationService.registerEvent(1L, freeRequest));
        assertEquals("Ticket Type not found", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw ResourceNotFound when tickets are out of stock")
    void registerEvent_OutOfStock_ThrowsException() {
        freeTicketType.setSoldCount(10L); // all sold
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(freeTicketType));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> eventRegistrationService.registerEvent(1L, freeRequest));
        assertEquals("Ticket Type is out of stock", ex.getMessage());
    }

    @Test
    @DisplayName("Should process paid event registration and simulate payment completion")
    void registerEvent_PaidTicket_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findById(2L)).thenReturn(Optional.of(paidTicketType));
        when(eventRegistrationRepository.save(any())).thenAnswer(invocation -> {
            EventRegistration reg = invocation.getArgument(0);
            reg.setId(22L);
            return reg;
        });

        when(eventMapper.toResponse(event)).thenReturn(
                new EventResponse(event.getId(), event.getTitle(), "desc", event.getStartTime(),
                        "Accra", "flyer.jpg", "+0")
        );

        EventRegistrationResponse response = eventRegistrationService.registerEvent(1L, paidRequest);

        assertNotNull(response);
        assertEquals("Tech Summit", response.eventTitle());
        verify(sqsClient, atLeastOnce()).sendMessage(any(Consumer.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException if payment details missing for paid event")
    void registerEvent_PaidTicketWithoutPayment_ThrowsException() {
        EventRegistrationRequest invalidPaidRequest =
                new EventRegistrationRequest(2L, 2L, "Sam Doe", "sam@example.com", null);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findById(2L)).thenReturn(Optional.of(paidTicketType));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> eventRegistrationService.registerEvent(1L, invalidPaidRequest));
        assertEquals("Payment details is required for paid ticket types", ex.getMessage());
    }

    @Test
    @DisplayName("Should handle payment completion and generate tickets")
    void paymentCompletedListener_GeneratesTickets_Success() {
        EventRegistration reg = EventRegistration.builder()
                .id(5L)
                .event(event)
                .ticketType(paidTicketType)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .ticketQuantity(1L)
                .status(EventRegistrationStatusEnum.PENDING)
                .build();

        when(eventRegistrationRepository.findById(5L)).thenReturn(Optional.of(reg));
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessPaymentEvent eventMsg = ProcessPaymentEvent.builder()
                .eventRegistrationId(5L)
                .attendeeEmail("jane@example.com")
                .attendeeName("Jane Doe")
                .paymentRequest(paymentRequest)
                .build();

        eventRegistrationService.paymentCompletedListener(eventMsg);

        verify(ticketRepository, times(1)).saveAll(any());
        verify(sqsClient, times(1)).sendMessage(any(Consumer.class));
    }
}
