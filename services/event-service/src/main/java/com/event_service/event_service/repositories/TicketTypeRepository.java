package com.event_service.event_service.repositories;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findAllByEvent(Event event);
}
