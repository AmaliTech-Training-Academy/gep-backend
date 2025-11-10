package com.example.auth_service.repository;

import com.example.auth_service.dto.projection.TopOrganizerProjection;
import com.example.auth_service.model.UserEventStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEventStatsRepository extends JpaRepository<UserEventStats, Long> {
    @Query(value = """
        SELECT 
            u.full_name as fullName,
            u.email,
            s.total_events_created as totalEventsCreated,
            COALESCE(
                (CAST(s.total_events_created AS DECIMAL) / 
                    NULLIF((SELECT SUM(s2.total_events_created) FROM user_event_stats s2), 0) * 100.0),
                0.0
            ) as growthPercentage
        FROM user_event_stats s
        JOIN users u ON s.user_id = u.id
        ORDER BY s.total_events_created DESC
    """, nativeQuery = true)
    List<TopOrganizerProjection> findTopOrganizers(Pageable pageable);

    Optional<UserEventStats> findByUserId(Long userId);
}
