package com.example.payment_service.services;

import com.example.payment_service.ResourceNotFound;
import com.example.payment_service.dto.PaystackRequest;
import com.example.payment_service.dto.PaystackTransaction;
import com.example.payment_service.dto.TransactionRequest;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.models.TransactionStatus;
import com.example.payment_service.repos.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ExternalAPIService externalAPIService;

    @Override
    public Transaction createTransaction(TransactionRequest transactionRequest) {
        BigDecimal amountInSmallestUnit = transactionRequest.price().multiply(new BigDecimal("100"));
        Integer amountToSend = amountInSmallestUnit.setScale(0, RoundingMode.HALF_UP).intValue();

        PaystackRequest paystackRequest = new PaystackRequest(
                transactionRequest.email(),
                amountToSend
        );

        PaystackTransaction paystackTransaction = createPaystackTransaction(paystackRequest);

        if (paystackTransaction.status()) {
            log.info("Paystack Transaction: {}", paystackTransaction.data().authorizationUrl());
        }

        Transaction transaction = Transaction.builder()
                .accessToken(paystackTransaction.data().accessCode())
                .reference(paystackTransaction.data().reference())
                .authorizationUrl(paystackTransaction.data().authorizationUrl())
                .email(transactionRequest.email())
                .status(TransactionStatus.PENDING)
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    public void updateTransaction(Long id, TransactionRequest transactionRequest) {
        Transaction transaction = getTransaction(id);
        transaction.setStatus(TransactionStatus.SUCCESSFUL);
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
                .orElseThrow(() -> new ResourceNotFound("Transaction not found"));
    }

    private PaystackTransaction createPaystackTransaction(PaystackRequest paystackRequest) {
        return externalAPIService.createPaystackTransaction(paystackRequest);
    }

    private Transaction getTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Transaction not found"));
    }
}
