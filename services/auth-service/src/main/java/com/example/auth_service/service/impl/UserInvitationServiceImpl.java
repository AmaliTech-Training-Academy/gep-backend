package com.example.auth_service.service.impl;

import com.example.auth_service.dto.base.UserRegistrationBase;
import com.example.auth_service.dto.request.BulkUserInvitationRequest;
import com.example.auth_service.dto.request.InvitationAcceptanceRequest;
import com.example.auth_service.dto.request.UserInvitationRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.InviteeDetailsResponse;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.model.User;
import com.example.auth_service.model.UserInvitation;
import com.example.auth_service.model.UserInvitee;
import com.example.auth_service.repository.UserInvitationRepository;
import com.example.auth_service.repository.UserInviteeRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.AuthUser;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.UserInvitationService;
import com.example.common_libraries.dto.UserCreationResponse;
import com.example.common_libraries.dto.queue_events.UserInvitedEvent;
import com.example.common_libraries.enums.InvitationStatus;
import com.example.common_libraries.enums.InviteStatus;
import com.example.common_libraries.exception.DuplicateResourceException;
import com.example.common_libraries.exception.InactiveAccountException;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserInvitationServiceImpl implements UserInvitationService {

    private final UserInviteeRepository userInviteeRepository;
    private final UserInvitationRepository userInvitationRepository;
    private final UserRepository userRepository;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private static final long INVITATION_EXPIRATION_DAYS = 2;
    private final AuthServiceImpl authServiceImpl;

    @Value("${sqs.user-invitation-queue}")
    private String userInvitationQueue;

    @Override
    @Transactional
    public void inviteBulkUsers(BulkUserInvitationRequest invitationRequest) {
        UserInvitation userInvitation = createUserInvitation(invitationRequest);
        userInvitationRepository.save(userInvitation);
        for(UserInvitationRequest invitee : invitationRequest.invitees()){
            if(!(invitee.role() == UserRole.ADMIN || invitee.role() == UserRole.ORGANISER)){
                throw new IllegalArgumentException("Invalid role for invitation. Only ADMIN and ORGANISER roles are allowed.");
            }
            validateUserEmailAndInvitationDoesNotExist(invitee.email());
            UserInvitee userInvitee = createUserInvitee(invitee, userInvitation, invitationRequest.status());
            userInviteeRepository.save(userInvitee);
            if(invitationRequest.status() == InvitationStatus.SEND){
                sendUserInvitationMessageToQueue(userInvitee, userInvitation.getMessage());
            }
        }
    }



    private UserInvitation createUserInvitation(BulkUserInvitationRequest invitationRequest) {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        User user = userRepository.findByEmail(authUser.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        return UserInvitation.builder()
                .inviter(user)
                .message(invitationRequest.message())
                .status(invitationRequest.status())
                .build();
    }

    private UserInvitee createUserInvitee(UserInvitationRequest request, UserInvitation userInvitation, InvitationStatus status) {
        String invitationToken = status == InvitationStatus.SEND ? generateInvitationToken() : null;
        LocalDate expiresAt = status == InvitationStatus.SEND ? calculateExpirationTime() : null;
        return UserInvitee.builder()
                .invitationToken(invitationToken)
                .fullName(request.fullName())
                .email(request.email().toLowerCase().trim())
                .role(request.role())
                .expiresAt(expiresAt)
                .invitation(userInvitation)
                .build();

    }

    private void validateUserEmailAndInvitationDoesNotExist(String email){
        if(userRepository.existsByEmail(email)){
            throw new DuplicateResourceException(String.format("A user already exists with this email '%s'",
                    email));
        }
        if(userInviteeRepository.existsByEmail(email)){
            throw new DuplicateResourceException(String.format("A user invitation already exists with this email '%s'",
                    email));
        }
    }


    private String generateInvitationToken() {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (userInviteeRepository.findByInvitationToken(token).isPresent());
        return token;
    }

    private LocalDate calculateExpirationTime() {
        return LocalDate.now().plusDays(INVITATION_EXPIRATION_DAYS);
    }

    private void sendUserInvitationMessageToQueue(UserInvitee invitee, String message){
        try{
            UserInvitedEvent event = new UserInvitedEvent(invitee.getFullName(), invitee.getEmail(), invitee.getRole().name(), invitee.getInvitationToken(), message);
            String messageBody = objectMapper.writeValueAsString(event);
            sqsClient.sendMessage(builder -> builder.queueUrl(userInvitationQueue).messageBody(messageBody));
        }catch (Exception e){
            log.error("Error user invitation sending message to SQS: {}", e.getMessage());
        }
    }

    @Override
    public InviteeDetailsResponse inviteeDetails(String token) {
        UserInvitee invitee = userInviteeRepository.findByInvitationToken(token).orElseThrow(
                () -> new ResourceNotFoundException("Invitation does not exist")
        );
        return new InviteeDetailsResponse(
                invitee.getFullName(),
                invitee.getEmail(),
                invitee.getInvitationToken()
        );
    }

    @Override
    @Transactional
    public AuthResponse acceptInvitation(InvitationAcceptanceRequest request) {
           UserInvitee invitation = getValidUserInvitationByToken(request.invitationToken());
           invitation.setStatus(InviteStatus.ACCEPTED);
           userInviteeRepository.save(invitation);
           UserRegistrationBase registrationData = setupRegistrationData(request, invitation);
           authServiceImpl.validateRequest(registrationData);
           User savedUser = authServiceImpl.createAndSaveUser(registrationData, invitation.getRole());
           authServiceImpl.createUserRelatedEntities(savedUser);
            return new AuthResponse(savedUser.getId(),savedUser.getEmail(), savedUser.getFullName(), savedUser.getProfile().getProfileImageUrl(), savedUser.getRole());
    }

    private UserRegistrationBase setupRegistrationData(InvitationAcceptanceRequest request, UserInvitee invitee){
        return new UserRegistrationBase() {
            @Override
            public String fullName() {
                return request.fullName();
            }

            @Override
            public String email() {
                return invitee.getEmail();
            }

            @Override
            public String password() {
                return request.password();
            }

            @Override
            public String confirmPassword() {
                return request.confirmPassword();
            }
        };
    }

    private UserInvitee getValidUserInvitationByToken(String token){
        UserInvitee invitee = userInviteeRepository.findByInvitationToken(token).orElseThrow(
                () -> new ResourceNotFoundException("Invitation does not exist")
        );
        if(invitee.getExpiresAt() != null && invitee.getExpiresAt().isBefore(LocalDate.now())){
            throw new InactiveAccountException("Invitation has expired");
        }

        if(invitee.getStatus().equals(InviteStatus.ACCEPTED)){
            throw new ResourceNotFoundException("Invitation has already been accepted");
        }
        return invitee;
    }
}
