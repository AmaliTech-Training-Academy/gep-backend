package com.example.auth_service.service;

import com.example.auth_service.dto.request.UserUpdateRequest;
import com.example.auth_service.dto.response.UserManagementResponse;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.dto.response.UserSummaryReport;
import com.example.auth_service.enums.UserRole;
import com.example.common_libraries.dto.TopOrganizerResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface UserService {
    UserSummaryReport getUserSummaryReport();
    void updateUserStatus(Long userId);
    Page<UserManagementResponse> userSearch(String keyword,UserRole role, Boolean status, int page);
    UserResponse getUserById(Long userId);
    UserResponse updateUser(Long userId, UserUpdateRequest request, MultipartFile profilePicture);
//    Page<UserManagementResponse> filterUsers(UserRole role, Boolean status, int page);
    List<TopOrganizerResponse> getTopOrganizers();
}
