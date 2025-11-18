package com.example.auth_service.repository;

import com.example.auth_service.model.User;
import com.example.auth_service.model.UserBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBankAccountRepository extends JpaRepository<UserBankAccount, Long> {
    Optional<UserBankAccount> findByUser(User user);

    boolean existsByUser(User currentUser);
}
