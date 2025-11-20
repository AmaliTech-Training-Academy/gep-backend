package com.example.payment_service.services;

import com.example.payment_service.dto.TransactionRequest;
import com.example.payment_service.models.Transaction;

public interface TransactionService {
    Transaction createTransaction(TransactionRequest transactionRequest);
    void updateTransaction(Long id, TransactionRequest transactionRequest);
    void deleteTransaction(Long id);
    Transaction findByReference(String reference);
}
