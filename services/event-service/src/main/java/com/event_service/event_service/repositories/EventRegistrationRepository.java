package com.event_service.event_service.repositories;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventRegistration;
import com.event_service.event_service.specifications.EventRegistrationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long>, JpaSpecificationExecutor<EventRegistration> {
    @Query("SELECT COALESCE(SUM(er.ticketQuantity), 0) " +
            "FROM EventRegistration er " +
            "WHERE er.event = :event AND er.email = :email")
    Integer sumTicketsByEventIdAndEmail(@Param("event") Event event,
                                        @Param("email") String email);

    Long countByEventUserId(Long id);
}
