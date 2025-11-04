package com.event_service.event_service.repositories;

import com.event_service.event_service.models.EventOrganizer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventOrganizerRepository extends JpaRepository<EventOrganizer, Long> {
}
