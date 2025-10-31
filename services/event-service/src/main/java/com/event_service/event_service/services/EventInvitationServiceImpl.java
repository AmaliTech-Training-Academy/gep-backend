package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventInvitationRequest;
import com.event_service.event_service.event.EventInvitationEvent;
import com.event_service.event_service.exceptions.DuplicateInvitationException;
import com.event_service.event_service.exceptions.EventNotFoundException;
import com.event_service.event_service.exceptions.InvitationPublishException;
import com.event_service.event_service.models.AppUser;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventInvitation;
import com.event_service.event_service.repositories.EventInvitationRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.utilities.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.LocalDateTime;
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

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new EventNotFoundException("Event not found");
                });
    }

    private void validateInvitationDoesNotExist(Long eventId, String inviteeEmail) {
        if (eventInvitationRepository.existsByEventIdAndInviteeEmail(eventId, inviteeEmail)) {
            log.warn("Duplicate invitation attempt for event ID: {} and email: {}", eventId, inviteeEmail);
            throw new DuplicateInvitationException(
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