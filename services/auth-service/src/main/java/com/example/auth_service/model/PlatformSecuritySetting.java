package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "platform_security_settings")
public class PlatformSecuritySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform_name", nullable = false, unique = true)
    private String platformName;

    @Column(name = "platform_url", nullable = false, unique = true)
    private String platformUrl;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "platform_description", length = 1000)
    private String platformDescription;

    @Column(name = "maintenance_mode")
    private Boolean maintenanceMode;
}
