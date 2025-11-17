package com.event_service.event_service.repositories;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventInvitation;
import com.event_service.event_service.models.enums.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface EventInvitationRepository extends JpaRepository<EventInvitation, Long> {
    Page<EventInvitation> findAllByStatus(InvitationStatus status, Pageable pageable);
    @Query("SELECT ei FROM EventInvitation ei " +
            "LEFT JOIN FETCH ei.invitees " +
            "LEFT JOIN FETCH ei.event " +
            "WHERE ei.id = :invitationId AND ei.inviterId = :inviterId")
    Optional<EventInvitation> findByIdAndInviterIdWithInvitees(
            @Param("invitationId") Long invitationId,
            @Param("inviterId") Long inviterId
    );

    @Query("SELECT e FROM EventInvitation e WHERE e.status = :status " +
            "AND e.inviterId = :inviterId " +
            "AND (LOWER(e.invitationTitle) LIKE %:searchTerm% " +
            "OR LOWER(e.message) LIKE %:searchTerm% " +
            "OR LOWER(e.inviterName) LIKE %:searchTerm%)")
    Page<EventInvitation> findAllByStatusAndSearchTermAndInviterId(
            InvitationStatus status,
            String searchTerm,
            Long inviterId,
            Pageable pageable
    );

    @Query("SELECT e FROM EventInvitation e WHERE e.status = :status " +
            "AND e.inviterId = :inviterId")
    Page<EventInvitation> findAllByStatusAndInviterId(
            InvitationStatus status,
            Long inviterId,
            Pageable pageable
    );

    @Query("SELECT e FROM EventInvitation e WHERE " +
            "e.inviterId = :inviterId AND (" +
            "LOWER(e.invitationTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(e.message) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(e.inviterName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<EventInvitation> findAllBySearchTermAndInviterId(
            @Param("searchTerm") String searchTerm,
            @Param("inviterId") Long inviterId,
            Pageable pageable
    );

    @Query("SELECT e FROM EventInvitation e WHERE e.inviterId = :inviterId")
    Page<EventInvitation> findAllByInviterId(
            @Param("inviterId") Long inviterId,
            Pageable pageable
    );

    List<EventInvitation> findAllByEvent(Event event);
}