package com.event_service.event_service.controllers;

import com.event_service.event_service.dto.EventEarningResponse;
import com.event_service.event_service.dto.EventEarningWithdrawalRequest;
import com.event_service.event_service.services.EventEarningService;
import com.example.common_libraries.dto.CustomApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/events/earnings")
public class EventEarningsController {

    private final EventEarningService eventEarningService;

    @GetMapping
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<EventEarningResponse>> getEventEarnings(){
        EventEarningResponse earnings = eventEarningService.getEventEarnings();
        return ResponseEntity.ok(CustomApiResponse.success(earnings));
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<CustomApiResponse<Object>> requestWithdrawal(@Valid @RequestBody EventEarningWithdrawalRequest request){
        eventEarningService.withdrawEarnings(request);
        return ResponseEntity.ok(CustomApiResponse.success("Withdrawal request submitted successfully"));
    }
}
