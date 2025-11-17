package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.UserBankAccountCreationRequest;
import com.example.auth_service.dto.request.UserBankAccountUpdateRequest;
import com.example.auth_service.dto.request.UserMobileMoneyCreationRequest;
import com.example.auth_service.dto.request.UserMobileMoneyUpdateRequest;
import com.example.auth_service.dto.response.UserBankAccountResponse;
import com.example.auth_service.dto.response.UserMobileMoneyResponse;
import com.example.auth_service.mapper.UserPaymentSettingsMapper;
import com.example.auth_service.model.User;
import com.example.auth_service.model.UserBankAccount;
import com.example.auth_service.model.UserMobileMoney;
import com.example.auth_service.repository.UserBankAccountRepository;
import com.example.auth_service.repository.UserMobileMoneyRepository;
import com.example.auth_service.service.PaymentSettingService;
import com.example.common_libraries.exception.BadRequestException;
import com.example.common_libraries.exception.DuplicateResourceException;
import com.example.common_libraries.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class PaymentSettingServiceImpl implements PaymentSettingService {

    private final UserBankAccountRepository userBankAccountRepository;
    private final UserMobileMoneyRepository userMobileMoneyRepository;
    private final AuthServiceImpl authServiceImpl;

    @Override
    @Transactional
    public void createBankAccount(UserBankAccountCreationRequest request) {
        User currentUser = authServiceImpl.getAuthenticatedUser();
        if(userBankAccountRepository.existsByUser(currentUser)){
            throw new DuplicateResourceException("User bank account already exists");
        }
        UserBankAccount bankAccount = UserBankAccount.builder()
                .user(currentUser)
                .bankName(request.bankName())
                .accountNumber(request.accountNumber())
                .accountHolderName(request.accountHolderName())
                .build();

        userBankAccountRepository.save(bankAccount);
    }

    @Override
    public UserBankAccountResponse getBankAccountDetails(){
        User currentUser = authServiceImpl.getAuthenticatedUser();
        UserBankAccount bankAccount = userBankAccountRepository.findByUser(currentUser).
                orElseThrow(() -> new ResourceNotFoundException("User bank account not found"));
        return UserPaymentSettingsMapper.toBankAccountResponse(bankAccount);
    }

    @Override
    @Transactional
    public void updateBankAccount(UserBankAccountUpdateRequest request){
        User currentUser = authServiceImpl.getAuthenticatedUser();
        UserBankAccount bankAccount = userBankAccountRepository.findByUser(currentUser).orElseThrow(()-> new ResourceNotFoundException("User bank account not found"));
        if(request.bankName() != null && !Objects.equals(request.bankName(), bankAccount.getBankName())){
            bankAccount.setBankName(request.bankName());
        }
        if(request.accountNumber() != null && !Objects.equals(request.accountNumber(), bankAccount.getAccountNumber())){
            bankAccount.setAccountNumber(request.accountNumber());
        }
        if(request.accountHolderName() != null && !Objects.equals(request.accountHolderName(), bankAccount.getAccountHolderName())){
            bankAccount.setAccountHolderName(request.accountHolderName());
        }
        if(request.isActive() != null && !Objects.equals(request.isActive(), bankAccount.getIsActive())){
            bankAccount.setIsActive(request.isActive());
        }
        userBankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public void createMobileMoneyAccount(UserMobileMoneyCreationRequest request){
        User currentUser = authServiceImpl.getAuthenticatedUser();
        if(userMobileMoneyRepository.existsByUser(currentUser)){
            throw new DuplicateResourceException("User mobile money account already exists");
        }
        UserMobileMoney mobileMoneyAccount = UserMobileMoney.builder()
                .user(currentUser)
                .networkOperator(request.networkOperator())
                .mobileMoneyNumber(request.mobileMoneyNumber())
                .accountHolderName(request.accountHolderName())
                .build();

        userMobileMoneyRepository.save(mobileMoneyAccount);
    }

    @Override
    @Transactional
    public UserMobileMoneyResponse getMobileMoneyAccount(){
        User currentUser = authServiceImpl.getAuthenticatedUser();
        UserMobileMoney mobileMoneyAccount = userMobileMoneyRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("User mobile money account not found"));
        return UserPaymentSettingsMapper.toMobileMoneyResponse(mobileMoneyAccount);
    }

    @Override
    @Transactional
    public void updateMobileMoneyAccount(UserMobileMoneyUpdateRequest request){
        User currentUser = authServiceImpl.getAuthenticatedUser();
        UserMobileMoney mobileMoneyAccount = userMobileMoneyRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("User mobile money account not found"));
        if(request.networkOperator() != null && !Objects.equals(request.networkOperator(), mobileMoneyAccount.getNetworkOperator())){
            mobileMoneyAccount.setNetworkOperator(request.networkOperator());
        }
        if(request.mobileMoneyNumber() != null && !Objects.equals(request.mobileMoneyNumber(), mobileMoneyAccount.getMobileMoneyNumber())){
            mobileMoneyAccount.setMobileMoneyNumber(request.mobileMoneyNumber());
        }
        if(request.isActive() != null && !Objects.equals(request.isActive(), mobileMoneyAccount.getIsActive())){
            mobileMoneyAccount.setIsActive(request.isActive());
        }

        if(request.accountHolderName() != null && !Objects.equals(request.accountHolderName(), mobileMoneyAccount.getAccountHolderName())){
            mobileMoneyAccount.setAccountHolderName(request.accountHolderName());
        }
        userMobileMoneyRepository.save(mobileMoneyAccount);
    }
}
