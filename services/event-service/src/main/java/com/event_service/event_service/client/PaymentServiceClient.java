package com.event_service.event_service.client;

import com.example.common_libraries.dto.PaystackResponse;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.common_libraries.exception.ForbiddenException;
import com.example.common_libraries.exception.ServiceCommunicationException;
import com.example.common_libraries.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
public class PaymentServiceClient {
    private final WebClient webClient;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    public PaymentServiceClient(WebClient.Builder builder,@Value("${payment.service.url}") String paymentServiceUrl) {
        this.webClient = builder.baseUrl(paymentServiceUrl).build();
    }

    public PaystackResponse initializeTransaction(ProcessPaymentEvent paymentRequest){
        try{
            log.info("Calling Payment Service to initialize transaction");
            return webClient.post()
                    .uri("/api/v1/payment")
                    .bodyValue(paymentRequest)
                    .retrieve()
                    .bodyToMono(PaystackResponse.class)
                    .block();
        } catch (WebClientResponseException.Unauthorized ex) {
            log.error("Unauthorized error when calling Payment Service");
            throw new UnauthorizedException("Unauthorized: " + ex.getResponseBodyAsString());

        } catch (WebClientResponseException.Forbidden ex) {
            log.error("Forbidden error when calling Payment Service");
            throw new ForbiddenException("Forbidden: " + ex.getResponseBodyAsString());

        } catch (WebClientResponseException ex) {
            log.error("Service communication error (status {}):", ex.getStatusCode());
            throw new ServiceCommunicationException(
                    "Service communication error (status " + ex.getStatusCode() + "): " + ex.getResponseBodyAsString()
            );

        } catch (Exception ex) {
            log.error("Unexpected error calling Payment Service", ex);
            throw new ServiceCommunicationException("Unexpected error calling User Service: " + ex.getMessage());
        }

    }
}
