package com.event_service.event_service.dto.projection;

public interface EventMonthlyStatsProjection {
    Integer getYear();
    Integer getMonth();
    Long getTotalEventsCreated();
}
