package com.example.auth_service.repository;

import com.example.auth_service.dto.response.UserStatistics;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
        SELECT
            COUNT(u) AS totalUsers,
            SUM(CASE WHEN u.role = 'ORGANISER' THEN 1 ELSE 0 END) AS totalOrganizers,
            SUM(CASE WHEN u.role = 'ATTENDEE' THEN 1 ELSE 0 END) AS totalAttendees,
            SUM(CASE WHEN u.isActive = false THEN 1 ELSE 0 END) AS totalDeactivatedUsers
        FROM User u
    """)
    UserStatistics getUserStatistics();

    List<User> findAllByRole(UserRole userRole);
}
