package com.event_service.event_service.client;

import com.event_service.event_service.dto.InviteeRegistrationRequest;
import com.event_service.event_service.dto.UserResponse;
import com.event_service.event_service.exceptions.ServiceCommunicationException;
import com.event_service.event_service.exceptions.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceClient {
    private final WebClient authServiceWebClient;

    public UserResponse createUser(InviteeRegistrationRequest request) {
        log.info("Calling Auth Service to create user for email: {}", request.email());

        try {
            UserResponse response =
                    authServiceWebClient
                    .post()
                    .uri("/api/v1/auth/register-invitee")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .block();
            log.info("Created user response: {}", response);

            return response;
        } catch (WebClientResponseException e) {
            log.error("Error calling Auth Service: Status {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new UserAlreadyExistsException("User already exists with email: " + request.email());
            }

            throw new ServiceCommunicationException
                    ("Failed to create user in Auth Service: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error calling Auth Service", e);
            throw new ServiceCommunicationException
                    ("Failed to communicate with Auth Service: " + e.getMessage());
        }

    }

}
