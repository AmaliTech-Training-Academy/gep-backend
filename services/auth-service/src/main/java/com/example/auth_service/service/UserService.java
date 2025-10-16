package com.example.auth_service.service;

import com.example.auth_service.dto.request.UserUpdateRequest;
import com.example.auth_service.dto.response.UserManagementResponse;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.dto.response.UserSummaryReport;
import com.example.auth_service.enums.UserRole;
import org.springframework.data.domain.Page;


public interface UserService {
    UserSummaryReport getUserSummaryReport();
    void updateUserStatus(Long userId);
    Iterable<UserManagementResponse> userSearch(String keyword, int page);
    UserResponse getUserById(Long userId);
    UserResponse updateUser(Long userId, UserUpdateRequest request);
    Page<UserManagementResponse> filterUsers(UserRole role, Boolean status, int page);
}
