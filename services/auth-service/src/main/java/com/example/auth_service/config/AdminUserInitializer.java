package com.example.auth_service.config;

import com.example.auth_service.enums.UserRole;
import com.example.auth_service.model.Profile;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ProfileRepository;
import com.example.auth_service.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("admin@example.com")
    private String adminEmail;

    @Value("Admin@123")
    private String adminPassword;

    @Value("System Administrator")
    private String adminFullName;

    @PostConstruct
    @Transactional
    public void init() {
        createAdminUser();
    }

    private void createAdminUser() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists: {}", adminEmail);
            return;
        }

        log.info("Initializing admin user...");

        User admin = User.builder()
                .fullName(adminFullName)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();

        userRepository.save(admin);

        Profile userProfile = Profile.builder().user(admin).build();
        profileRepository.save(userProfile);

    }
}