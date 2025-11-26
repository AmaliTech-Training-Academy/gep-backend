package com.event_service.event_service.repositories;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Ticket findByIdAndTicketCode(Long id, String ticketCode);

    Ticket findByTicketCode(String ticketCode);

    @Query("""
        SELECT COALESCE(SUM(tt.price), 0)
        FROM Ticket t
        JOIN t.ticketType tt
        JOIN tt.event e
        WHERE e.userId = :userId
    """)
    Double findTotalTicketSalesForUser(@Param("userId") Long userId);

    @Query("""
        SELECT COALESCE(SUM(tt.price), 0)
        FROM Ticket t
        JOIN t.ticketType tt
        WHERE tt.event = :event
    """)
    Double findTotalTicketSalesForEvent(@Param("event") Event event);

    @Query("""
        SELECT COALESCE(SUM(tt.price), 0.0)
        FROM Ticket t
        JOIN t.ticketType tt
        JOIN tt.event e
        JOIN e.organizers eo
        WHERE eo.userId = :userId
    """)
    Double findTotalTicketSalesForCoOrganizer(@Param("userId") Long userId);
}
