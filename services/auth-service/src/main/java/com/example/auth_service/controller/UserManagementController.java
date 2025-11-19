package com.example.auth_service.controller;

import com.example.auth_service.dto.request.UserUpdateRequest;
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

    /**
     * Handles the management of user data and retrieves a summary report containing
     * information about the total number of users, organizers, attendees, deactivated users,
     * and a paginated overview of user details.
     *
     * This method is accessible only to users with the `ADMIN` authority.
     *
     * @return a ResponseEntity containing a CustomApiResponse with a UserSummaryReport object
     */
    @GetMapping("/management")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<UserSummaryReport>> userManagement(){
        CustomApiResponse<UserSummaryReport> response = CustomApiResponse.success(userService.getUserSummaryReport());
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivates a user by updating their status based on the provided user ID.
     * This method requires the requesting user to have admin authority or ownership of the user resource.
     *
     * @param userId the ID of the user whose status will be updated
     * @return a ResponseEntity with no content
     */
    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or @resourceOwner.isOwner(#userId,principal)")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long userId){
        userService.updateUserStatus(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Searches for users based on the provided criteria, including keyword, role, status, and page number.
     * This method is accessible only to users with the `ADMIN` authority.
     *
     * @param keyword the search keyword to filter users by their full name or email (optional)
     * @param role the role of the users to filter (optional)
     * @param status the active status of the users to filter (optional)
     * @param page the page number for pagination (defaults to 0 if not provided)
     * @return a ResponseEntity containing a CustomApiResponse with a paginated list of UserManagementResponse objects
     */
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

    /**
     * Retrieves a user's information by their ID.
     * This method requires the requesting user to have either admin authority
     * or ownership of the user resource.
     *
     * @param userId the ID of the user to be retrieved
     * @return a ResponseEntity containing a CustomApiResponse with the user's details
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @resourceOwner.isOwner(#userId,principal)")
    public ResponseEntity<CustomApiResponse<UserResponse>> getUserById(@PathVariable Long userId){
        CustomApiResponse<UserResponse> response = CustomApiResponse.success(userService.getUserById(userId));
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the details of an existing user with the specified user ID.
     * This method requires the requesting user to have either admin authority or ownership of the user resource.
     *
     * @param userId the ID of the user to be updated
     * @param userUpdateRequestJson the new details for the user, including fields such as full name, email, phone, address, and status
     * @return a ResponseEntity containing a CustomApiResponse with the updated user's details
     */
    @PutMapping(path = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or @resourceOwner.isOwner(#userId, principal)")
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
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid request.");
            }
        }

        // validate DTO
        var violations = validator.validate(userUpdateRequest);
        if(!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }

        CustomApiResponse<UserResponse> response =
                CustomApiResponse.success(userService.updateUser(userId, userUpdateRequest, profilePicture));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-organizers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopOrganizerResponse>> getTopOrganizers(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getTopOrganizers());
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