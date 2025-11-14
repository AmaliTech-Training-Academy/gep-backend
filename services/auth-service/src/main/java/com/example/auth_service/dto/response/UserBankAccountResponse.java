package com.example.auth_service.dto.response;

public record UserBankAccountResponse(
        String bankName,
        String accountNumber,
        String accountHolderName,
        Boolean isActive
) {
}
