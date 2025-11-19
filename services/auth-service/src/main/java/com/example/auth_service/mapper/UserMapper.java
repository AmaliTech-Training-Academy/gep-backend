package com.example.auth_service.mapper;

import com.example.auth_service.dto.response.*;
import com.example.auth_service.model.Profile;
import com.example.auth_service.model.User;
import com.example.auth_service.model.UserEventStats;
import com.example.common_libraries.dto.AppUser;
import com.example.common_libraries.dto.UserCreationResponse;
import com.example.common_libraries.dto.UserInfoResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

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
                .profileImageUrl(
                        Optional.ofNullable(user.getProfile())
                                .map(Profile::getProfileImageUrl)
                                .orElse(null)
                )
                .eventsOrganized(
                        Optional.ofNullable(user.getUserEventStats())
                                .map(UserEventStats::getTotalEventsAttended)
                                .orElse(0)
                )
                .eventsAttended(
                        Optional.ofNullable(user.getUserEventStats())
                                .map(UserEventStats::getTotalEventsAttended)
                                .orElse(0)
                )
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

    public static UserCreationResponse toUserCreationResponse(User user){
        return new UserCreationResponse(user.getId(), user.getEmail());
    }

    public static UserInfoResponse toAppUser(User user){
        return new UserInfoResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public static UserListResponse toUserListResponse(User user){
        return new UserListResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive(),
                Optional.ofNullable(user.getProfile())
                        .map(Profile::getProfileImageUrl)
                        .orElse(null)
        );
    }
}
