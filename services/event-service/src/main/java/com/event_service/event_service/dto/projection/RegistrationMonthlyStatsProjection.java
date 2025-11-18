package com.event_service.event_service.dto.projection;

public interface RegistrationMonthlyStatsProjection {
    Integer getYear();
    Integer getMonth();
    Long getTotalRegistrations();
}
