package com.example.payment_service.services;

import com.example.common_libraries.dto.queue_events.PaymentStatusEvent;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.payment_service.dto.PaystackRequest;
import com.example.payment_service.dto.PaystackTransaction;
import com.example.payment_service.dto.TransactionRequest;
import com.example.payment_service.models.PaymentRequestObject;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.models.TransactionStatus;
import com.example.payment_service.publisher.MessagePublisher;
import com.example.payment_service.repos.PaymentRequestObjectRepository;
import com.example.payment_service.repos.TransactionRepository;
import com.example.common_libraries.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ExternalAPIService externalAPIService;
    private final PaymentRequestObjectRepository paymentRequestObjectRepository;
    private final MessagePublisher messagePublisher;

    @Override
    @Transactional
    public Transaction createTransaction(ProcessPaymentEvent paymentRequest) {

        BigDecimal totalTicketPrice = BigDecimal.valueOf(paymentRequest.amount()*paymentRequest.numberOfTickets());
        BigDecimal amountInSmallestUnit = totalTicketPrice.multiply(new BigDecimal("100"));
        Integer amountToSend = amountInSmallestUnit.setScale(0, RoundingMode.HALF_UP).intValue();

        PaystackRequest paystackRequest = new PaystackRequest(
                paymentRequest.email(),
                amountToSend
        );

        PaystackTransaction paystackTransaction = createPaystackTransaction(paystackRequest);

        if (paystackTransaction.status()) {
            log.info("Paystack Transaction: {}", paystackTransaction.data().authorizationUrl());
        }

        Transaction transaction = Transaction.builder()
                .accessToken(paystackTransaction.data().accessCode())
                .reference(paystackTransaction.data().reference())
                .amount(totalTicketPrice)
                .authorizationUrl(paystackTransaction.data().authorizationUrl())
                .email(paymentRequest.email())
                .status(TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        PaymentRequestObject paymentRequestObject = PaymentRequestObject
                .builder()
                .transaction(savedTransaction)
                .email(paymentRequest.email())
                .fullName(paymentRequest.fullName())
                .amount(paymentRequest.amount())
                .numberOfTickets(paymentRequest.numberOfTickets())
                .ticketTypeId(paymentRequest.ticketTypeId())
                .eventId(paymentRequest.eventRegistrationResponse().id())
                .eventTitle(paymentRequest.eventRegistrationResponse().eventTitle())
                .location(paymentRequest.eventRegistrationResponse().location())
                .organizer(paymentRequest.eventRegistrationResponse().organizer())
                .startDate(paymentRequest.eventRegistrationResponse().startDate())
                .build();

        // Send payment initiated message to queue
        PaymentStatusEvent statusEvent = PaymentStatusEvent
                .builder()
                .transactionId(savedTransaction.getReference())
                .email(savedTransaction.getEmail())
                .fullName(paymentRequest.fullName())
                .paymentMethod("N/A")
                .status(TransactionStatus.PENDING.name())
                .amount(savedTransaction.getAmount())
                .timestamp(Instant.now())
                .build();

        messagePublisher.publishPaymentStatusToQueue(statusEvent);

        paymentRequestObjectRepository.save(paymentRequestObject);

        return savedTransaction;
    }

    @Override
    public void updateTransaction(Long id, TransactionRequest transactionRequest) {
        Transaction transaction = getTransaction(id);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);
    }

    @Override
    public void deleteTransaction(Long id) {
        Transaction transaction = getTransaction(id);
        transactionRepository.delete(transaction);
    }

    @Override
    public Transaction findByReference(String reference) {
        return transactionRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private PaystackTransaction createPaystackTransaction(PaystackRequest paystackRequest) {
        return externalAPIService.createPaystackTransaction(paystackRequest);
    }

    private Transaction getTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }
}
