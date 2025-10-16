package com.example.auth_service.repository;

import com.example.auth_service.enums.UserRole;
import com.example.auth_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // number of all organizers
    long countAllByRole(UserRole role);

    long countAllByIsActive(boolean isActive);

    Page<User> findAllByFullNameContainingIgnoreCase(String keyword, Pageable pageable);
}
