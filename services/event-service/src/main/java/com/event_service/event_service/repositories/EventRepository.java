package com.event_service.event_service.repositories;

import com.event_service.event_service.dto.EventManagementResponse;
import com.event_service.event_service.dto.projection.EventManagementProjection;
import com.event_service.event_service.dto.projection.EventStatProjection;
import com.event_service.event_service.models.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
    SELECT
        COUNT(e) AS totalEvents,
        SUM(CASE WHEN e.startTime <= :instant AND e.endTime >= :instant THEN 1 ELSE 0 END) AS activeEvents,
        SUM(CASE WHEN e.startTime > :instant THEN 1 ELSE 0 END) AS draftEvents,
        SUM(CASE WHEN e.endTime < :instant THEN 1 ELSE 0 END) AS completedEvents
    FROM Event e
    """)
    EventStatProjection getEventStats(@Param("instant") Instant instant);

    @Query("""
        SELECT e
        FROM Event e
        WHERE e.startTime > :instant
        ORDER BY e.startTime ASC
    """)
    List<Event> findUpcomingEvents(@Param("instant") Instant instant, Pageable pageable);

    //TODO replace hardcoded Organizer name with actual Organizer name
    @Query("""
        SELECT e.id AS id,
               e.title AS title,
               'ORGANIZER' AS organizer,
               e.startTime AS startTime,
               e.endTime AS endTime,
               COUNT(er.id) AS attendeeCount
        FROM Event e
        LEFT JOIN e.eventRegistrations er
        GROUP BY e.id, e.title, e.startTime, e.endTime
        ORDER BY COUNT(er.id) DESC
    """)
    List<EventManagementProjection> getEventManagement(Pageable pageable);
}
