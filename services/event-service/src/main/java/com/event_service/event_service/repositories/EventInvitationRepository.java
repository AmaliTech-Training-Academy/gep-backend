package com.event_service.event_service.repositories;

import com.event_service.event_service.models.EventInvitation;
import com.event_service.event_service.models.enums.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface EventInvitationRepository extends JpaRepository<EventInvitation, Long> {
    Page<EventInvitation> findAllByStatus(InvitationStatus status, Pageable pageable);
    @Query("SELECT ei FROM EventInvitation ei " +
            "LEFT JOIN FETCH ei.invitees " +
            "LEFT JOIN FETCH ei.event " +
            "WHERE ei.id = :invitationId")
    Optional<EventInvitation> findByIdWithInvitees(@Param("invitationId") Long invitationId);

    @Query("SELECT e FROM EventInvitation e WHERE e.status = :status " +
            "AND (LOWER(e.invitationTitle) LIKE %:searchTerm% " +
            "OR LOWER(e.message) LIKE %:searchTerm% " +
            "OR LOWER(e.inviterName) LIKE %:searchTerm%)")
    Page<EventInvitation> findAllByStatusAndSearchTerm(
            InvitationStatus status,
            String searchTerm,
            Pageable pageable
    );

    @Query("SELECT e FROM EventInvitation e WHERE " +
            "LOWER(e.invitationTitle) LIKE %:searchTerm% " +
            "OR LOWER(e.message) LIKE %:searchTerm% " +
            "OR LOWER(e.inviterName) LIKE %:searchTerm%")
    Page<EventInvitation> findAllBySearchTerm(
            String searchTerm,
            Pageable pageable
    );
}