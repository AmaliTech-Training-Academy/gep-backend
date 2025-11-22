package com.example.payment_service.services;

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
}
