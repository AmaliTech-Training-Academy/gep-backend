package com.event_service.event_service.services;

import com.event_service.event_service.client.UserServiceClient;
import com.event_service.event_service.dto.*;
import com.event_service.event_service.mappers.EventInvitationMapper;
import com.event_service.event_service.models.*;
import com.event_service.event_service.models.enums.InvitationStatus;
import com.event_service.event_service.models.enums.InviteStatus;
import com.event_service.event_service.models.enums.InviteeRole;
import com.event_service.event_service.repositories.EventInvitationRepository;
import com.event_service.event_service.repositories.EventInviteeRepository;
import com.event_service.event_service.repositories.EventOrganizerRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.specifications.EventInviteeSpecification;
import com.event_service.event_service.utils.SecurityUtils;
import com.example.common_libraries.dto.UserCreationResponse;
import com.example.common_libraries.dto.AppUser;
import com.example.common_libraries.dto.queue_events.EventInvitationEvent;
import com.example.common_libraries.exception.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    private final UserServiceClient userServiceClient;


    private static final long INVITATION_EXPIRATION_DAYS = 2;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${aws.sqs.event-invitation-queue-url}")
    private String invitationQueueUrl;

    @Override
    @Transactional
    public void sendEventInvitation(EventInvitationRequest request) {

        AppUser currentUser = securityUtils.getCurrentUser();
        Event event = findEventOrThrow(request.event(), currentUser.id());

        EventInvitation invitation = createInvitation(request, event, currentUser);
        EventInvitation savedInvitation = eventInvitationRepository.save(invitation);

        for(InviteeRequest invitee: request.invitees()){
            validateInvitationDoesNotExist(request.event(), invitee.inviteeEmail());
            EventInvitee eventInvitee = createEventInvitee(invitee, savedInvitation, invitation.getStatus());
            EventInvitee savedEventInvitee = eventInviteeRepository.save(eventInvitee);
            if(invitation.getStatus() == InvitationStatus.SEND) {
                publishInvitationEmail(savedEventInvitee);
            }
        }
    }

    @Override
    @Transactional
    public void deleteEventInvitation(Long invitationId) {
        EventInvitation invitation = eventInvitationRepository.findById(invitationId).orElseThrow(()-> new ResourceNotFoundException("Invitation not found"));
        if (!Objects.equals(invitation.getInviterId(), securityUtils.getCurrentUser().id())) {
            throw new AuthorizationDeniedException("You are not authorized to delete this invitation");
        }
        eventInvitationRepository.delete(invitation);
    }

    @Override
    @Transactional
    public void updateEventInvitation(Long invitationId, EventInvitationRequest request){
        EventInvitation invitation = eventInvitationRepository.findById(invitationId).orElseThrow(()-> new ResourceNotFoundException("Invitation not found"));
        if (!Objects.equals(invitation.getInviterId(), securityUtils.getCurrentUser().id())) {
            throw new AuthorizationDeniedException("You are not authorized to update this invitation");
        }

        if (request.invitationTitle() != null) {
            invitation.setInvitationTitle(request.invitationTitle());
        }

        if (request.message() != null) {
            invitation.setMessage(request.message());
        }

        if (request.status() != null) {
            invitation.setStatus(request.status());
        }

        updateInvitees(invitation, request.invitees());


    }

    private void updateInvitees(EventInvitation invitation, List<InviteeRequest> inviteeDtos){
        List<EventInvitee> existingInvitees = invitation.getInvitees();

        for (InviteeRequest inviteeDto : inviteeDtos) {
            EventInvitee matchingInvitee = existingInvitees.stream()
                    .filter(e -> e.getInviteeEmail().equals(inviteeDto.inviteeEmail()))
                    .findFirst()
                    .orElse(null);

            if (matchingInvitee != null) {
                if (inviteeDto.inviteeName() != null) {
                    matchingInvitee.setInviteeName(inviteeDto.inviteeName());
                }
                if (inviteeDto.role() != null) {
                    matchingInvitee.setRole(inviteeDto.role());
                }
                if(invitation.getStatus() == InvitationStatus.SEND) {
                    matchingInvitee.setInvitationToken(generateInvitationToken());
                    matchingInvitee.setExpiresAt(calculateExpirationTime());
                    publishInvitationEmail(matchingInvitee);
                }
            } else {
                EventInvitee newInvitee = createEventInvitee(inviteeDto, invitation, invitation.getStatus());
                eventInviteeRepository.save(newInvitee);
                if(invitation.getStatus() == InvitationStatus.SEND) {
                    publishInvitationEmail(newInvitee);
                }
            }
        }
    }

    @Override
    public EventInvitationDetailsResponse getEventInvitationDetail(Long invitationId) {
        AppUser currentUser = securityUtils.getCurrentUser();
        EventInvitation invitation = eventInvitationRepository.findByIdAndInviterIdWithInvitees(invitationId, currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        return eventInvitationMapper.mapToDetailsResponse(invitation);
    }

    @Override
    @Transactional
    public void acceptInvitation(EventInvitationAcceptanceRequest acceptanceRequest) {
        EventInvitee eventInvitation = getInvitationByToken(acceptanceRequest.invitationCode());
        validateInvitation(eventInvitation);
        createInviteeAccount(acceptanceRequest, eventInvitation);
        eventInvitation.setStatus(InviteStatus.ACCEPTED);
        eventInviteeRepository.save(eventInvitation);
    }

    @Override
    public void acceptInvitationForExistingUser(String token) {
        EventInvitee eventInvitation = getInvitationByToken(token);
        validateInvitation(eventInvitation);
        UserCreationResponse createdUser = userServiceClient.checkUserExists(eventInvitation.getInviteeEmail());
        if(createdUser == null){
            throw new BadRequestException("User does not exist");
        }
        EventOrganizer eventOrganizer = EventOrganizer.builder()
                .event(eventInvitation.getInvitation().getEvent())
                .userId(createdUser.id())
                .role(eventInvitation.getRole())
                .invitedBy(eventInvitation.getInvitation().getInviterId())
                .build();

        eventOrganizerRepository.save(eventOrganizer);
        eventInvitation.setStatus(InviteStatus.ACCEPTED);
        eventInviteeRepository.save(eventInvitation);
    }

    private void validateInvitation(EventInvitee eventInvitation) {
        if(eventInvitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invitation has expired");
        }

        if(eventInvitation.getStatus() == InviteStatus.ACCEPTED) {
            throw new BadRequestException("Invite has already been accepted");
        }
    }

    private void createInviteeAccount(EventInvitationAcceptanceRequest request, EventInvitee eventInvitation){
        InviteeRegistrationRequest registrationRequest = new InviteeRegistrationRequest(
                request.fullName(),
                eventInvitation.getInviteeEmail(),
                request.password(),
                String.valueOf(eventInvitation.getRole())
        );
        UserCreationResponse createdUser = userServiceClient.createUser(registrationRequest);

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
                    return new ResourceNotFoundException("Invitation not found");
                });
    }

    @Override
    @Transactional
    public void resendInvitation(Long invitationId) {
        EventInvitee invitation = eventInviteeRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!Objects.equals(invitation.getInvitation().getInviterId(), securityUtils.getCurrentUser().id())) {
            throw new UnauthorizedException("You are not authorized to resend this invitation");
        }

        if(invitation.getStatus() == InviteStatus.ACCEPTED) {
            log.warn("Attempt to resend already accepted invitation ID: {}", invitationId);
            throw new DuplicateResourceException("Cannot resend an already accepted invitation");
        }
        invitation.setInvitationToken(generateInvitationToken());
        invitation.setExpiresAt(calculateExpirationTime());
        invitation.setStatus(InviteStatus.PENDING);
        eventInviteeRepository.save(invitation);

        publishInvitationEmail(invitation);
    }

    @Override
    public Page<EventInvitationListResponse> getInvitationList(Pageable pageable, String search) {
        AppUser currentUser = securityUtils.getCurrentUser();
        if(search != null && !search.trim().isEmpty()) {
            return eventInvitationRepository.findAllBySearchTermAndInviterId(
                    search.trim().toLowerCase(),
                    currentUser.id(),
                    pageable
            ).map(eventInvitationMapper::toEventInvitationList);
        }
        return eventInvitationRepository.findAllByInviterId(currentUser.id(),pageable).map(eventInvitationMapper::toEventInvitationList);
    }

    @Override
    public Page<EventInvitationListResponse> getSavedInvitations(Pageable pageable, String search) {
        AppUser currentUser = securityUtils.getCurrentUser();
        if(search != null && !search.trim().isEmpty()) {
            return eventInvitationRepository.findAllByStatusAndSearchTermAndInviterId(
                    InvitationStatus.SAVE,
                    search.trim().toLowerCase(),
                    currentUser.id(),
                    pageable
            ).map(eventInvitationMapper::toEventInvitationList);
        }
        return eventInvitationRepository.findAllByStatusAndInviterId(InvitationStatus.SAVE, currentUser.id(), pageable)
                .map(eventInvitationMapper::toEventInvitationList);
    }

    @Override
    public Page<EventInviteeResponse> getInviteeList(Long eventId, int page, String keyword, InviteeRole role, LocalDate date) {
        AppUser currentUser = securityUtils.getCurrentUser();

        Event event;
        if(!currentUser.role().equals("ADMIN")){
            event = eventRepository.findByIdAndUserId(eventId,currentUser.id()).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        }else{
            event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        }

        page = Math.max(page, 0);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 10, sort);

        Specification<EventInvitee> spec;

        spec = Specification.allOf(
                EventInviteeSpecification.belongsToEvent(event),
                EventInviteeSpecification.hasKeyword(keyword.trim().toLowerCase()),
                EventInviteeSpecification.hasRole(role),
                EventInviteeSpecification.hasDateCreated(date)
        );

        Page<EventInvitee> invitees = eventInviteeRepository.findAll(spec, pageable);

        return invitees
                .map(invitee -> EventInviteeResponse
                        .builder()
                        .id(invitee.getId())
                        .inviteeName(invitee.getInviteeName())
                        .inviteeEmail(invitee.getInviteeEmail())
                        .role(invitee.getRole())
                        .build()
                );
    }

    private Event findEventOrThrow(Long eventId, Long userId) {
        return eventRepository.findByIdAndUserId(eventId, userId).orElseThrow(
                () -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new ResourceNotFoundException("Event not found");
                }
        );
    }

    private void validateInvitationDoesNotExist(Long eventId, String inviteeEmail) {
        if (eventInviteeRepository.existsByEmailAndEventId(inviteeEmail, eventId)) {
            log.warn("Duplicate invitation attempt for event ID: {} and email: {}", eventId, inviteeEmail);
            throw new DuplicateResourceException(
                    String.format("An invitation already exists for email '%s' for this event",
                            inviteeEmail)
            );
        }
    }

    private EventInvitation createInvitation(EventInvitationRequest request, Event event, AppUser currentUser) {
        return EventInvitation.builder()
                .event(event)
                .invitationTitle(request.invitationTitle())
                .inviterId(currentUser.id())
                .inviterName(currentUser.fullName())
                .message(request.message())
                .status(request.status())
                .build();
    }

    private EventInvitee createEventInvitee(InviteeRequest request, EventInvitation invitation, InvitationStatus status) {
        String invitationToken = status == InvitationStatus.SEND ? generateInvitationToken() : null;
        LocalDateTime expiresAt = status == InvitationStatus.SEND ? calculateExpirationTime() : null;
        return EventInvitee.builder()
                .inviteeName(request.inviteeName())
                .inviteeEmail(request.inviteeEmail())
                .invitationToken(invitationToken)
                .role(request.role())
                .invitation(invitation)
                .status(InviteStatus.PENDING)
                .expiresAt(expiresAt)
                .build();
    }

    private void publishInvitationEmail(EventInvitee invitation) {
        try {
            String token = null;
            if(invitation.getRole() == InviteeRole.ORGANISER || invitation.getRole() == InviteeRole.CO_ORGANIZER){
                token = invitation.getInvitationToken();
            }else{
                token = invitation.getInvitation().getEvent().getId().toString();
            }
            String inviteLink = buildInvitationLink(token, invitation.getRole(), invitation.getInviteeEmail());
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

    private String buildInvitationLink(String token, InviteeRole role, String email) {
        if(role == InviteeRole.ORGANISER || role == InviteeRole.CO_ORGANIZER) {
            UserCreationResponse user = userServiceClient.checkUserExists(email);
            if(user != null){
                return String.format("%s/auth/event-invitations/existing-user/accept?token=%s", frontendBaseUrl, token);
            }
            return String.format("%s/auth/event-invitations/accept?token=%s", frontendBaseUrl, token);
        } else {
            return String.format("%s/app/event/%s/", frontendBaseUrl, token);
        }
    }


    private EventInvitationEvent createInvitationEvent(EventInvitee invitation, String inviteLink) {
        return new EventInvitationEvent(
                invitation.getInvitation().getInvitationTitle(),
                invitation.getInviteeName(),
                invitation.getInviteeEmail(),
                inviteLink,
                invitation.getRole().toString()
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