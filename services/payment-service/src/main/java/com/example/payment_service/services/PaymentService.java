package com.example.payment_service.services;

import com.example.payment_service.dto.TransactionResponse;
import com.example.payment_service.models.TransactionStatus;
import org.springframework.data.domain.Page;

public interface PaymentService {
    Page<TransactionResponse> getAllTransactions(int page, String keyword, TransactionStatus status);
}
