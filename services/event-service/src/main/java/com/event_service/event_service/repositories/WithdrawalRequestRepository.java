package com.event_service.event_service.repositories;

import com.event_service.event_service.models.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    List<WithdrawalRequest> findByUserId(Long userId);
}
