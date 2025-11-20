package com.example.payment_service.controller;


import com.example.common_libraries.dto.PaystackResponse;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.common_libraries.dto.PaystackResponse;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
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
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class TestEndpoint {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<PaystackResponse> payment(@RequestBody ProcessPaymentEvent paymentRequest) {
        Transaction transaction = transactionService.createTransaction(paymentRequest);
        PaystackResponse paystackResponse = new PaystackResponse(transaction.getAuthorizationUrl(), transaction.getReference());
        return ResponseEntity.ok(paystackResponse);
    }
}
