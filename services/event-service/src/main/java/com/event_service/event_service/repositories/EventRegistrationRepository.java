package com.event_service.event_service.repositories;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    @Query("SELECT COALESCE(SUM(er.ticketQuantity), 0) " +
            "FROM EventRegistration er " +
            "WHERE er.event = :event AND er.email = :email")
    Integer sumTicketsByEventIdAndEmail(@Param("event") Event event,
                                        @Param("email") String email);
}
