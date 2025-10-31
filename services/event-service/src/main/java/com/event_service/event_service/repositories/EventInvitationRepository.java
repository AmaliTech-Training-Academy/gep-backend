package com.event_service.event_service.repositories;

import com.event_service.event_service.models.EventInvitation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventInvitationRepository extends JpaRepository<EventInvitation, Long> {
    boolean existsByEventIdAndInviteeEmail(@NotBlank(message = "Event ID is required.") Long event, @NotBlank(message = "Invitee email is required.") @Email(message = "Invalid email address.") String s);
}
