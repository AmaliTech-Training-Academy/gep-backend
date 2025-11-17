package com.example.auth_service.dto.response;

public record UserMobileMoneyResponse(
        String networkOperator,
        String mobileMoneyNumber,
        String accountHolderName,
        Boolean isActive
) {
}
