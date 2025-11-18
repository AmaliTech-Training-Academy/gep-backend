package com.example.auth_service.mapper;

import com.example.auth_service.dto.response.UserBankAccountResponse;
import com.example.auth_service.dto.response.UserMobileMoneyResponse;
import com.example.auth_service.model.UserBankAccount;
import com.example.auth_service.model.UserMobileMoney;

public class UserPaymentSettingsMapper {
    public static UserBankAccountResponse toBankAccountResponse(UserBankAccount bankAccount){
        return new UserBankAccountResponse(
                bankAccount.getBankName(),
                bankAccount.getAccountNumber(),
                bankAccount.getAccountHolderName(),
                bankAccount.getIsActive()
        );
    }

    public static UserMobileMoneyResponse toMobileMoneyResponse(UserMobileMoney mobileMoneyAccount){
        return new UserMobileMoneyResponse(
          mobileMoneyAccount.getNetworkOperator(),
          mobileMoneyAccount.getMobileMoneyNumber(),
          mobileMoneyAccount.getAccountHolderName(),
          mobileMoneyAccount.getIsActive()
        );
    }
}
