package com.example.auth_service.service;

import com.example.auth_service.dto.request.UserBankAccountCreationRequest;
import com.example.auth_service.dto.request.UserBankAccountUpdateRequest;
import com.example.auth_service.dto.request.UserMobileMoneyCreationRequest;
import com.example.auth_service.dto.request.UserMobileMoneyUpdateRequest;
import com.example.auth_service.dto.response.UserBankAccountResponse;
import com.example.auth_service.dto.response.UserMobileMoneyResponse;

public interface PaymentSettingService {
    void createBankAccount(UserBankAccountCreationRequest request);
    void updateBankAccount(UserBankAccountUpdateRequest request);
    void createMobileMoneyAccount(UserMobileMoneyCreationRequest request);
    void updateMobileMoneyAccount(UserMobileMoneyUpdateRequest request);
    UserBankAccountResponse getBankAccountDetails();
    UserMobileMoneyResponse getMobileMoneyAccount();
}
