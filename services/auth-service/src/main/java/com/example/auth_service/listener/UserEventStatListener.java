package com.example.auth_service.listener;

import com.example.auth_service.service.UserEventStatService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserEventStatListener {
    private final UserEventStatService userEventStatService;

    @SqsListener("${sqs.event-stat-queue-url}")
    public void listenEventCreated(Long organizerId) {
        log.info("Updating event stats for organizer with id: {}", organizerId);
        userEventStatService.updateUserEventStats(organizerId);
    }
}
