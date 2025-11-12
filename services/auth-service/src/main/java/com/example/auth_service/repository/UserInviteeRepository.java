package com.example.auth_service.repository;

import com.example.auth_service.model.UserInvitee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInviteeRepository extends JpaRepository<UserInvitee, Long> {
    boolean existsByEmail(String email);

    Optional<UserInvitee> findByInvitationToken(String token);
}
