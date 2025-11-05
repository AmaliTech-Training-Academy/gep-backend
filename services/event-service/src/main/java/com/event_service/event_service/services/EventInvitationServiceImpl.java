package com.event_service.event_service.services;

import com.event_service.event_service.client.AuthServiceClient;
import com.event_service.event_service.dto.*;
import com.event_service.event_service.event.EventInvitationEvent;
import com.event_service.event_service.exceptions.*;
import com.event_service.event_service.mappers.EventInvitationMapper;
import com.event_service.event_service.models.*;
import com.event_service.event_service.models.enums.InvitationStatus;
import com.event_service.event_service.models.enums.InviteStatus;
import com.event_service.event_service.repositories.EventInvitationRepository;
import com.event_service.event_service.repositories.EventInviteeRepository;
import com.event_service.event_service.repositories.EventOrganizerRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.utilities.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
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
    private final EventInviteeRepository eventInviteeRepository;
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

        EventInvitation invitation = createInvitation(request, event, currentUser);
        EventInvitation savedInvitation = eventInvitationRepository.save(invitation);

        for(InviteeRequest invitee: request.invitees()){
            validateInvitationDoesNotExist(request.event(), invitee.inviteeEmail());
            EventInvitee eventInvitee = createEventInvitee(invitee, savedInvitation);
            EventInvitee savedEventInvitee = eventInviteeRepository.save(eventInvitee);
            if(invitation.getStatus() == InvitationStatus.SEND) {
                publishInvitationEmail(savedEventInvitee);
            }
        }
    }

    @Override
    @Transactional
    public void acceptInvitation(EventInvitationAcceptanceRequest acceptanceRequest) {
        EventInvitee eventInvitation = getInvitationByToken(acceptanceRequest.invitationCode());
        validateInvitation(eventInvitation);
        createInviteeAccount(acceptanceRequest, eventInvitation);
        eventInvitation.setStatus(InviteStatus.ACCEPTED);
    }

    private void validateInvitation(EventInvitee eventInvitation) {
        if(eventInvitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidInvitationException("Invitation has expired");
        }

        if(eventInvitation.getStatus() == InviteStatus.ACCEPTED) {
            throw new InvalidInvitationException("Invite has already been accepted");
        }
    }

    private void createInviteeAccount(EventInvitationAcceptanceRequest request, EventInvitee eventInvitation){
        InviteeRegistrationRequest registrationRequest = new InviteeRegistrationRequest(
                request.fullName(),
                eventInvitation.getInviteeEmail(),
                request.password(),
                String.valueOf(eventInvitation.getRole())
        );
        UserResponse createdUser = authServiceClient.createUser(registrationRequest);

        EventOrganizer eventOrganizer = EventOrganizer.builder()
                .event(eventInvitation.getInvitation().getEvent())
                .userId(createdUser.id())
                .role(eventInvitation.getRole())
                .invitedBy(eventInvitation.getInvitation().getInviterId())
                .build();

        eventOrganizerRepository.save(eventOrganizer);

    }

    private EventInvitee getInvitationByToken(String token){
        return eventInviteeRepository.findByInvitationToken(token)
                .orElseThrow(() -> {
                    log.error("Invitation not found with token: {}", token);
                    return new EventNotFoundException("Invitation not found");
                });
    }

    @Override
    @Transactional
    public void resendInvitation(Long invitationId) {
        EventInvitee invitation = eventInviteeRepository.findById(invitationId)
                .orElseThrow(() -> new EventNotFoundException("Invitation not found"));

        if (!Objects.equals(invitation.getInvitation().getInviterId(), securityUtils.getCurrentUser().getId())) {
            throw new AuthorizationDeniedException("You are not authorized to resend this invitation");
        }

        if(invitation.getStatus() == InviteStatus.ACCEPTED) {
            log.warn("Attempt to resend already accepted invitation ID: {}", invitationId);
            throw new DuplicateInvitationException("Cannot resend an already accepted invitation");
        }
        invitation.setInvitationToken(generateInvitationToken());
        invitation.setExpiresAt(calculateExpirationTime());
        invitation.setStatus(InviteStatus.PENDING);
        eventInviteeRepository.save(invitation);

        publishInvitationEmail(invitation);
    }

    @Override
    public Page<EventInvitationListResponse> getInvitationList(Pageable pageable) {
        return null;
    }

    @Override
    public Page<EventInvitationListResponse> getSavedInvitations(Pageable pageable) {
        return eventInvitationRepository.findAllByStatus(InvitationStatus.SAVE, pageable)
                .map(eventInvitationMapper::toEventInvitationList);
    }

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new EventNotFoundException("Event not found");
                });
    }

    private void validateInvitationDoesNotExist(Long eventId, String inviteeEmail) {
        if (eventInviteeRepository.existsByEmailAndEventId(inviteeEmail, eventId)) {
            log.warn("Duplicate invitation attempt for event ID: {} and email: {}", eventId, inviteeEmail);
            throw new DuplicateInvitationException(
                    String.format("An invitation already exists for email '%s' for this event",
                            inviteeEmail)
            );
        }
    }

    private EventInvitation createInvitation(EventInvitationRequest request, Event event, AppUser currentUser) {
        return EventInvitation.builder()
                .event(event)
                .invitationTitle(request.invitationTitle())
                .inviterId(currentUser.getId())
                .inviterName(currentUser.getFullName())
                .message(request.message())
                .status(request.status())
                .build();
    }

    private EventInvitee createEventInvitee(InviteeRequest request, EventInvitation invitation) {
        return EventInvitee.builder()
                .inviteeName(request.inviteeName())
                .inviteeEmail(request.inviteeEmail())
                .invitationToken(generateInvitationToken())
                .role(request.role())
                .invitation(invitation)
                .status(InviteStatus.PENDING)
                .expiresAt(calculateExpirationTime())
                .build();
    }

    private void publishInvitationEmail(EventInvitee invitation) {
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
            throw new InvitationPublishException("Failed to serialize invitation event");
        } catch (Exception e) {
            log.error("Failed to publish invitation email event to SQS for email: {}",
                    invitation.getInviteeEmail(), e);
            throw new InvitationPublishException("Failed to publish invitation email event");
        }
    }

    private String buildInvitationLink(String token) {
        return String.format("%s/invitations/accept?token=%s", frontendBaseUrl, token);
    }

    private EventInvitationEvent createInvitationEvent(EventInvitee invitation, String inviteLink) {
        return new EventInvitationEvent(
                invitation.getInvitation().getInvitationTitle(),
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
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (eventInviteeRepository.findByInvitationToken(token).isPresent());
        return token;
    }

    private LocalDateTime calculateExpirationTime() {
        return LocalDateTime.now().plusDays(INVITATION_EXPIRATION_DAYS);
    }
}