package com.example.payment_service.services;

import com.example.common_libraries.dto.EventRegistrationResponse;
import com.example.common_libraries.dto.queue_events.PaymentStatusEvent;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.payment_service.dto.PaystackWebhook;
import com.example.payment_service.models.PaymentRequestObject;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.models.TransactionStatus;
import com.example.payment_service.publisher.MessagePublisher;
import com.example.payment_service.repos.PaymentRequestObjectRepository;
import com.example.payment_service.repos.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService{
    private final TransactionService transactionService;
    private final PaymentRequestObjectRepository paymentRequestObjectRepository;
    private final MessagePublisher messagePublisher;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void handleWebhook(String rawBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PaystackWebhook payload = objectMapper.readValue(rawBody, PaystackWebhook.class);

            String reference = payload.data().reference();
            log.info("Webhook received: {}", payload);
            Transaction transaction = transactionService.findByReference(reference);
            if(transaction.getStatus() == TransactionStatus.SUCCESS || transaction.getStatus() == TransactionStatus.FAILED){
                log.warn("Transaction already processed for reference {}", reference);
                return;
            }


            if ("charge.success".equals(payload.event())) {
                log.info("Payment successful for reference {}", reference);
                // update transaction status and payment method
                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setPaymentMethod(payload.data().channel());

                // handle payment success
                handlePaymentSuccess(payload, transaction);
            } else {
                // update transaction status
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setPaymentMethod(payload.data().channel());

                // handle payment failure
                handlePaymentFailure(payload, transaction);
            }
        }catch (Exception e){
            log.error("Error processing webhook: {}", e.getMessage());
        }
    }

    private void handlePaymentFailure(PaystackWebhook payload, Transaction transaction) {
        log.warn("Handling payment failed for reference {}", payload.data().reference());
        // save transaction
        transactionRepository.save(transaction);

        // Build payment status event
        PaymentStatusEvent statusEvent = PaymentStatusEvent
                .builder()
                .transactionId(transaction.getReference())
                .email(transaction.getEmail())
                .fullName(transaction.getPaymentRequestObject().getFullName())
                .paymentMethod(payload.data().channel())
                .status(TransactionStatus.FAILED.name())
                .amount(transaction.getAmount())
                .timestamp(Instant.now())
                .build();

        // publish to payment status queue
        messagePublisher.publishPaymentStatusToQueue(statusEvent);
        log.warn("Payment failed for reference {}", payload.data().reference());
    }

    protected void handlePaymentSuccess(PaystackWebhook payload, Transaction transaction) {
        log.info("Handling payment successful for reference {}", payload.data().reference());
        // fetch payment request object
        PaymentRequestObject pro = paymentRequestObjectRepository.findByTransaction(transaction).orElse(null);
        if(pro != null){
            // Build event response object
            EventRegistrationResponse eventRegistrationResponse =
                    EventRegistrationResponse
                            .builder()
                            .id(pro.getEventId())
                            .eventTitle(pro.getEventTitle())
                            .location(pro.getLocation())
                            .organizer(pro.getOrganizer())
                            .startDate(pro.getStartDate())
                            .authorizationUrl(transaction.getAuthorizationUrl())
                            .build();

            // Build process payment event
            ProcessPaymentEvent processPaymentEvent =
                    ProcessPaymentEvent
                            .builder()
                            .amount(pro.getAmount())
                            .ticketTypeId(pro.getTicketTypeId())
                            .numberOfTickets(pro.getNumberOfTickets())
                            .fullName(pro.getFullName())
                            .email(pro.getEmail())
                            .eventRegistrationResponse(eventRegistrationResponse)
                            .build();

            // Build payment success event
            PaymentStatusEvent statusEvent = PaymentStatusEvent
                    .builder()
                    .transactionId(transaction.getReference())
                    .email(transaction.getEmail())
                    .fullName(pro.getFullName())
                    .paymentMethod(transaction.getPaymentMethod())
                    .status(TransactionStatus.SUCCESS.name())
                    .amount(transaction.getAmount())
                    .timestamp(Instant.now())
                    .build();

            // save transaction
            transactionRepository.save(transaction);

            // publish to payment status queue
            messagePublisher.publishPaymentStatusToQueue(statusEvent);
            // publish to payment successful event queue
            messagePublisher.publishPaymentSuccessfulEventToQueue(processPaymentEvent);
            return;
        }

        // save transaction even if the payment request object is null
        transactionRepository.save(transaction);
    }
}
