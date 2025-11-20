package com.event_service.event_service.services;

import com.event_service.event_service.dto.TicketVerificationResponse;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.Ticket;
import com.event_service.event_service.models.TicketType;
import com.event_service.event_service.models.enums.TicketStatusEnum;
import com.event_service.event_service.repositories.TicketRepository;
import com.example.common_libraries.exception.BadRequestException;
import com.example.common_libraries.exception.ResourceNotFoundException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket activeTicket;

    @BeforeEach
    void setup() {
        TicketType type = new TicketType();
        type.setType("VIP");
        type.setPrice(200.0);

        Event event = new Event();
        event.setZoomMeetingLink("https://zoom.us/meeting/test123");
        event.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));

        activeTicket = new Ticket();
        activeTicket.setTicketCode("ABC123");
        activeTicket.setStatus(TicketStatusEnum.ACTIVE);
        activeTicket.setTicketType(type);
        activeTicket.setEvent(event);
    }

    // ----------------------------------------------------------------------
    // verifyTicket()
    // ----------------------------------------------------------------------
    @Nested
    @DisplayName("verifyTicket()")
    class VerifyTicketTests {

        @Test
        @DisplayName("should verify active ticket successfully and set status to USED")
        void shouldVerifyActiveTicketSuccessfully() {
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            LocalDateTime before = LocalDateTime.now();
            TicketVerificationResponse response = ticketService.verifyTicket("ABC123");
            LocalDateTime after = LocalDateTime.now();

            assertNotNull(response);
            assertEquals("ABC123", response.code());
            assertEquals("VIP", response.ticketType());
            assertEquals(200.0, response.price());
            assertEquals("Ticket verified successfully ✅", response.message());

            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(captor.capture());
            Ticket saved = captor.getValue();

            assertEquals(TicketStatusEnum.USED, saved.getStatus());
            assertTrue(saved.getCheckedInAt().isAfter(before.minusSeconds(1)));
            assertTrue(saved.getCheckedInAt().isBefore(after.plusSeconds(1)));
        }

        @Test
        @DisplayName("should throw when ticket not found")
        void shouldThrowWhenTicketNotFound() {
            when(ticketRepository.findByTicketCode("MISSING")).thenReturn(null);

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            ticketService.verifyTicket("MISSING")
                    );

            assertEquals("Ticket not found", ex.getMessage());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when ticket is not ACTIVE")
        void shouldThrowWhenTicketNotActive() {
            activeTicket.setStatus(TicketStatusEnum.CANCELLED);
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            ticketService.verifyTicket("ABC123")
                    );

            assertTrue(ex.getMessage().contains("Ticket is not active"));
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw if ticketType is missing")
        void shouldThrowIfTicketTypeMissing() {
            activeTicket.setTicketType(null);
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            assertThrows(NullPointerException.class, () ->
                    ticketService.verifyTicket("ABC123")
            );
        }
    }


    // ----------------------------------------------------------------------
    // validateAndGetMeetingUrl()
    // ----------------------------------------------------------------------
    @Nested
    @DisplayName("validateAndGetMeetingUrl()")
    class ValidateAndGetMeetingUrlTests {

        @Test
        @DisplayName("should return meeting URL and set ticket to USED for valid ticket")
        void shouldReturnMeetingUrlForValidTicket() {
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            String url = ticketService.validateAndGetMeetingUrl("ABC123");

            assertEquals("https://zoom.us/meeting/test123", url);

            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(captor.capture());
            assertEquals(TicketStatusEnum.USED, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("should throw BadRequestException for blank ticket code")
        void shouldThrowForBlankCode() {
            assertThrows(BadRequestException.class, () ->
                    ticketService.validateAndGetMeetingUrl(" ")
            );
        }

        @Test
        @DisplayName("should throw when ticket not found")
        void shouldThrowWhenTicketNotFound() {
            when(ticketRepository.findByTicketCode("X")).thenReturn(null);

            assertThrows(ResourceNotFoundException.class, () ->
                    ticketService.validateAndGetMeetingUrl("X")
            );
        }

        @Test
        @DisplayName("should throw when ticket is not ACTIVE")
        void shouldThrowWhenTicketNotActive() {
            activeTicket.setStatus(TicketStatusEnum.CANCELLED);
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            BadRequestException ex = assertThrows(BadRequestException.class, () ->
                    ticketService.validateAndGetMeetingUrl("ABC123")
            );

            assertTrue(ex.getMessage().contains("invalid"));
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set ticket to EXPIRED if event has ended, then throw exception")
        void shouldExpireTicketIfEventEnded() {
            activeTicket.getEvent().setEndTime(Instant.now().minus(1, ChronoUnit.HOURS));
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            BadRequestException ex = assertThrows(BadRequestException.class, () ->
                    ticketService.validateAndGetMeetingUrl("ABC123")
            );

            assertTrue(ex.getMessage().contains("Event has ended"));

            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(captor.capture());
            assertEquals(TicketStatusEnum.EXPIRED, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("should return empty string if event has no meeting link")
        void shouldReturnEmptyIfNoMeetingLink() {
            activeTicket.getEvent().setZoomMeetingLink(null);

            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            String result = ticketService.validateAndGetMeetingUrl("ABC123");

            assertEquals("", result);
        }

        @Test
        @DisplayName("should return empty string if event is missing")
        void shouldReturnEmptyIfEventMissing() {
            activeTicket.setEvent(null);
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            String result = ticketService.validateAndGetMeetingUrl("ABC123");

            assertEquals("", result);
        }
    }
}
