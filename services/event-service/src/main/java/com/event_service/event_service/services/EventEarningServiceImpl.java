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
import com.example.common_libraries.dto.queue_events.WithdrawalNotificationEvent;
import com.example.common_libraries.enums.WithdrawalMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventEarningServiceImpl implements EventEarningService{

    private final SecurityUtils securityUtils;
    private final EventRepository eventRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${sqs.withdrawal-notification-queue}")
    private String withdrawalNotificationQueue;

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
        WithdrawalRequest withdrawal = requestWithdrawal(withdrawalDto);
        publishWithdrawalMessageToQueue(withdrawal, currentUser);
    }

    public WithdrawalRequest requestWithdrawal(WithdrawalRequestDto withdrawalRequest) {
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
        return request;
    }

    protected Double getTotalWithdrawalsByUserId(Long userId) {
        List<WithdrawalRequest> withdrawalRequests = withdrawalRequestRepository.findByUserId(userId);
        return withdrawalRequests.stream()
                .mapToDouble(WithdrawalRequest::getAmount)
                .sum();

    }

    private void publishWithdrawalMessageToQueue(WithdrawalRequest withdrawalRequest, AppUser currentUser) {
        try{
            String paymentMethod="";
            if(withdrawalRequest.getWithdrawalMethod().equals(WithdrawalMethod.BANK)){
                paymentMethod = "Bank Transfer";
            }else if(withdrawalRequest.getWithdrawalMethod().equals(WithdrawalMethod.MOBILE_MONEY)){
                paymentMethod = "Mobile Money";
            }
            WithdrawalNotificationEvent event = new WithdrawalNotificationEvent(
                    currentUser.email(),
                    currentUser.fullName(),
                    withdrawalRequest.getAmount(),
                    paymentMethod,
                    LocalDateTime.now(),
                    withdrawalRequest.getProviderName(),
                    maskAccountNumberFixed(withdrawalRequest.getAccountNumber())
            );
            String messageBody = objectMapper.writeValueAsString(event);
            sqsClient.sendMessage(builder -> builder.queueUrl(withdrawalNotificationQueue).messageBody(messageBody));
        }catch(Exception e){
            log.error("Error publishing information to withdrawal queue");
        }
    }

    public static String maskAccountNumberFixed(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return accountNumber;
        }

        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
