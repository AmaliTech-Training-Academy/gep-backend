package com.example.payment_service.controller;

import com.example.common_libraries.dto.EventRegistrationResponse;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.payment_service.models.PaymentRequestObject;
import com.example.payment_service.repos.PaymentRequestObjectRepository;
import com.example.payment_service.repos.TransactionRepository;
import com.example.payment_service.dto.PaystackWebhook;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.models.TransactionStatus;
import com.example.payment_service.services.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final SqsClient sqsClient;
    private final PaymentRequestObjectRepository paymentRequestObjectRepository;

    @Value("${paystack.secret}")
    private String paystackSecret;

    @Value("${sqs.payment-completed-event-queue-url}")
    private String paymentCompletedEventQueueUrl;


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
                transaction.setStatus(TransactionStatus.SUCCESSFUL);
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

                    // On successful payment send SQS message to the event service to generate tickets
                    publishPaymentSuccessfulEventToQueue(processPaymentEvent);
                }

                transactionRepository.save(transaction);
            }else {
                Transaction transaction = transactionService.findByReference(webhook.data().reference());
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);

                // delete stall paymentRequest object
                paymentRequestObjectRepository.deleteByTransaction(transaction);
                log.info("Payment failed for reference {}", webhook.data().reference());
                log.info("Payment request object deleted for reference {}", webhook.data().reference());
            }
            return ResponseEntity.ok("Processed");
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    private void publishPaymentSuccessfulEventToQueue(ProcessPaymentEvent event) {
        try{
            log.info("Sending payment successful event to SQS");
            String messageBody = objectMapper.writeValueAsString(event);
            sqsClient.sendMessage(builder -> builder.queueUrl(paymentCompletedEventQueueUrl).messageBody(messageBody));
        } catch (Exception e) {
            log.error("Error sending payment successful event to SQS: {}", e.getMessage());
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


