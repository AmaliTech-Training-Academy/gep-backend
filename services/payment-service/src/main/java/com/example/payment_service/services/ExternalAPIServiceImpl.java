package com.example.payment_service.services;

import com.example.payment_service.dto.PaystackRequest;
import com.example.payment_service.dto.PaystackTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalAPIServiceImpl implements ExternalAPIService {

    @Value("${paystack.secret}")
    private String paystackSecret;

    @Value("${paystack.url}")
    private String paystackUrl;

    private final RestTemplate restTemplate;

    public ExternalAPIServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public PaystackTransaction createPaystackTransaction(PaystackRequest paystackRequest) {
        HttpEntity<Object> httpEntity = paystackRequestObject(paystackRequest);
        return restTemplate.postForObject(paystackUrl, httpEntity, PaystackTransaction.class);
    }

    private HttpEntity<Object> paystackRequestObject(PaystackRequest paystackRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecret);
        headers.set("Content-Type", "application/json");
        return new HttpEntity<>(paystackRequest, headers);
    }
}
