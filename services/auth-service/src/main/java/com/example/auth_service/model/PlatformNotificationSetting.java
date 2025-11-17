package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "platform_notification_settings")
public class PlatformNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean eventCreation;

    private Boolean paymentFailures;

    private Boolean platformErrors;
}
