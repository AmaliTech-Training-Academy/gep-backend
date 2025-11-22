package com.example.payment_service.controller;

import com.example.common_libraries.dto.EventRegistrationResponse;
import com.example.common_libraries.dto.queue_events.PaymentStatusEvent;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.payment_service.models.PaymentRequestObject;
import com.example.payment_service.publisher.MessagePublisher;
import com.example.payment_service.repos.PaymentRequestObjectRepository;
import com.example.payment_service.repos.TransactionRepository;
import com.example.payment_service.dto.PaystackWebhook;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.models.TransactionStatus;
import com.example.payment_service.services.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final PaymentRequestObjectRepository paymentRequestObjectRepository;
    private final MessagePublisher messagePublisher;

    @Value("${paystack.secret}")
    private String paystackSecret;


    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Paystack-Signature") String signature) {

        try {
            String computedHash = hmacSha512(rawBody, paystackSecret);
            if (!computedHash.equals(signature)) {
                logger.warn("Invalid Paystack signature");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
            }
            ObjectMapper mapper = new ObjectMapper();
            PaystackWebhook webhook = mapper.readValue(rawBody, PaystackWebhook.class);

            if ("charge.success".equals(webhook.event())) {
                logger.info("Payment successful for reference {}", webhook.data().reference());

                Transaction transaction = transactionService.findByReference(webhook.data().reference());
                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setPaymentMethod(webhook.data().channel());

                // fetch payment request object
                PaymentRequestObject paymentRequestObject =
                        paymentRequestObjectRepository.findByTransaction(transaction);

                if(paymentRequestObject != null){
                    EventRegistrationResponse eventRegistrationResponse =
                            EventRegistrationResponse
                                    .builder()
                                    .id(paymentRequestObject.getEventId())
                                    .eventTitle(paymentRequestObject.getEventTitle())
                                    .location(paymentRequestObject.getLocation())
                                    .organizer(paymentRequestObject.getOrganizer())
                                    .startDate(paymentRequestObject.getStartDate())
                                    .authorizationUrl(transaction.getAuthorizationUrl())
                                    .build();

                    ProcessPaymentEvent processPaymentEvent =
                            ProcessPaymentEvent
                                    .builder()
                                    .amount(paymentRequestObject.getAmount())
                                    .ticketTypeId(paymentRequestObject.getTicketTypeId())
                                    .numberOfTickets(paymentRequestObject.getNumberOfTickets())
                                    .fullName(paymentRequestObject.getFullName())
                                    .email(paymentRequestObject.getEmail())
                                    .eventRegistrationResponse(eventRegistrationResponse)
                                    .build();

                    // Publish payment successful event to the notification service
                    PaymentStatusEvent statusEvent = PaymentStatusEvent
                            .builder()
                            .transactionId(transaction.getReference())
                            .email(transaction.getEmail())
                            .fullName(paymentRequestObject.getFullName())
                            .paymentMethod(transaction.getPaymentMethod())
                            .status(TransactionStatus.SUCCESS.name())
                            .amount(transaction.getAmount())
                            .timestamp(Instant.now())
                            .build();

                    messagePublisher.publishPaymentStatusToQueue(statusEvent);
                    // On successful payment send SQS message to the event service to generate tickets
                    messagePublisher.publishPaymentSuccessfulEventToQueue(processPaymentEvent);
                }

                transactionRepository.save(transaction);
            }else {
                Transaction transaction = transactionService.findByReference(webhook.data().reference());
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setPaymentMethod(webhook.data().channel());
                transactionRepository.save(transaction);

                // send a payment failed event to the notification service
                PaymentStatusEvent statusEvent = PaymentStatusEvent
                        .builder()
                        .transactionId(transaction.getReference())
                        .email(transaction.getEmail())
                        .fullName(transaction.getPaymentRequestObject().getFullName())
                        .paymentMethod(webhook.data().channel())
                        .status(TransactionStatus.FAILED.name())
                        .amount(transaction.getAmount())
                        .timestamp(Instant.now())
                        .build();

                messagePublisher.publishPaymentStatusToQueue(statusEvent);
                log.info("Payment failed for reference {}", webhook.data().reference());
            }
            return ResponseEntity.ok("Processed");
        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }



    private String hmacSha512(String data, String secret) throws Exception {
        Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512_HMAC.init(secretKey);
        byte[] hashBytes = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}


