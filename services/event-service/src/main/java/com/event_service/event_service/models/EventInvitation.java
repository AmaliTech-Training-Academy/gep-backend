package com.event_service.event_service.models;

import com.event_service.event_service.models.enums.InvitationStatus;
import com.event_service.event_service.models.enums.InviteStatus;
import com.event_service.event_service.models.enums.InviteeRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event_invitations", indexes = {
        @Index(name = "idx_event_id", columnList = "event_id")
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

    @Column(name = "invitation_title", nullable = false)
    private String invitationTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "inviter_id", nullable = false)
    private Long inviterId;

    @Column(name = "inviter_name")
    private String inviterName;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.SEND;

    @Column(columnDefinition = "TEXT")
    private String message;

    @OneToMany(mappedBy = "invitation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventInvitee> invitees = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
