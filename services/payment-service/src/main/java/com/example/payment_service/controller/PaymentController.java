package com.example.payment_service.controller;

import com.example.common_libraries.dto.CustomApiResponse;
import com.example.common_libraries.dto.PaystackResponse;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.example.payment_service.dto.TransactionResponse;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.models.TransactionStatus;
import com.example.payment_service.services.PaymentService;
import com.example.payment_service.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<PaystackResponse> payment(@RequestBody ProcessPaymentEvent paymentRequest) {
        Transaction transaction = transactionService.createTransaction(paymentRequest);
        PaystackResponse paystackResponse = new PaystackResponse(transaction.getAuthorizationUrl(), transaction.getReference());
        return ResponseEntity.ok(paystackResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<Page<TransactionResponse>>> getAllTransactions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false, defaultValue = "0") int page
    ){
        return ResponseEntity.ok(CustomApiResponse.success(paymentService.getAllTransactions(page, keyword, status)));
    }
}
