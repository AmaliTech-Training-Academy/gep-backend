package com.event_service.event_service.repositories;

import com.event_service.event_service.models.EventOrganizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventOrganizerRepository extends JpaRepository<EventOrganizer, Long> {
    @Query("SELECT eo.userId FROM EventOrganizer eo WHERE eo.event.id = :eventId")
    List<Long> findUserIdsByEventId(@Param("eventId") Long eventId);

    List<EventOrganizer> findAllByUserId(Long id);
}
