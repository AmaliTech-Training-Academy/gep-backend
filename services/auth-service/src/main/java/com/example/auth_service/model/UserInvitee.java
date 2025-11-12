package com.example.auth_service.model;

import com.example.auth_service.enums.UserRole;
import com.example.common_libraries.enums.InviteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_invitees")
public class UserInvitee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invitation_token", nullable = true, unique = true)
    private String invitationToken;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, name="email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="role")
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private InviteStatus status;

    @Column(name = "expires_at", nullable = true)
    private LocalDate expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    private UserInvitation invitation;

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
