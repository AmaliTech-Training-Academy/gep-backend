package com.event_service.event_service.services;

import com.event_service.event_service.dto.TicketVerificationResponse;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.Ticket;
import com.event_service.event_service.models.TicketType;
import com.event_service.event_service.models.enums.TicketStatusEnum;
import com.event_service.event_service.repositories.TicketRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
        TicketType ticketType = new TicketType();
        ticketType.setType("VIP");
        ticketType.setPrice(200.0);

        Event event = new Event();
        event.setZoomMeetingLink("https://zoom.us/meeting/test123");

        activeTicket = new Ticket();
        activeTicket.setTicketCode("ABC123");
        activeTicket.setStatus(TicketStatusEnum.ACTIVE);
        activeTicket.setTicketType(ticketType);
        activeTicket.setEvent(event);
    }

    // ------------------------------------------------------------------------
    // VERIFY TICKET TESTS
    // ------------------------------------------------------------------------
    @Nested
    @DisplayName("verifyTicket()")
    class VerifyTicketTests {

        @Test
        @DisplayName("should verify active ticket successfully and set status to USED with valid timestamp")
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

            // capture saved ticket
            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(captor.capture());
            Ticket saved = captor.getValue();

            // ensure ticket is marked used
            assertEquals(TicketStatusEnum.USED, saved.getStatus());

            // ensure checkedInAt timestamp is within expected range (1s tolerance)
            assertTrue(saved.getCheckedInAt().isAfter(before.minus(1, ChronoUnit.SECONDS)));
            assertTrue(saved.getCheckedInAt().isBefore(after.plus(1, ChronoUnit.SECONDS)));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when ticket is missing")
        void shouldThrowWhenTicketNotFound() {
            when(ticketRepository.findByTicketCode("MISSING")).thenReturn(null);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                    ticketService.verifyTicket("MISSING")
            );

            assertEquals("Ticket not found", ex.getMessage());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when ticket is not active")
        void shouldThrowWhenTicketNotActive() {
            activeTicket.setStatus(TicketStatusEnum.CANCELLED);
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                    ticketService.verifyTicket("ABC123")
            );

            assertTrue(ex.getMessage().contains("Ticket is not active"));
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw NullPointerException when ticketType is null")
        void shouldThrowWhenTicketTypeIsNull() {
            activeTicket.setTicketType(null);
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            assertThrows(NullPointerException.class, () ->
                    ticketService.verifyTicket("ABC123")
            );
        }
    }

    // ------------------------------------------------------------------------
    // IS TICKET CODE VALID TESTS
    // ------------------------------------------------------------------------
    @Nested
    @DisplayName("isTicketCodeValid()")
    class IsTicketCodeValidTests {

        @Test
        @DisplayName("should return true and update status to USED for valid ticket")
        void shouldReturnTrueForValidTicket() {
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            boolean result = ticketService.isTicketCodeValid("ABC123");

            assertTrue(result);

            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(captor.capture());
            Ticket updated = captor.getValue();
            assertEquals(TicketStatusEnum.USED, updated.getStatus());
            assertNotNull(updated.getCheckedInAt());
        }

        @Test
        @DisplayName("should return false for blank or empty ticket code")
        void shouldReturnFalseForBlankCode() {
            assertFalse(ticketService.isTicketCodeValid(" "));
            assertFalse(ticketService.isTicketCodeValid(""));
            verify(ticketRepository, never()).findByTicketCode(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for non-existent ticket")
        void shouldThrowWhenTicketNotFound() {
            when(ticketRepository.findByTicketCode("UNKNOWN")).thenReturn(null);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                    ticketService.isTicketCodeValid("UNKNOWN")
            );

            assertEquals("Ticket not found", ex.getMessage());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("should return false when ticket is inactive")
        void shouldReturnFalseWhenTicketInactive() {
            activeTicket.setStatus(TicketStatusEnum.CANCELLED);
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            boolean result = ticketService.isTicketCodeValid("ABC123");

            assertFalse(result);
            verify(ticketRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------------
    // GET MEETING URL TESTS
    // ------------------------------------------------------------------------
    @Nested
    @DisplayName("getMeetingUrl()")
    class GetMeetingUrlTests {

        @Test
        @DisplayName("should return valid zoom meeting link when ticket exists")
        void shouldReturnZoomLink() {
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            String link = ticketService.getMeetingUrl("ABC123");

            assertEquals("https://zoom.us/meeting/test123", link);
        }

        @Test
        @DisplayName("should throw NullPointerException if event is missing")
        void shouldThrowIfEventIsMissing() {
            activeTicket.setEvent(null);
            when(ticketRepository.findByTicketCode("ABC123")).thenReturn(activeTicket);

            assertThrows(NullPointerException.class, () ->
                    ticketService.getMeetingUrl("ABC123")
            );
        }
    }
}
