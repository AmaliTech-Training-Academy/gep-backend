package com.event_service.event_service.services;

import com.event_service.event_service.dto.TicketVerificationResponse;
import com.event_service.event_service.exceptions.ResourceNotFound;
import com.event_service.event_service.models.Ticket;
import com.event_service.event_service.models.enums.TicketStatusEnum;
import com.event_service.event_service.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService{
    private final TicketRepository ticketRepository;


    /**
     * Verifies a ticket based on its ticket code.
     *
     * @param ticketCode The unique code of the ticket to verify.
     * @return A response containing ticket verification details.
     * @throws ResourceNotFound if the ticket is not found or is not active.
     */
    @Override
    public TicketVerificationResponse verifyTicket(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode);
        if(ticket == null){
            throw new ResourceNotFound("Ticket not found");
        }

        // Verify ticket
        if(ticket.getStatus() != TicketStatusEnum.ACTIVE){
            throw new ResourceNotFound("Ticket is not active, ticket status:" + ticket.getStatus());
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
    public Boolean isTicketCodeValid(String ticketCode) {
        if(ticketCode.isBlank() || ticketCode.trim().isEmpty()){
            return false;
        }
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode);
        if(ticket == null){
            throw new ResourceNotFound("Ticket not found");
        }
        if(ticket.getStatus() != TicketStatusEnum.ACTIVE){
            return false;
        }

        // update status to used
        ticket.setStatus(TicketStatusEnum.USED);
        ticket.setCheckedInAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        return true;
    }

    @Override
    public String getMeetingUrl(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode);

        return ticket.getEvent().getZoomMeetingLink();
    }
}
