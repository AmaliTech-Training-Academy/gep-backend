package com.event_service.event_service.repositories;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



@Repository
public interface EventOptionsRepository extends JpaRepository<EventOptions, Long> {
    @Query("SELECT eo.capacity FROM EventOptions eo WHERE eo.event = :event")
    Long findCapacityByEvent(Event event);
}
