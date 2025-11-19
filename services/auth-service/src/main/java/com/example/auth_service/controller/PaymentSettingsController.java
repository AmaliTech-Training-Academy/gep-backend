package com.example.auth_service.controller;

import com.example.auth_service.dto.request.UserBankAccountCreationRequest;
import com.example.auth_service.dto.request.UserBankAccountUpdateRequest;
import com.example.auth_service.dto.request.UserMobileMoneyCreationRequest;
import com.example.auth_service.dto.request.UserMobileMoneyUpdateRequest;
import com.example.auth_service.dto.response.UserBankAccountResponse;
import com.example.auth_service.dto.response.UserMobileMoneyResponse;
import com.example.auth_service.service.PaymentSettingService;
import com.example.common_libraries.dto.CustomApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users/payment-settings")
public class PaymentSettingsController {

    private final PaymentSettingService paymentSettingService;

    @GetMapping("/mobile-money")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<UserMobileMoneyResponse>> getMobileMoneyAccount(){
        UserMobileMoneyResponse mobileMoneyResponse = paymentSettingService.getMobileMoneyAccount();
        return ResponseEntity.ok(CustomApiResponse.success("Mobile Money Account fetched successfully",mobileMoneyResponse ));
    }

    @PostMapping("/mobile-money")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<Object>> createMobileMoneyAccount(@Valid @RequestBody UserMobileMoneyCreationRequest request){
        paymentSettingService.createMobileMoneyAccount(request);
        return ResponseEntity.ok(CustomApiResponse.success("Mobile Money Account created successfully"));
    }

    @PutMapping("/mobile-money")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<Object>> updateMobileMoneyAccount(@Valid @RequestBody UserMobileMoneyUpdateRequest request){
        paymentSettingService.updateMobileMoneyAccount(request);
        return ResponseEntity.ok(CustomApiResponse.success("Mobile Money Account updated successfully"));
    }

    @GetMapping("/bank-account")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<UserBankAccountResponse>> getBankAccountDetails(){
        UserBankAccountResponse bankAccountResponse = paymentSettingService.getBankAccountDetails();
        return ResponseEntity.ok(CustomApiResponse.success("Bank Account Details fetched successfully", bankAccountResponse));
    }

    @PostMapping("/bank-account")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<Object>> createBankAccount(@Valid @RequestBody UserBankAccountCreationRequest request){
        paymentSettingService.createBankAccount(request);
        return ResponseEntity.ok(CustomApiResponse.success("Bank Account created successfully"));
    }

    @PutMapping("/bank-account")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<Object>> updateBankAccount(@Valid @RequestBody UserBankAccountUpdateRequest request){
        paymentSettingService.updateBankAccount(request);
        return ResponseEntity.ok(CustomApiResponse.success("Bank Account updated successfully"));
    }
}
