package com.event_service.event_service.services;

import com.event_service.event_service.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("eventOwnerShipService")
@RequiredArgsConstructor
public class EventOwnershipService {

    private final EventRepository eventRepository;

    public boolean checkOwnerShip(Long id, Long userId) {
        return eventRepository.existsByIdAndUserId(id, userId);
    }


}