package com.example.payment_service.services;

import com.example.payment_service.dto.PaystackRequest;
import com.example.payment_service.dto.PaystackTransaction;

public interface ExternalAPIService {
    PaystackTransaction createPaystackTransaction(PaystackRequest paystackRequest);
}

