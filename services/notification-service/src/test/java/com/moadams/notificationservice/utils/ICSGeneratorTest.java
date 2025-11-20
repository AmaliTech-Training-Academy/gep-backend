package com.moadams.notificationservice.utils;


import com.example.common_libraries.dto.TicketEventDetailResponse;
import com.example.common_libraries.dto.TicketResponse;
import com.example.common_libraries.dto.queue_events.TicketPurchasedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ICSGeneratorTest {

    private ICSGenerator icsGenerator;
    private TicketPurchasedEvent sampleEvent;

    @BeforeEach
    void setUp() {
        icsGenerator = new ICSGenerator();
        ReflectionTestUtils.setField(icsGenerator, "virtualTicketVerificationUrl", "https://eventhub.com/verify");

        var eventDetails = new TicketEventDetailResponse(
                1L,
                "Tech Summit 2025",
                "EventHub",
                "A virtual summit on modern software practices",
                Instant.parse("2025-12-10T10:00:00Z"),
                Instant.parse("2025-12-10T11:00:00Z"),
                "VIRTUAL",
                "UTC"
        );

        var ticket = new TicketResponse(1L, "VIP", "ABC123", "qr.png", "CONFIRMED");

        sampleEvent = new TicketPurchasedEvent(
                "John Doe",
                "john@example.com",
                List.of(ticket),
                eventDetails
        );
    }

    @Test
    @DisplayName("Should generate valid ICS file for a valid event")
    void generateICS_Success() {
        ByteArrayResource resource = icsGenerator.generateICS(sampleEvent);

        assertNotNull(resource);
        assertEquals("event.ics", resource.getFilename());

        String content = new String(resource.getByteArray(), StandardCharsets.UTF_8);

        // Core ICS structure
        assertTrue(content.contains("BEGIN:VCALENDAR"));
        assertTrue(content.contains("END:VCALENDAR"));
        assertTrue(content.contains("BEGIN:VEVENT"));
        assertTrue(content.contains("END:VEVENT"));
        assertTrue(content.contains("UID:"));
        assertTrue(content.contains("DTSTAMP:"));
        assertTrue(content.contains("DTSTART:20251210T100000Z"));
        assertTrue(content.contains("SUMMARY:Tech Summit 2025"));
        assertTrue(content.contains("LOCATION:Online Event"));
        assertTrue(content.contains("ATTENDEE;CN=John Doe;RSVP=TRUE:mailto:john@example.com"));
        assertTrue(content.contains("Join Link: https://eventhub.com/verify?ticketCode=ABC123"));
    }

    @Test
    @DisplayName("Should correctly escape special characters in text")
    void escape_SpecialCharactersHandled() {
        var details = new TicketEventDetailResponse(
                2L,
                "DevOps, Cloud; & 'Kubernetes'\nSession",
                "EventHub",
                "Line1\nLine2;Line3,End",
                Instant.parse("2025-12-10T10:00:00Z"),
                Instant.parse("2025-12-10T11:00:00Z"),
                "ONLINE",
                "UTC"
        );

        var ticket = new TicketResponse(2L, "Standard", "XYZ789", "qr.png", "CONFIRMED");
        var event = new TicketPurchasedEvent("Jane, Doe", "jane@example.com", List.of(ticket), details);

        ByteArrayResource resource = icsGenerator.generateICS(event);
        String content = new String(resource.getByteArray(), StandardCharsets.UTF_8);

        // Ensure escaped characters
        assertTrue(content.contains("DevOps\\, Cloud\\; & \\'Kubernetes\\'"));
        assertTrue(content.contains("Line1\\nLine2\\;Line3\\,End"));
        assertTrue(content.contains("Jane\\, Doe"));
    }

    @Test
    @DisplayName("Should return fallback ICS file when an error occurs during generation")
    void generateICS_ErrorFallback() {
        // Create event that triggers a NullPointerException by nulling eventDetails
        TicketPurchasedEvent badEvent = new TicketPurchasedEvent("Bad User", "bad@example.com", null, null);

        ByteArrayResource resource = icsGenerator.generateICS(badEvent);

        assertNotNull(resource);
        assertEquals("event-fallback.ics", resource.getFilename());

        String content = new String(resource.getByteArray(), StandardCharsets.UTF_8);
        assertTrue(content.contains("BEGIN:VCALENDAR"));
        assertTrue(content.contains("END:VCALENDAR"));
    }

    @Test
    @DisplayName("Should handle null or empty text safely in escape()")
    void escape_NullOrEmptyHandled() {
        String resultNull = ReflectionTestUtils.invokeMethod(icsGenerator, "escape", (String) null);
        String resultEmpty = ReflectionTestUtils.invokeMethod(icsGenerator, "escape", "");
        String resultNormal = ReflectionTestUtils.invokeMethod(icsGenerator, "escape", "Hello;World,Path\\Test");

        assertEquals("", resultNull);
        assertEquals("", resultEmpty);
        assertEquals("Hello\\;World\\,Path\\\\Test", resultNormal);
    }

    @Test
    @DisplayName("Generated ICS should include reminder and organizer info")
    void generateICS_ShouldContainReminderAndOrganizer() {
        ByteArrayResource resource = icsGenerator.generateICS(sampleEvent);
        String content = new String(resource.getByteArray(), StandardCharsets.UTF_8);

        assertTrue(content.contains("BEGIN:VALARM"));
        assertTrue(content.contains("TRIGGER:-PT15M"));
        assertTrue(content.contains("DESCRIPTION:Event starting in 15 minutes"));
        assertTrue(content.contains("ORGANIZER;CN=EventHub:mailto:noreply.event.planner.amalitech@gmail.com"));
    }
}
