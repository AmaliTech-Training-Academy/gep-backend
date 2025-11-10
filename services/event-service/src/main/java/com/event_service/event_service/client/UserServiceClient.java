package com.event_service.event_service.client;

import com.event_service.event_service.dto.InviteeRegistrationRequest;
import com.example.common_libraries.dto.TopOrganizerResponse;
import com.example.common_libraries.dto.UserCreationResponse;
import com.example.common_libraries.exception.DuplicateResourceException;
import com.example.common_libraries.exception.ForbiddenException;
import com.example.common_libraries.exception.ServiceCommunicationException;
import com.example.common_libraries.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Service
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder builder, @Value("${user.service.url}") String eventsServiceUrl) {
        this.webClient = builder.baseUrl(eventsServiceUrl).build();
    }


    public List<TopOrganizerResponse> getTopOrganizers(String accessToken) {
        try {
            log.info("Calling Organizer Service to get top organizers with token: {}", accessToken);
            List<TopOrganizerResponse> list = webClient.get()
                    .uri("/api/v1/users/top-organizers")
                    .cookie("accessToken", accessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TopOrganizerResponse>>() {})
                    .block();
            log.info("Top Organizers response succeeded");
            return list;

        } catch (WebClientResponseException.Unauthorized ex) {
            log.error("Unauthorized error when calling Organizer Service");
            throw new UnauthorizedException("Unauthorized: " + ex.getResponseBodyAsString());

        } catch (WebClientResponseException.Forbidden ex) {
            log.error("Forbidden error when calling Organizer Service");
            throw new ForbiddenException("Forbidden: " + ex.getResponseBodyAsString());

        } catch (WebClientResponseException ex) {
            log.error("Service communication error (status {}):", ex.getStatusCode());
            throw new ServiceCommunicationException(
                    "Service communication error (status " + ex.getStatusCode() + "): " + ex.getResponseBodyAsString()
            );

        } catch (Exception ex) {
            log.error("Unexpected error calling User Service", ex);
            throw new ServiceCommunicationException("Unexpected error calling User Service: " + ex.getMessage());
        }
    }

    public UserCreationResponse createUser(InviteeRegistrationRequest request) {
        log.info("Calling User Service to create user for email: {}", request.email());
        try {
            UserCreationResponse response =
                    webClient
                            .post()
                            .uri("/api/v1/auth/register-invitee")
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(UserCreationResponse.class)
                            .block();
            log.info("Created user response: {}", response);

            return response;
        } catch (WebClientResponseException e) {
            log.error("Error calling Auth Service: Status {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new DuplicateResourceException("User already exists with email: " + request.email());
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
