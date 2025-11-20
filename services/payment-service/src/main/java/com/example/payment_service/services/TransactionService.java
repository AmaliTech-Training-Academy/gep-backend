package com.example.payment_service.services;

import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.payment_service.dto.TransactionRequest;
import com.example.payment_service.models.Transaction;

public interface TransactionService {
    Transaction createTransaction(ProcessPaymentEvent paymentEvent);
    void updateTransaction(Long id, TransactionRequest transactionRequest);
    void deleteTransaction(Long id);
    Transaction findByReference(String reference);
}
