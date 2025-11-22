package com.event_service.event_service.dto;

public record EventEarningResponse(
        Double ticketsSold,
        Double amountWithdrawn,
        Double outstandingBalance
) {
}
