package com.example.auth_service.controller;

import com.example.auth_service.dto.request.UserUpdateRequest;
import com.example.auth_service.dto.response.UserListResponse;
import com.example.auth_service.dto.response.UserManagementResponse;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.dto.response.UserSummaryReport;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.service.UserService;
import com.example.common_libraries.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserManagementController {
    private final UserService userService;
    private final Validator validator;

    @GetMapping("/management")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<UserSummaryReport>> userManagement(){
        CustomApiResponse<UserSummaryReport> response = CustomApiResponse.success(userService.getUserSummaryReport());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long userId){
        userService.updateUserStatus(userId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<Page<UserManagementResponse>>> userSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page
    ){
        CustomApiResponse<Page<UserManagementResponse>> response = CustomApiResponse.success(userService.userSearch(keyword,role,status, page));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<Page<UserListResponse>>> getAdminUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page
    ){
        CustomApiResponse<Page<UserListResponse>> response = CustomApiResponse.success(userService.getAdminUsers(keyword,status, page));
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANISER','CO_ORGANIZER')")
    public ResponseEntity<CustomApiResponse<UserResponse>> getUserById(@PathVariable Long userId){
        CustomApiResponse<UserResponse> response = CustomApiResponse.success(userService.getUserById(userId));
        return ResponseEntity.ok(response);
    }


    @PutMapping(path = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANISER','CO_ORGANIZER')")
    public ResponseEntity<CustomApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @RequestPart(value = "userUpdateRequest", required = false) String userUpdateRequestJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture
    ) {
        UserUpdateRequest userUpdateRequest = null;

        // Parse JSON string to UserUpdateRequest object
        if (userUpdateRequestJson != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                userUpdateRequest = objectMapper.readValue(userUpdateRequestJson, UserUpdateRequest.class);

                // validate DTO
                var violations = validator.validate(userUpdateRequest);
                if(!violations.isEmpty()){
                    throw new ConstraintViolationException(violations);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid request.");
            }
        }

        CustomApiResponse<UserResponse> response =
                CustomApiResponse.success(userService.updateUser(userId, userUpdateRequest, profilePicture));

        return ResponseEntity.ok(response);
    }

    // Inter-Service Endpoints
    @GetMapping("/top-organizers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopOrganizerResponse>> getTopOrganizers(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getTopOrganizers());
    }

    @GetMapping("/hosts")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANISER','CO_ORGANIZER')")
    public ResponseEntity<List<HostsResponse>> getEventHosts(@RequestParam List<Long> hostIds){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getEventHosts(hostIds));
    }

    @GetMapping("/exists")
    public ResponseEntity<UserCreationResponse> checkUserExists(@RequestParam String email) {
        UserCreationResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/active-admins")
    public ResponseEntity<List<UserInfoResponse>> getActiveAdmins(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getActiveAdmins());
    }
}