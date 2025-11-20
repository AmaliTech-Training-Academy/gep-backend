package com.example.auth_service.service.impl;

import com.example.auth_service.dto.projection.TopOrganizerProjection;
import com.example.auth_service.dto.request.UserUpdateRequest;
import com.example.auth_service.dto.response.*;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.model.Profile;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ProfileRepository;
import com.example.auth_service.repository.UserEventStatsRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.utils.AuthUserUtil;
import com.example.common_libraries.dto.TopOrganizerResponse;
import com.example.common_libraries.dto.UserCreationResponse;
import com.example.common_libraries.dto.UserInfoResponse;
import com.example.common_libraries.exception.DuplicateResourceException;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.example.common_libraries.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock ProfileRepository profileRepository;
    @Mock S3Service s3Service;
    @Mock UserEventStatsRepository userEventStatsRepository;
    @Mock AuthUserUtil authUserUtil;

    @InjectMocks UserServiceImpl userService;

    private User adminUser;
    private User regularUser;
    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        profile.setId(1L);
        profile.setAddress("Old Addr");
        profile.setPhoneNumber("111");
        profile.setProfileImageUrl("old.png");

        regularUser = new User();
        regularUser.setId(5L);
        regularUser.setEmail("test@test.com");
        regularUser.setFullName("Test User");
        regularUser.setActive(true);
        regularUser.setRole(UserRole.ORGANISER);
        regularUser.setProfile(profile);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@test.com");
        adminUser.setFullName("Admin");
        adminUser.setActive(true);
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setProfile(profile);
    }

    // ----------------------------------------------------------
    // getUserById
    // ----------------------------------------------------------
    @Test
    void getUserById_shouldThrow_whenNonAdminAccessesAnotherUser() {
        when(authUserUtil.getAuthenticatedUser()).thenReturn(regularUser);

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    void getUserById_shouldReturn_whenAdminAccessesAnyone() {
        when(authUserUtil.getAuthenticatedUser()).thenReturn(adminUser);
        when(userRepository.findById(5L)).thenReturn(Optional.of(regularUser));

        UserResponse res = userService.getUserById(5L);
        assertNotNull(res);
        assertEquals("Test User", res.fullName());
    }

    @Test
    void getUserById_shouldReturn_whenUserAccessesSelf() {
        when(authUserUtil.getAuthenticatedUser()).thenReturn(regularUser);
        when(userRepository.findById(5L)).thenReturn(Optional.of(regularUser));

        UserResponse res = userService.getUserById(5L);
        assertNotNull(res);
        assertEquals("Test User", res.fullName());
    }

    // ----------------------------------------------------------
    // updateUser
    // ----------------------------------------------------------
    @Test
    void updateUser_shouldThrow_whenNonAdminUpdatesAnotherUser() {
        when(authUserUtil.getAuthenticatedUser()).thenReturn(regularUser);

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(999L, null, null));
    }

    @Test
    void updateUser_shouldThrow_whenUserNotFound() {
        when(authUserUtil.getAuthenticatedUser()).thenReturn(adminUser);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(99L, null, null));
    }

    @Test
    void updateUser_shouldThrow_whenEmailExists() {
        UserUpdateRequest req = new UserUpdateRequest("New", "taken@mail.com", "0548730194", "123", true);

        when(authUserUtil.getAuthenticatedUser()).thenReturn(adminUser);
        when(userRepository.findById(5L)).thenReturn(Optional.of(regularUser));

        User another = new User();
        another.setId(9L);
        another.setEmail("taken@mail.com");

        when(userRepository.findByEmail("taken@mail.com"))
                .thenReturn(Optional.of(another));

        assertThrows(DuplicateResourceException.class,
                () -> userService.updateUser(5L, req, null));
    }

    @Test
    void updateUser_shouldUpdateWithoutProfilePicture() {
        UserUpdateRequest req = new UserUpdateRequest("New Name", "new@mail.com", "0548730194", "222", false);

        when(authUserUtil.getAuthenticatedUser()).thenReturn(adminUser);
        when(userRepository.findById(5L)).thenReturn(Optional.of(regularUser));
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());

        UserResponse res = userService.updateUser(5L, req, null);

        assertEquals("New Name", res.fullName());
        assertEquals("new@mail.com", res.email());
        assertFalse(res.status());
    }

    @Test
    void updateUser_shouldUploadProfilePicture() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("img.png");

        when(authUserUtil.getAuthenticatedUser()).thenReturn(adminUser);
        when(userRepository.findById(5L)).thenReturn(Optional.of(regularUser));

        when(s3Service.uploadImage(file)).thenReturn("https://s3/img.png");

        UserResponse res = userService.updateUser(5L, null, file);

        assertEquals("https://s3/img.png", res.profileImageUrl());
    }

    // ----------------------------------------------------------
    // userSearch
    // ----------------------------------------------------------
    @Test
    void userSearch_shouldReturnPagedResults() {
        Page<User> page = new PageImpl<>(List.of(regularUser));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<UserManagementResponse> result =
                userService.userSearch("test", UserRole.ORGANISER, true, 0);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test User", result.getContent().get(0).fullName());
    }

    // ----------------------------------------------------------
    // getAdminUsers
    // ----------------------------------------------------------
    @Test
    void getAdminUsers_shouldReturnList() {
        Page<User> page = new PageImpl<>(List.of(adminUser));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<UserListResponse> result = userService.getAdminUsers("admin", true, 0);

        assertEquals(1, result.getTotalElements());
        assertEquals("Admin", result.getContent().get(0).fullName());
    }

    // ----------------------------------------------------------
    // getTopOrganizers
    // ----------------------------------------------------------
    @Test
    void getTopOrganizers_shouldReturnList() {
        TopOrganizerProjection projection = mock(TopOrganizerProjection.class);
        when(projection.getEmail()).thenReturn("aaa@test.com");
        when(projection.getFullName()).thenReturn("AAA");
        when(projection.getTotalEventsCreated()).thenReturn(10L);
        when(projection.getGrowthPercentage()).thenReturn(50.0);

        when(userEventStatsRepository.findTopOrganizers(any()))
                .thenReturn(List.of(projection));

        List<TopOrganizerResponse> list = userService.getTopOrganizers();

        assertEquals(1, list.size());
        assertEquals("AAA", list.get(0).name());
        assertEquals(10L, list.get(0).eventCount());
    }

    // ----------------------------------------------------------
    // getActiveAdmins
    // ----------------------------------------------------------
    @Test
    void getActiveAdmins_shouldReturnAdminList() {
        when(userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN))
                .thenReturn(List.of(adminUser));

        List<UserInfoResponse> result = userService.getActiveAdmins();

        assertEquals(1, result.size());
        assertEquals("Admin", result.get(0).fullName());
    }

    // ----------------------------------------------------------
    // getUserByEmail
    // ----------------------------------------------------------
    @Test
    void getUserByEmail_shouldReturnResponse() {
        when(userRepository.findByEmail("x@test.com"))
                .thenReturn(Optional.of(regularUser));

        UserCreationResponse res = userService.getUserByEmail("x@test.com");

        assertNotNull(res);
        assertEquals(5L, res.id());
    }

    @Test
    void getUserByEmail_shouldReturnNullWhenNotFound() {
        when(userRepository.findByEmail("none@test.com"))
                .thenReturn(Optional.empty());

        assertNull(userService.getUserByEmail("none@test.com"));
    }
}
