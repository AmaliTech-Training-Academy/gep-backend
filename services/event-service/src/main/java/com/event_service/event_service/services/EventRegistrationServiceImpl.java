package com.event_service.event_service.services;

import com.event_service.event_service.dto.*;
import com.example.common_libraries.exception.BadRequestException;
import com.example.common_libraries.exception.InputOutputException;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.event_service.event_service.mappers.EventDetailMapper;
import com.event_service.event_service.mappers.EventMapper;
import com.event_service.event_service.mappers.TicketPurchasedEventMapper;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventRegistration;
import com.event_service.event_service.models.Ticket;
import com.event_service.event_service.models.TicketType;
import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import com.event_service.event_service.models.enums.EventRegistrationStatusEnum;
import com.event_service.event_service.models.enums.TicketStatusEnum;
import com.event_service.event_service.repositories.EventRegistrationRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.repositories.TicketRepository;
import com.event_service.event_service.repositories.TicketTypeRepository;
import com.event_service.event_service.utils.QRCodeGenerator;
import com.example.common_libraries.dto.PaymentRequest;
import com.example.common_libraries.dto.TicketEventDetailResponse;
import com.example.common_libraries.dto.TicketResponse;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.common_libraries.dto.queue_events.TicketPurchasedEvent;
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
    private final ObjectMapper objectMapper;
    private final EventMapper eventMapper;

    @Value("${sqs.ticket-purchased-event-queue-url}")
    private String ticketPurchasedEventQueueUrl;

    @Value("${sqs.payment-processing-event-queue-url}")
    private String processPaymentQueueUrl;

    @Value("${application.alb.url}")
    private String albUrl;


    /**
     * Registers an event for a user.
     *
     * @param eventId             The ID of the event to register.
     * @param registrationRequest The registration request containing user details and ticket type.
     * @return A success message indicating the registration was successful.
     * @throws ResourceNotFoundException if the event or ticket type is not found, or if tickets are out of stock.
     */
    @Transactional
    @Override
    public EventRegistrationResponse registerEvent(Long eventId, EventRegistrationRequest registrationRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(()-> new ResourceNotFoundException("Event not found"));
        TicketType ticketType = ticketTypeRepository.findById(registrationRequest.ticketTypeId()).orElseThrow(()-> new ResourceNotFoundException("Ticket Type not found"));
        Long quantity = registrationRequest.numberOfTickets();

        if(ticketType.getQuantity() - ticketType.getSoldCount() < quantity){
            throw new ResourceNotFoundException("Ticket Type is out of stock");
        }

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
        if(Boolean.FALSE.equals(ticketType.getIsPaid())){
            quantity = 1L; // One ticket for free ticket types
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
        }else{
            // for paid events send a message to the payment service to process payment
            if(registrationRequest.paymentRequest() == null){
                throw new BadRequestException("Payment details is required for paid ticket types");
            }
            PaymentRequest paymentRequest = registrationRequest.paymentRequest();

            // build process payment event
            ProcessPaymentEvent processPaymentEvent = ProcessPaymentEvent
                    .builder()
                    .eventRegistrationId(registration.getId())
                    .attendeeEmail(registration.getEmail())
                    .attendeeName(registration.getFullName())
                    .paymentRequest(paymentRequest)
                    .build();
            publishProcessPaymentEventToQueue(processPaymentEvent);

            // Simulate successful payment for TEST purposes
            // TODO remove this when payment service is implemented
            paymentCompletedListener(processPaymentEvent);
        }
        EventResponse eventResponse = eventMapper.toResponse(event);

        String location;
        if(event.getEventMeetingType().getName() == EventMeetingTypeEnum.VIRTUAL){
            location = "Virtual via Zoom";
        }else{
            location = event.getLocation();
        }
        return EventRegistrationResponse
                .builder()
                .eventTitle(eventResponse.title())
                .location(location)
                .organizer("Event Organizer")
                .startDate(eventResponse.startTime())
                .build();
    }


    // SQS Listener to handle payment success messages
    //@SqsListener("${sqs.payment-completed-event-queue-url}")
    public void paymentCompletedListener(ProcessPaymentEvent message){
        // Generate tickets and send to attendee via email
        EventRegistration registration = eventRegistrationRepository.findById(message.eventRegistrationId()).orElse(null);
        if(registration != null){
            Long quantity;
            Event event = registration.getEvent();
            TicketType ticketType = registration.getTicketType();
            TicketEventDetailResponse eventDetailResponse = EventDetailMapper.toTicketEventDetails(event);

            if(event.getEventMeetingType().getName() == EventMeetingTypeEnum.VIRTUAL ){
                quantity = 1L; // One ticket for virtual events
            }else{
                quantity = registration.getTicketQuantity();
            }

            List<TicketResponse> tickets = generateTicket(ticketType,event,quantity)
                    .stream()
                    .map(TicketPurchasedEventMapper::toTicketResponse).toList();

            // Publish to queue for sending tickets to attendees
            TicketPurchasedEvent ticketPurchasedEvent = TicketPurchasedEventMapper
                    .toTicketPurchasedEvent(
                            registration.getFullName(),
                            registration.getEmail(),
                            tickets,
                            eventDetailResponse
                    );

            publishTicketsPurchaseEventToQueue(ticketPurchasedEvent);
        }
    }


    /**
     * Generates tickets for an event.
     *
     * @param ticketType The type of ticket to generate.
     * @param event The event for which tickets are being generated.
     * @param quantity The number of tickets to generate.
     * @return A list of generated tickets.
     * @throws ResourceNotFoundException if QR code generation or ticket saving fails.
     */
    private List<Ticket> generateTicket(TicketType ticketType,Event event,Long quantity){
        List<Ticket> ticketsToBeGenerated = new java.util.ArrayList<>(List.of());

        try {
            for (int i = 0; i < quantity; i++) {
                String ticketCode = UUID.randomUUID().toString();

                String ticketUrl = albUrl + "/api/v1/tickets/verify/" + ticketCode;

                String base64QRCode = QRCodeGenerator.generateQRCodeBase64(ticketUrl, 100, 100);
                ticketsToBeGenerated.add(Ticket.builder()
                        .event(event)
                        .ticketType(ticketType)
                        .quantity(1)
                        .ticketCode(ticketCode)
                        .qrCodeUrl(base64QRCode)
                        .status(TicketStatusEnum.ACTIVE)
                        .build());

            }
            // Update ticket type details
            ticketType.setSoldCount(ticketType.getSoldCount()+quantity);
            if(ticketType.getQuantity().longValue() == ticketType.getSoldCount().longValue()){
                ticketType.setIsActive(false);
            }
            ticketTypeRepository.save(ticketType);

            // save tickets
            return ticketRepository.saveAll(ticketsToBeGenerated);
        } catch (WriterException e) {
            throw new InputOutputException("QR Code Generation Failed");
        } catch (IOException e){
            throw new InputOutputException("Ticket Generation Failed");
        }
    }


    /**
     * Publishes a ticket purchase event to the SQS queue.
     *
     * @param ticketPurchasedEvent The ticket purchase event to publish.
     */
    private void publishTicketsPurchaseEventToQueue(TicketPurchasedEvent ticketPurchasedEvent){
        try{
            log.info("Sending ticket purchase event to SQS {}", ticketPurchasedEventQueueUrl);
            String messageBody = objectMapper.writeValueAsString(ticketPurchasedEvent);
            log.info("Ticket purchase event message body: {}", messageBody);
            sqsClient.sendMessage(builder -> builder.queueUrl(ticketPurchasedEventQueueUrl).messageBody(messageBody));
        }catch (Exception e){
            log.error("Error sending ticket purchase event to SQS: {}", e.getMessage());
        }
    }


    private void publishProcessPaymentEventToQueue(ProcessPaymentEvent processPaymentEvent) {
        try{
            log.info("Sending process payment event");
            String messageBody = objectMapper.writeValueAsString(processPaymentEvent);
            log.info("Process payment event message body: {}", messageBody);
            sqsClient.sendMessage(builder -> builder.queueUrl(processPaymentQueueUrl).messageBody(messageBody));
        }catch (Exception e){
            log.error("Error sending process payment event: {}", e.getMessage());
        }
    }
}
