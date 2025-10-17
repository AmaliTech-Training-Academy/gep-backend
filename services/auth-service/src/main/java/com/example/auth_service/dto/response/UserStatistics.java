package com.example.auth_service.dto.response;

public interface UserStatistics {
    long getTotalUsers();
    long getTotalOrganizers();
    long getTotalAttendees();
    long getTotalDeactivatedUsers();
}
