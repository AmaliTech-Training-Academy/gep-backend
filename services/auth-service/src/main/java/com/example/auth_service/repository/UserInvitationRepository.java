package com.example.auth_service.repository;

import com.example.auth_service.model.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
}
