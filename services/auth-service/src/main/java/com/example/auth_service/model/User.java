package com.example.auth_service.model;

import com.example.auth_service.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users",indexes = {
        @Index(name = "idx_user_email", columnList = "email")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "is_active")
    private boolean isActive;

    private String profileImageUrl;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String address;

    @Column(name = "created_at", nullable =false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
