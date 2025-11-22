package com.event_service.event_service.services;

import com.event_service.event_service.client.PaymentServiceClient;
import com.event_service.event_service.dto.*;
import com.event_service.event_service.models.*;
import com.event_service.event_service.specifications.EventRegistrationSpecification;
import com.event_service.event_service.utils.SecurityUtils;
import com.example.common_libraries.dto.*;
import com.example.common_libraries.exception.BadRequestException;
import com.example.common_libraries.exception.InputOutputException;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.event_service.event_service.mappers.EventDetailMapper;
import com.event_service.event_service.mappers.EventMapper;
import com.event_service.event_service.mappers.TicketPurchasedEventMapper;
import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import com.event_service.event_service.models.enums.EventRegistrationStatusEnum;
import com.event_service.event_service.models.enums.TicketStatusEnum;
import com.event_service.event_service.repositories.EventRegistrationRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.repositories.TicketRepository;
import com.event_service.event_service.repositories.TicketTypeRepository;
import com.event_service.event_service.utils.QRCodeGenerator;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.common_libraries.dto.queue_events.TicketPurchasedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
    private final SecurityUtils securityUtils;
    private final PaymentServiceClient paymentServiceClient;
    private final EventDetailMapper eventDetailMapper;
    private final TicketPurchasedEventMapper ticketPurchasedEventMapper;

    @Value("${sqs.ticket-purchased-event-queue-url}")
    private String ticketPurchasedEventQueueUrl;

    @Value("${sqs.payment-processing-event-queue-url}")
    private String processPaymentQueueUrl;

    @Value("${application.alb.url}")
    private String albUrl;


    @Transactional
    @Override
    public EventRegistrationResponse registerEvent(Long eventId, EventRegistrationRequest registrationRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(()-> new ResourceNotFoundException("Event not found"));
        TicketType ticketType = ticketTypeRepository.findByIdAndEvent(registrationRequest.ticketTypeId(),event).orElseThrow(()-> new ResourceNotFoundException("Ticket Type not found"));
        Long quantity = registrationRequest.numberOfTickets();

        if(ticketType.getQuantity() - ticketType.getSoldCount() < quantity){
            throw new ResourceNotFoundException("Ticket Type is out of stock");
        }

        if(event.getEventMeetingType().getName() == EventMeetingTypeEnum.VIRTUAL && quantity > 1){
            throw new BadRequestException("Cannot buy more than one ticket for virtual events");
        }

        // Ensure a user is not buying more than quantity per attendee
        int maxPerAttendee = ticketType.getQuantityPerAttendee();

        Integer quantityPurchased = eventRegistrationRepository.sumTicketsByEventIdAndEmail(event, registrationRequest.email());
        if(quantity > maxPerAttendee|| quantityPurchased + quantity > maxPerAttendee){
            throw new BadRequestException("Cannot buy more than "+ maxPerAttendee +" tickets for this ticket type");
        }



        // if a ticket type is free, save registration and then send tickets via email
        if(Boolean.FALSE.equals(ticketType.getIsPaid())){
            quantity = 1L; // One ticket for free ticket types
            //generate ticket and send to attendee email
            List<Ticket> tickets = generateTicket(ticketType,event,quantity);
            TicketEventDetailResponse eventDetailResponse = eventDetailMapper.toTicketEventDetails(event);
            List<TicketResponse> ticketResponses = tickets.stream().map(ticket -> ticketPurchasedEventMapper.toTicketResponse(ticket)).toList();

            // Publish to queue for sending tickets to attendees
            TicketPurchasedEvent ticketPurchasedEvent = ticketPurchasedEventMapper
                    .toTicketPurchasedEvent(
                            registrationRequest.fullName(),
                            registrationRequest.email(),
                            ticketResponses,
                            eventDetailResponse
                    );

            publishTicketsPurchaseEventToQueue(ticketPurchasedEvent);

            EventRegistration registration = EventRegistration
                    .builder()
                    .event(event)
                    .fullName(registrationRequest.fullName())
                    .email(registrationRequest.email())
                    .ticketType(ticketType)
                    .ticketQuantity(registrationRequest.numberOfTickets())
                    .status(EventRegistrationStatusEnum.CONFIRMED)
                    .build();

            eventRegistrationRepository.save(registration);
        }else{
            // build process payment event
            ProcessPaymentEvent processPaymentEvent = ProcessPaymentEvent
                    .builder()
                    .amount(ticketType.getPrice() * quantity)
                    .ticketTypeId(ticketType.getId())
                    .numberOfTickets(quantity)
                    .fullName(registrationRequest.fullName())
                    .email(registrationRequest.email())
                    .eventRegistrationResponse(EventRegistrationResponse
                            .builder()
                            .id(event.getId())
                            .eventTitle(event.getTitle())
                            .location(event.getLocation())
                            .organizer(event.getCreatedBy())
                            .startDate(event.getStartTime())
                            .build()
                    )
                    .build();

            // receive payStack authorization URL
            PaystackResponse initializePayment = paymentServiceClient.initializeTransaction(processPaymentEvent);

            // return authorization url gotten from payment service
            return EventRegistrationResponse
                    .builder()
                    .id(event.getId())
                    .authorizationUrl(initializePayment.authorizationUrl())
                    .build();
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
                .id(event.getId())
                .eventTitle(eventResponse.title())
                .location(location)
                .organizer("Event Organizer")
                .startDate(eventResponse.startTime())
                .build();
    }

    @Override
    public Page<EventRegistrationsListResponse> getEventRegistrations(Long eventId, int page, String keyword, String ticketType) {
        AppUser currentUser = securityUtils.getCurrentUser();

        Event event;
        if(!currentUser.role().equals("ADMIN")){
            event = eventRepository.findByIdAndUserId(eventId, currentUser.id()).orElseThrow(()-> new ResourceNotFoundException("Event not found"));
        }else{
            event = eventRepository.findById(eventId).orElseThrow(()-> new ResourceNotFoundException("Event not found"));
        }

        page = Math.max(page, 0);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, 10, sort);
        Specification<EventRegistration> spec;

        spec = Specification.allOf(
                EventRegistrationSpecification.hasEvent(event.getId()),
                EventRegistrationSpecification.hasKeyword(keyword.trim()),
                EventRegistrationSpecification.hasTicketType(ticketType)
        );


        return eventRegistrationRepository.findAll(spec, pageable)
                .map(registration -> EventRegistrationsListResponse
                        .builder()
                        .id(registration.getId())
                        .name(registration.getFullName())
                        .email(registration.getEmail())
                        .numberOfTickets(registration.getTicketQuantity())
                        .ticketType(registration.getTicketType().getType())
                        .build());
    }

    @Override
    public EventRegistrationPageResponse getEventRegistrationPageOverview(Long eventId) {
        AppUser currentUser = securityUtils.getCurrentUser();

        Event event;
        if(!currentUser.role().equals("ADMIN")){
            event = eventRepository.findByIdAndUserId(eventId, currentUser.id()).orElseThrow(()-> new ResourceNotFoundException("Event not found"));
        }else{
            event = eventRepository.findById(eventId).orElseThrow(()-> new ResourceNotFoundException("Event not found"));
        }
        EventOptions eventOptions = Optional.ofNullable(event.getEventOptions()).orElse(EventOptions.builder().build());

        List<TicketTypeResponse> ticketTypes = Optional
                .ofNullable(event.getTicketTypes())
                .orElse(List.of())
                .stream()
                .map(type ->
                        TicketTypeResponse
                                .builder()
                                .id(type.getId())
                                .type(type.getType())
                                .description(type.getDescription())
                                .price(type.getPrice())
                                .isActive(type.getIsActive())
                                .remainingTickets(type.getQuantity() - type.getSoldCount())
                                .isPaid(type.getIsPaid())
                                .build()
                ).toList();
        List<String> filters = ticketTypes.stream().map(TicketTypeResponse::type).toList();
        Page<EventRegistrationsListResponse> registrationsListResponses = getEventRegistrations(eventId,0,"","");

        return EventRegistrationPageResponse
                .builder()
                .eventRegistrations(registrationsListResponses)
                .ticketTypes(ticketTypes)
                .filters(filters)
                .capacity(eventOptions.getCapacity())
                .build();
    }


    // SQS Listener to handle payment success messages from payment service
    @Transactional // This ensures there is an active hibernate session for lazy initialization fields
    @SqsListener("${sqs.payment-completed-event-queue-url}")
    public void paymentCompletedListener(ProcessPaymentEvent message){
        // Generate tickets and send to attendee via email
        Event event = eventRepository.findById(message.eventRegistrationResponse().id()).orElse(null);
        if(event == null){
            log.error("Event not found for payment completed event: {}", message.eventRegistrationResponse().id());
            return;
        }
        TicketType ticketType = ticketTypeRepository.findById(message.ticketTypeId()).orElse(null);
        if(ticketType == null){
            log.error("Ticket Type not found for payment completed event: {}", message.ticketTypeId());
            return;
        }
        EventRegistration registration = EventRegistration
                .builder()
                .event(event)
                .fullName(message.fullName())
                .email(message.email())
                .ticketType(ticketType)
                .ticketQuantity(message.numberOfTickets())
                .status(EventRegistrationStatusEnum.CONFIRMED)
                .build();

        eventRegistrationRepository.save(registration);

        TicketEventDetailResponse eventDetailResponse = eventDetailMapper.toTicketEventDetails(event);
        Long quantity;

        if(event.getEventMeetingType().getName() == EventMeetingTypeEnum.VIRTUAL ){
            quantity = 1L; // One ticket for virtual events
        }else{
            quantity = registration.getTicketQuantity();
        }

        List<TicketResponse> tickets = generateTicket(ticketType,event,quantity)
                .stream()
                .map(ticket -> ticketPurchasedEventMapper.toTicketResponse(ticket)).toList();

        // Publish to queue for sending tickets to attendees
        TicketPurchasedEvent ticketPurchasedEvent = ticketPurchasedEventMapper
                .toTicketPurchasedEvent(
                        registration.getFullName(),
                        registration.getEmail(),
                        tickets,
                        eventDetailResponse
                );

        publishTicketsPurchaseEventToQueue(ticketPurchasedEvent);
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
