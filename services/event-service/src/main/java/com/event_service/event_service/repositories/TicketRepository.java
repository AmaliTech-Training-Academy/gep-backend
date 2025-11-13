package com.event_service.event_service.repositories;

import com.event_service.event_service.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Ticket findByIdAndTicketCode(Long id, String ticketCode);

    Ticket findByTicketCode(String ticketCode);

    @Query("SELECT COALESCE(SUM(tt.price), 0) " +
            "FROM Ticket t " +
            "JOIN t.ticketType tt " +
            "JOIN tt.event e " +
            "WHERE e.userId = :userId")
    Double findTotalTicketSalesForUser(@Param("userId") Long userId);}
