package com.example.auth_service.dto.response;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record UserSummaryReport(
        long totalUsers,
        long totalOrganizers,
        long totalAttendees,
        long totalDeactivatedUsers,
        Page<UserManagementResponse> users
) {
}
