package com.event_service.event_service.models;

import com.event_service.event_service.models.enums.InviteStatus;
import com.event_service.event_service.models.enums.InviteeRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_invitees")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventInvitee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invitee_name", nullable = false)
    private String inviteeName;

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @Column(name = "invitation_token", unique = true)
    private String invitationToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    private EventInvitation invitation;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteeRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status;

    @Column(name = "responded_at")
    LocalDateTime respondedAt;

    @Column(name = "expires_at")
    LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        status = InviteStatus.PENDING;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
