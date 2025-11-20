package com.example.payment_service.controller;


import com.example.payment_service.dto.PaystackResponse;
import com.example.payment_service.dto.TransactionRequest;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class TestEnpoint {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<PaystackResponse> payment(@RequestBody TransactionRequest transactionRequest) {
        Transaction transaction = transactionService.createTransaction(transactionRequest);
        PaystackResponse paystackResponse = new PaystackResponse(transaction.getAuthorizationUrl(), transaction.getReference());
        return ResponseEntity.ok(paystackResponse);
    }
}
