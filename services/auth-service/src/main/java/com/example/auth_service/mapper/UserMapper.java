package com.example.auth_service.mapper;

import com.example.auth_service.dto.response.UserManagementResponse;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.dto.response.UserStatistics;
import com.example.auth_service.dto.response.UserSummaryReport;
import com.example.auth_service.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public class UserMapper {
    private UserMapper(){
        throw new IllegalStateException("Utility class");
    }
    public static UserManagementResponse toUserManagementResponse(User user){
        return UserManagementResponse
                .builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.isActive())
                .profileImageUrl(user.getProfile().getProfileImageUrl())
                .eventsOrganized(user.getUserEventStats().getTotalEventsCreated())
                .eventsAttended(user.getUserEventStats().getTotalEventsAttended())
                .build();
    }

    public static UserSummaryReport toUserSummary(Page<UserManagementResponse> userManagementResponses, UserStatistics userStats){
        return UserSummaryReport
                .builder()
                .totalUsers(userStats.getTotalUsers())
                .totalOrganizers(userStats.getTotalOrganizers())
                .totalAttendees(userStats.getTotalAttendees())
                .totalDeactivatedUsers(userStats.getTotalDeactivatedUsers())
                .users(userManagementResponses)
                .build();
    }

    public static UserResponse toUserResponse(User user){
        return UserResponse
                .builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getProfile().getPhoneNumber())
                .address(user.getProfile().getAddress())
                .profileImageUrl(user.getProfile().getProfileImageUrl())
                .status(user.isActive())
                .build();
    }
}
