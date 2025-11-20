package com.example.payment_service.controller;

import com.example.payment_service.repos.TransactionRepository;
import com.example.payment_service.dto.PaystackWebhook;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.models.TransactionStatus;
import com.example.payment_service.services.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;


    @Value("${paystack.secret}")
    private String paystackSecret;

    public WebhookController(TransactionService transactionService, TransactionRepository transactionRepository) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Paystack-Signature") String signature) {

        try {
            String computedHash = hmacSha512(rawBody, paystackSecret);
            if (!computedHash.equals(signature)) {
                logger.warn("Invalid Paystack signature");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
            }
            ObjectMapper mapper = new ObjectMapper();
            PaystackWebhook webhook = mapper.readValue(rawBody, PaystackWebhook.class);
            if ("charge.success".equals(webhook.event())) {
                logger.info("Payment successful for reference {}", webhook.data().reference());
                Transaction transaction = transactionService.findByReference(webhook.data().reference());
                transaction.setStatus(TransactionStatus.SUCCESSFUL);
                transactionRepository.save(transaction);
            }else {
                Transaction transaction = transactionService.findByReference(webhook.data().reference());
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
            }
            return ResponseEntity.ok("Processed");
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    private String hmacSha512(String data, String secret) throws Exception {
        Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512_HMAC.init(secretKey);
        byte[] hashBytes = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}


