package com.moadams.notificationservice.utils;

import com.example.common_libraries.dto.queue_events.TicketPurchasedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
public class ICSGenerator {

    @Value("${events.virtual.ticket.verification.url}")
    private String virtualTicketVerificationUrl;

    public ByteArrayResource generateICS(TicketPurchasedEvent ticketPurchasedEvent) {
        try{
            Instant eventStartTime = ticketPurchasedEvent.eventDetails().startTime();
            Instant eventEndTime = ticketPurchasedEvent.eventDetails().endTime();

            // Use UTC format for better compatibility
            DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

            String verificationLink = String.format(
                    "%s?ticketCode=%s",
                    virtualTicketVerificationUrl,
                    ticketPurchasedEvent.tickets().getFirst().ticketCode()
            );

            // Build description with proper line breaks
            String description = "Join us for this virtual event!\\n\\n" +
                    escape(ticketPurchasedEvent.eventDetails().description()) + "\\n\\n" +
                    "Join Link: " + verificationLink;

            // Build the calendar
            StringBuilder ics = new StringBuilder();
            ics.append("BEGIN:VCALENDAR\r\n");
            ics.append("VERSION:2.0\r\n");
            ics.append("PRODID:-//EventHub//Virtual Event//EN\r\n");
            ics.append("CALSCALE:GREGORIAN\r\n");
            ics.append("METHOD:REQUEST\r\n");
            ics.append("BEGIN:VEVENT\r\n");

            // UID should include domain for uniqueness
            ics.append("UID:").append(UUID.randomUUID()).append("@eventhub.com\r\n");

            // DTSTAMP is current time in UTC
            ics.append("DTSTAMP:").append(utcFormatter.format(Instant.now())).append("\r\n");

            // Event start and end times in UTC (most compatible)
            ics.append("DTSTART:").append(utcFormatter.format(eventStartTime)).append("\r\n");
            ics.append("DTEND:").append(utcFormatter.format(eventEndTime)).append("\r\n");

            // Event details
            ics.append("SUMMARY:").append(escape(ticketPurchasedEvent.eventDetails().title())).append("\r\n");
            ics.append("DESCRIPTION:").append(description).append("\r\n");
            ics.append("LOCATION:Online Event\r\n");
            ics.append("URL:").append(verificationLink).append("\r\n");
            ics.append("STATUS:CONFIRMED\r\n");
            ics.append("SEQUENCE:0\r\n");

            ics.append("ORGANIZER;CN=EventHub:mailto:noreply.event.planner.amalitech@gmail.com\r\n");

            // Attendee
            ics.append("ATTENDEE;CN=").append(escape(ticketPurchasedEvent.attendeeName()))
                    .append(";RSVP=TRUE:mailto:").append(ticketPurchasedEvent.attendeeEmail()).append("\r\n");

            // Reminder 15 minutes before
            ics.append("BEGIN:VALARM\r\n");
            ics.append("TRIGGER:-PT15M\r\n");
            ics.append("ACTION:DISPLAY\r\n");
            ics.append("DESCRIPTION:Event starting in 15 minutes\r\n");
            ics.append("END:VALARM\r\n");

            ics.append("END:VEVENT\r\n");
            ics.append("END:VCALENDAR\r\n");

            byte[] icsBytes = ics.toString().getBytes(StandardCharsets.UTF_8);
            return new ByteArrayResource(icsBytes) {
                @Override
                public String getFilename() {
                    return "event.ics";
                }
            };
        }catch (Exception e){
            log.error("An Error occurred while generating ICS file: {}", e.getMessage());
            String fallbackContent = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nEND:VCALENDAR\r\n";
            byte[] fallbackBytes = fallbackContent.getBytes(StandardCharsets.UTF_8);
            return new ByteArrayResource(fallbackBytes) {
                @Override
                public String getFilename() {
                    return "event-fallback.ics";
                }
            };
        }
    }

    // Escape special characters for iCalendar compliance
    private String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("'", "\\'");
    }
}