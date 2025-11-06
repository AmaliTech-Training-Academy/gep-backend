package com.event_service.event_service.services;

import com.event_service.event_service.client.AuthServiceClient;
import com.event_service.event_service.dto.*;
import com.event_service.event_service.mappers.EventInvitationMapper;
import com.event_service.event_service.models.AppUser;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventInvitation;
import com.event_service.event_service.models.EventOrganizer;
import com.event_service.event_service.models.enums.InviteStatus;
import com.event_service.event_service.repositories.EventInvitationRepository;
import com.event_service.event_service.repositories.EventOrganizerRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.utils.SecurityUtils;
import com.example.common_libraries.dto.queue_events.EventInvitationEvent;
import com.example.common_libraries.exception.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventInvitationServiceImpl implements EventInvitationService {

    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;
    private final SqsClient sqsClient;
    private final EventRepository eventRepository;
    private final EventInvitationRepository eventInvitationRepository;
    private final EventOrganizerRepository eventOrganizerRepository;
    private final EventInvitationMapper eventInvitationMapper;
    private final AuthServiceClient authServiceClient;


    private static final long INVITATION_EXPIRATION_DAYS = 2;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${aws.sqs.event-invitation-queue-url}")
    private String invitationQueueUrl;

    @Override
    @Transactional
    public void sendEventInvitation(EventInvitationRequest request) {

        AppUser currentUser = securityUtils.getCurrentUser();
        Event event = findEventOrThrow(request.event());

        validateInvitationDoesNotExist(request.event(), request.inviteeEmail());

        EventInvitation invitation = createInvitation(request, event, currentUser.getId());
        EventInvitation savedInvitation = eventInvitationRepository.save(invitation);

        publishInvitationEmail(savedInvitation);

    }

    @Override
    @Transactional
    public void acceptInvitation(EventInvitationAcceptanceRequest acceptanceRequest) {
        EventInvitation eventInvitation = getInvitationByToken(acceptanceRequest.invitationCode());
        validateInvitation(eventInvitation);
        createInviteeAccount(acceptanceRequest, eventInvitation);
        eventInvitation.setStatus(InviteStatus.ACCEPTED);
    }

    private void validateInvitation(EventInvitation eventInvitation) {
        if(eventInvitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invitation has expired");
        }

        if(eventInvitation.getStatus() == InviteStatus.ACCEPTED) {
            throw new BadRequestException("Invite has already been accepted");
        }
    }

    private void createInviteeAccount(EventInvitationAcceptanceRequest request, EventInvitation eventInvitation){
        InviteeRegistrationRequest registrationRequest = new InviteeRegistrationRequest(
                request.fullName(),
                eventInvitation.getInviteeEmail(),
                request.password(),
                String.valueOf(eventInvitation.getRole())
        );
        UserResponse createdUser = authServiceClient.createUser(registrationRequest);

        EventOrganizer eventOrganizer = EventOrganizer.builder()
                .event(eventInvitation.getEvent())
                .userId(createdUser.id())
                .role(eventInvitation.getRole())
                .invitedBy(eventInvitation.getInviterId())
                .invitation(eventInvitation)
                .build();

        eventOrganizerRepository.save(eventOrganizer);

    }


    private EventInvitation getInvitationByToken(String token){
        return eventInvitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> {
                    log.error("Invitation not found with token: {}", token);
                    return new ResourceNotFoundException("Invitation not found");
                });
    }


    @Override
    @Transactional
    public void resendInvitation(Long invitationId) {
        EventInvitation invitation = eventInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!Objects.equals(invitation.getInviterId(), securityUtils.getCurrentUser().getId())) {
            throw new BadRequestException("You are not authorized to resend this invitation");
        }

        if(invitation.getStatus() == InviteStatus.ACCEPTED) {
            log.warn("Attempt to resend already accepted invitation ID: {}", invitationId);
            throw new BadRequestException("Cannot resend an already accepted invitation");
        }
        invitation.setInvitationToken(generateInvitationToken());
        invitation.setExpiresAt(calculateExpirationTime());
        invitation.setStatus(InviteStatus.PENDING);
        eventInvitationRepository.save(invitation);

        publishInvitationEmail(invitation);
    }

    @Override
    public Page<EventInvitationListResponse> getInvitationList(Pageable pageable) {
        return eventInvitationRepository.findAll(pageable).map(eventInvitationMapper::toEventInvitationList);
    }

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new ResourceNotFoundException("Event not found");
                });
    }

    private void validateInvitationDoesNotExist(Long eventId, String inviteeEmail) {
        if (eventInvitationRepository.existsByEventIdAndInviteeEmail(eventId, inviteeEmail)) {
            log.warn("Duplicate invitation attempt for event ID: {} and email: {}", eventId, inviteeEmail);
            throw new DuplicateResourceException(
                    String.format("An invitation already exists for email '%s' for this event",
                            inviteeEmail)
            );
        }
    }

    private EventInvitation createInvitation(EventInvitationRequest request, Event event, Long inviterId) {
        return EventInvitation.builder()
                .event(event)
                .inviteeName(request.inviteeName())
                .invitationTitle(request.invitationTitle())
                .inviterId(inviterId)
                .invitationToken(generateInvitationToken())
                .role(request.role())
                .message(request.message())
                .inviteeEmail(request.inviteeEmail())
                .expiresAt(calculateExpirationTime())
                .build();
    }

    private void publishInvitationEmail(EventInvitation invitation) {
        try {
            String inviteLink = buildInvitationLink(invitation.getInvitationToken());
            EventInvitationEvent event = createInvitationEvent(invitation, inviteLink);
            String messageBody = serializeEvent(event);

            sendToSqs(messageBody);

            log.info("Invitation email event published successfully for token: {}",
                    invitation.getInvitationToken());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize invitation event for email: {}",
                    invitation.getInviteeEmail(), e);
            throw new BadRequestException("Failed to serialize invitation event");
        } catch (Exception e) {
            log.error("Failed to publish invitation email event to SQS for email: {}",
                    invitation.getInviteeEmail(), e);
            throw new BadRequestException("Failed to publish invitation email event");
        }
    }

    private String buildInvitationLink(String token) {
        return String.format("%s/invitations/accept?token=%s", frontendBaseUrl, token);
    }

    private EventInvitationEvent createInvitationEvent(EventInvitation invitation, String inviteLink) {
        return new EventInvitationEvent(
                invitation.getInvitationTitle(),
                invitation.getInviteeName(),
                invitation.getInviteeEmail(),
                inviteLink
        );
    }

    private String serializeEvent(EventInvitationEvent event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }

    private void sendToSqs(String messageBody) {
        log.debug("Sending message to SQS queue: {}", invitationQueueUrl);

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(invitationQueueUrl)
                .messageBody(messageBody)
                .build();

        sqsClient.sendMessage(sendMessageRequest);
    }

    private String generateInvitationToken() {
        return UUID.randomUUID().toString();
    }

    private LocalDateTime calculateExpirationTime() {
        return LocalDateTime.now().plusDays(INVITATION_EXPIRATION_DAYS);
    }
}