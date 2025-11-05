package com.event_service.event_service.repositories;

import com.event_service.event_service.models.EventInvitation;
import com.event_service.event_service.models.enums.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EventInvitationRepository extends JpaRepository<EventInvitation, Long> {
    Page<EventInvitation> findAllByStatus(InvitationStatus status, Pageable pageable);
}