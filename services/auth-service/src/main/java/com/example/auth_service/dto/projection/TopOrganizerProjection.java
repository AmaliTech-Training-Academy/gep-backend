package com.example.auth_service.dto.projection;

public interface TopOrganizerProjection {
    String getFullName();
    String getEmail();
    Long getTotalEventsCreated();
    Double getGrowthPercentage();
}

