package com.event_service.event_service.services;

import com.event_service.event_service.client.PaymentServiceClient;
import com.event_service.event_service.dto.EventEarningResponse;
import com.event_service.event_service.dto.EventEarningWithdrawalRequest;
import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.TicketType;
import com.event_service.event_service.models.WithdrawalRequest;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.repositories.WithdrawalRequestRepository;
import com.event_service.event_service.utils.SecurityUtils;
import com.example.common_libraries.dto.AppUser;
import com.example.common_libraries.dto.WithdrawalRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class EventEarningServiceImpl implements EventEarningService{

    private final SecurityUtils securityUtils;
    private final EventRepository eventRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final PaymentServiceClient paymentServiceClient;

    @Override
    public EventEarningResponse getEventEarnings() {
        AppUser currentUser = securityUtils.getCurrentUser();
        List<Event> events = eventRepository.getEventsByUserId(currentUser.id());
        double totalEarnings = 0;
        double totalWithdrawn = 0;

        for(Event event : events){
            List<TicketType> ticketTypes = event.getTicketTypes();
            for(TicketType ticketType : ticketTypes){
                if(ticketType.getSoldCount() > 0){
                    double earnings = ticketType.getSoldCount() * ticketType.getPrice();
                    totalEarnings += earnings;
                }
            }
        }

        totalWithdrawn = getTotalWithdrawalsByUserId(currentUser.id());

        double outstandingEarnings = totalEarnings - totalWithdrawn;

        return new EventEarningResponse(totalEarnings,totalWithdrawn,outstandingEarnings);
    }

    @Override
    public void withdrawEarnings(EventEarningWithdrawalRequest withdrawalRequest) {
        AppUser currentUser = securityUtils.getCurrentUser();
        EventEarningResponse earnings = getEventEarnings();
        if(withdrawalRequest.amount() > earnings.outstandingBalance()){
            throw new IllegalArgumentException("Withdrawal amount exceeds outstanding earnings.");
        }
        WithdrawalRequestDto withdrawalDto = new WithdrawalRequestDto(
                currentUser.id(),
                withdrawalRequest.withdrawalMethod(),
                withdrawalRequest.amount(),
                withdrawalRequest.provider(),
                withdrawalRequest.accountNumber(),
                withdrawalRequest.accountName()
        );
        requestWithdrawal(withdrawalDto);
    }

    public void requestWithdrawal(WithdrawalRequestDto withdrawalRequest) {
        WithdrawalRequest request = WithdrawalRequest.builder()
                .userId(withdrawalRequest.userId())
                .withdrawalMethod(withdrawalRequest.withdrawalMethod())
                .userId(withdrawalRequest.userId())
                .amount(withdrawalRequest.amount())
                .providerName(withdrawalRequest.providerName())
                .accountNumber(withdrawalRequest.accountNumber())
                .accountHolderName(withdrawalRequest.accountHolderName())
                .build();
        withdrawalRequestRepository.save(request);
//        publish withdrawal message to queue
    }

    protected Double getTotalWithdrawalsByUserId(Long userId) {
        List<WithdrawalRequest> withdrawalRequests = withdrawalRequestRepository.findByUserId(userId);
        return withdrawalRequests.stream()
                .mapToDouble(WithdrawalRequest::getAmount)
                .sum();

    }
}
