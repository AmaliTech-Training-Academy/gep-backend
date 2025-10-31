package com.event_service.event_service.models;

import com.event_service.event_service.models.enums.InviteStatus;
import com.event_service.event_service.models.enums.InviteeRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_invitations", indexes = {
        @Index(name = "idx_invitee_email", columnList = "inviteeEmail"),
        @Index(name = "idx_invitation_token", columnList = "invitationToken"),
        @Index(name = "idx_event_id", columnList = "event_id"),
})
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invitee_name", nullable = false)
    private String inviteeName;

    @Column(name = "invitation_title", nullable = false)
    private String invitationTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "inviter_id", nullable = false)
    private Long inviterId;

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @Column(name = "invitation_token", unique = true, nullable = false)
    private String invitationToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteeRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;


    @Column(name = "responded_at")
    LocalDateTime respondedAt;

    @Column(name = "expires_at", nullable = false)
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

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

}
