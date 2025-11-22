package com.example.common_libraries.dto;

import com.example.common_libraries.enums.WithdrawalMethod;

public record WithdrawalRequestDto(
        Long userId,
        WithdrawalMethod withdrawalMethod,
        Double amount,
        String providerName,
        String accountNumber,
        String accountHolderName
) {
}
