package com.example.auth_service.service.impl;

import com.example.auth_service.model.UserEventStats;
import com.example.auth_service.repository.UserEventStatsRepository;
import com.example.auth_service.service.UserEventStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventStatServiceImpl implements UserEventStatService {
    private final UserEventStatsRepository userEventStatsRepository;

    @Override
    public void updateUserEventStats(Long userId) {
        // get event stat by userId
        UserEventStats userEventStats = userEventStatsRepository.findByUserId(userId).orElse(null);
        if(userEventStats == null){
            log.warn("User event stats not found for user with id: {}", userId);
            return;
        }
        userEventStats.setTotalEventsCreated(userEventStats.getTotalEventsCreated() + 1);
        userEventStatsRepository.save(userEventStats);
    }
}
