package com.event_service.event_service.services;

import com.event_service.event_service.dto.TicketVerificationResponse;
import com.event_service.event_service.models.Event;
import com.example.common_libraries.exception.BadRequestException;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.event_service.event_service.models.Ticket;
import com.event_service.event_service.models.enums.TicketStatusEnum;
import com.event_service.event_service.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService{
    private final TicketRepository ticketRepository;


    /**
     * Verifies a ticket based on its ticket code.
     *
     * @param ticketCode The unique code of the ticket to verify.
     * @return A response containing ticket verification details.
     * @throws ResourceNotFoundException if the ticket is not found or is not active.
     */
    @Override
    public TicketVerificationResponse verifyTicket(String ticketCode) {
        Ticket ticket = Optional.ofNullable(ticketRepository.findByTicketCode(ticketCode)).orElseThrow(()-> new ResourceNotFoundException("Ticket not found"));

        // Verify ticket
        if(ticket.getStatus() != TicketStatusEnum.ACTIVE){
            throw new ResourceNotFoundException("Ticket is not active, ticket status:" + ticket.getStatus());
        }

        // update status to used
        ticket.setStatus(TicketStatusEnum.USED);
        ticket.setCheckedInAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return TicketVerificationResponse
                .builder()
                .code(ticket.getTicketCode())
                .price(ticket.getTicketType().getPrice())
                .ticketType(ticket.getTicketType().getType())
                .message("Ticket verified successfully ✅")
                .build();
    }

    @Override
    public String validateAndGetMeetingUrl(String ticketCode) {
        if (ticketCode == null || ticketCode.isBlank()) {
            throw new BadRequestException("Invalid ticket code");
        }

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode);
        if (ticket == null) {
            throw new ResourceNotFoundException("Ticket not found");
        }

        if (ticket.getStatus() != TicketStatusEnum.ACTIVE) {
            throw new BadRequestException("Ticket already used, expired, or invalid");
        }

        Event event = ticket.getEvent();
        Instant now = Instant.now();

        // Event expired mark the ticket as expired
        if (event != null && event.getEndTime() != null && event.getEndTime().isBefore(now)) {
            ticket.setStatus(TicketStatusEnum.EXPIRED);
            ticket.setCheckedInAt(LocalDateTime.now());
            ticketRepository.save(ticket);

            throw new BadRequestException("Event has ended");
        }

        // mark ticket as used
        ticket.setStatus(TicketStatusEnum.USED);
        ticket.setCheckedInAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return (event == null || event.getZoomMeetingLink() == null)
                ? ""
                : event.getZoomMeetingLink();
    }
}
