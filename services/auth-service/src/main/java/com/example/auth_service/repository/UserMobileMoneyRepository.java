package com.example.auth_service.repository;

import com.example.auth_service.model.User;
import com.example.auth_service.model.UserMobileMoney;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMobileMoneyRepository extends JpaRepository<UserMobileMoney, Long> {
    boolean existsByUser(User user);

    Optional<UserMobileMoney> findByUser(User currentUser);
}
