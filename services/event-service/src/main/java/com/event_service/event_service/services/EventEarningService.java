package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventEarningResponse;
import com.event_service.event_service.dto.EventEarningWithdrawalRequest;

public interface EventEarningService {
    EventEarningResponse getEventEarnings();
    void withdrawEarnings(EventEarningWithdrawalRequest amount);
}
