package com.example.auth_service.repository;

import com.example.auth_service.model.UserEventStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEventStatsRepository extends JpaRepository<UserEventStats, Long> {
}
