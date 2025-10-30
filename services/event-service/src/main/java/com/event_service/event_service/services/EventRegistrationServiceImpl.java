package com.event_service.event_service.services;

import com.event_service.event_service.dto.*;
import com.event_service.event_service.exceptions.ResourceNotFound;
import com.event_service.event_service.mappers.EventDetailMapper;
import com.event_service.event_service.mappers.TicketPurchasedEventMapper;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventRegistration;
import com.event_service.event_service.models.Ticket;
import com.event_service.event_service.models.TicketType;
import com.event_service.event_service.models.enums.EventRegistrationStatusEnum;
import com.event_service.event_service.repositories.EventRegistrationRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.repositories.TicketRepository;
import com.event_service.event_service.repositories.TicketTypeRepository;
import com.event_service.event_service.utilities.QRCodeGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventRegistrationServiceImpl implements EventRegistrationService{
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final SqsClient sqsClient;

    @Value("${sqs.ticket-purchased-event-queue-url}")
    private String ticketPurchasedEventQueueUrl;

    @Transactional
    @Override
    public String registerEvent(Long eventId, EventRegistrationRequest registrationRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(()-> new ResourceNotFound("Event not found"));
        TicketType ticketType = ticketTypeRepository.findById(registrationRequest.ticketTypeId()).orElseThrow(()-> new ResourceNotFound("Ticket Type not found"));

        EventRegistration registration = EventRegistration
                .builder()
                .event(event)
                .fullName(registrationRequest.fullName())
                .email(registrationRequest.email())
                .ticketType(ticketType)
                .ticketQuantity(registrationRequest.numberOfTickets())
                .status(EventRegistrationStatusEnum.PENDING)
                .build();

        // save registration
        eventRegistrationRepository.save(registration);

        // if a ticket type is free, save registration and then send tickets via email
        if(!ticketType.getIsPaid()){
            Long quantity = 1L;
            //generate ticket and send to attendee email
            List<Ticket> tickets = generateTicket(ticketType,event,quantity);
            TicketEventDetailResponse eventDetailResponse = EventDetailMapper.toTicketEventDetails(event);
            List<TicketResponse> ticketResponses = tickets.stream().map(TicketPurchasedEventMapper::toTicketResponse).toList();

            // Publish to queue for sending tickets to attendees
            TicketPurchasedEvent ticketPurchasedEvent = TicketPurchasedEventMapper
                    .toTicketPurchasedEvent(
                            registrationRequest.fullName(),
                            registrationRequest.email(),
                            ticketResponses,
                            eventDetailResponse
                    );

            publishTicketsPurchaseEventToQueue(ticketPurchasedEvent);
        }
        return "Event Registration Successful";
    }

    private List<Ticket> generateTicket(TicketType ticketType,Event event,Long quantity){
        List<Ticket> ticketsToBeGenerated = new java.util.ArrayList<>(List.of());

        try {
            for (int i = 0; i < quantity; i++) {
                String ticketCode = UUID.randomUUID().toString();

                String base64QRCode = QRCodeGenerator.generateQRCodeBase64(ticketCode, 300, 300);
                ticketsToBeGenerated.add(Ticket.builder()
                        .event(event)
                        .ticketType(ticketType)
                        .quantity(1)
                        .ticketCode(ticketCode)
                        .qrCodeUrl(base64QRCode)
                        .build());

            }
            // save tickets
            return ticketRepository.saveAll(ticketsToBeGenerated);
        } catch (WriterException e) {
            throw new ResourceNotFound("QR Code Generation Failed");
        } catch (IOException e){
            throw new ResourceNotFound("Ticket Generation Failed");
        }
    }

    private void publishTicketsPurchaseEventToQueue(TicketPurchasedEvent ticketPurchasedEvent){
        try{
            String messageBody = new ObjectMapper().writeValueAsString(ticketPurchasedEvent);
            sqsClient.sendMessage(builder -> builder.queueUrl(ticketPurchasedEventQueueUrl).messageBody(messageBody));
        }catch (Exception e){
            log.error("Error sending ticket purchase event to SQS: {}", e.getMessage());
        }
    }
}
