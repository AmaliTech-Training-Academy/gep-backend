package com.example.payment_service.services;

import com.example.common_libraries.dto.EventRegistrationResponse;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.example.payment_service.dto.TransactionResponse;
import com.example.payment_service.models.PaymentRequestObject;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.models.TransactionStatus;
import com.example.payment_service.repos.TransactionRepository;
import com.example.payment_service.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{
    private final TransactionRepository transactionRepository;

    @Override
    public Page<TransactionResponse> getAllTransactions(int page, String keyword, TransactionStatus status) {
        page = Math.max(page, 0);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, 10, sort);
        Specification<Transaction> spec = (root, query, cb) -> cb.conjunction();;

        if(keyword != null && !keyword.trim().isEmpty()) {
            spec.and(TransactionSpecification.hasKeyword(keyword.trim()));
        }
        if(status != null) {
            spec.and(TransactionSpecification.hasStatus(status));
        }

        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);
        return transactions.map(transaction -> {
            PaymentRequestObject pro = transaction.getPaymentRequestObject();
            return TransactionResponse.builder()
                    .transactionId(transaction.getReference())
                    .eventName(pro != null && pro.getEventTitle() != null ? pro.getEventTitle() : "N/A")
                    .eventOrganizer(pro != null && pro.getOrganizer() != null ? pro.getOrganizer() : "N/A")
                    .attendeeEmail(pro != null && pro.getEmail() != null ? pro.getEmail() : "N/A")
                    .amount(transaction.getAmount())
                    .paymentMethod(transaction.getPaymentMethod())
                    .status(transaction.getStatus())
                    .transactionTime(transaction.getCreatedAt())
                    .build();
        });
    }

    @Override
    public EventRegistrationResponse getRegistrationResponse(String reference) {
        if(reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Reference cannot be null or empty");
        }

        Transaction transaction = transactionRepository.findByReference(reference).orElseThrow(()-> new ResourceNotFoundException("Transaction not found"));
        PaymentRequestObject paymentRequestObject = Optional.ofNullable(transaction.getPaymentRequestObject()).orElseThrow(()-> new ResourceNotFoundException("Could not find event details for this payment"));

        return EventRegistrationResponse
                .builder()
                .id(paymentRequestObject.getEventId())
                .eventTitle(paymentRequestObject.getEventTitle())
                .location(paymentRequestObject.getLocation())
                .organizer(paymentRequestObject.getOrganizer())
                .startDate(paymentRequestObject.getStartDate())
                .build();
    }
}
