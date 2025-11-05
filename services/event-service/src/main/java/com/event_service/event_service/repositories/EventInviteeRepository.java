package com.event_service.event_service.repositories;

import com.event_service.event_service.models.EventInvitation;
import com.event_service.event_service.models.EventInvitee;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventInviteeRepository extends JpaRepository<EventInvitee, Long> {
    Optional<EventInvitee> findByInvitationToken(String token);

    @Query("SELECT COUNT(ei) > 0 FROM EventInvitee ei " +
            "WHERE ei.inviteeEmail = :email " +
            "AND ei.invitation.event.id = :eventId")
    boolean existsByEmailAndEventId(@Param("email") String email,
                                    @Param("eventId") Long eventId);

}
