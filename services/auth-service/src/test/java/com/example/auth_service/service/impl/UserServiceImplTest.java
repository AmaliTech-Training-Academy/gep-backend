package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.UserUpdateRequest;
import com.example.auth_service.dto.response.UserManagementResponse;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.dto.response.UserSummaryReport;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.exception.ResourceNotFoundException;
import com.example.auth_service.model.Profile;
import com.example.auth_service.model.User;
import com.example.auth_service.model.UserEventStats;
import com.example.auth_service.repository.ProfileRepository;
import com.example.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Profile testProfile;
    private UserEventStats testUserEventStats;
    private UserUpdateRequest testUpdateRequest;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testProfile = Profile.builder()
                .id(1L)
                .phoneNumber("1234567890")
                .address("123 Test Street")
                .profileImageUrl("http://example.com/profile.jpg")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testUserEventStats = UserEventStats
                .builder()
                .id(1L)
                .build();

        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(UserRole.ATTENDEE)
                .isActive(true)
                .profile(testProfile)
                .userEventStats(testUserEventStats)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testProfile.setUser(testUser);
        testUserEventStats.setUser(testUser);

        testUpdateRequest = new UserUpdateRequest(
                "Jane Doe",
                "jane.doe@example.com",
                "9876543210",
                "456 New Avenue",
                false
        );
    }

    // ==================== getUserSummaryReport Tests ====================

    @Test
    void shouldReturnUserSummaryReportSuccessfully() {
        // Arrange
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10, Sort.by("fullName")), 1);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countAllByRole(UserRole.ORGANISER)).thenReturn(3L);
        when(userRepository.countAllByRole(UserRole.ATTENDEE)).thenReturn(7L);
        when(userRepository.countAllByIsActive(false)).thenReturn(2L);

        // Act
        UserSummaryReport result = userService.getUserSummaryReport();

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.totalUsers());
        assertEquals(3L, result.totalOrganizers());
        assertEquals(7L, result.totalAttendees());
        assertEquals(2L, result.totalDeactivatedUsers());
        assertNotNull(result.users());
        assertEquals(1, result.users().getTotalElements());

        verify(userRepository).findAll(any(Pageable.class));
        verify(userRepository).count();
        verify(userRepository).countAllByRole(UserRole.ORGANISER);
        verify(userRepository).countAllByRole(UserRole.ATTENDEE);
        verify(userRepository).countAllByIsActive(false);
    }

    @Test
    void shouldReturnUserSummaryReportWithEmptyUserList() {
        // Arrange
        Page<User> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10, Sort.by("fullName")), 0);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countAllByRole(UserRole.ORGANISER)).thenReturn(0L);
        when(userRepository.countAllByRole(UserRole.ATTENDEE)).thenReturn(0L);
        when(userRepository.countAllByIsActive(false)).thenReturn(0L);

        // Act
        UserSummaryReport result = userService.getUserSummaryReport();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.totalUsers());
        assertEquals(0L, result.totalOrganizers());
        assertEquals(0L, result.totalAttendees());
        assertEquals(0L, result.totalDeactivatedUsers());
        assertEquals(0, result.users().getTotalElements());
    }

    // ==================== updateUserStatus Tests ====================

    @Test
    void shouldToggleStatusFromActiveThenInactiveWhenUserExists() {
        // Arrange
        testUser.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updateUserStatus(1L);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().isActive());
    }

    @Test
    void shouldToggleStatusFromInactiveToActiveWhenUserExists() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updateUserStatus(1L);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().isActive());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringStatusUpdate() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUserStatus(999L)
        );
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenNullUserIdProvidedForStatusUpdate() {
        // Arrange
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserStatus(null));
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== userSearch Tests ====================

    @Test
    void shouldReturnSearchResultsWhenKeywordMatches() {
        // Arrange
        List<User> users = List.of(testUser);
        Page<User> searchPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userRepository.findAllByFullNameContainingIgnoreCase("John", PageRequest.of(0, 10)))
                .thenReturn(searchPage);

        // Act
        Iterable<UserManagementResponse> result = userService.userSearch("John", 0);

        // Assert
        assertNotNull(result);
        List<UserManagementResponse> resultList = new ArrayList<>();
        result.forEach(resultList::add);
        assertEquals(1, resultList.size());
        verify(userRepository).findAllByFullNameContainingIgnoreCase("John", PageRequest.of(0, 10));
    }

    @Test
    void shouldReturnEmptyResultsWhenKeywordDoesNotMatch() {
        // Arrange
        Page<User> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);

        when(userRepository.findAllByFullNameContainingIgnoreCase("NonExistent", PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        // Act
        Iterable<UserManagementResponse> result = userService.userSearch("NonExistent", 0);

        // Assert
        assertNotNull(result);
        List<UserManagementResponse> resultList = new ArrayList<>();
        result.forEach(resultList::add);
        assertEquals(0, resultList.size());
    }

    @Test
    void shouldHandleEmptyKeywordInSearch() {
        // Arrange
        List<User> users = List.of(testUser);
        Page<User> searchPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userRepository.findAllByFullNameContainingIgnoreCase("", PageRequest.of(0, 10)))
                .thenReturn(searchPage);

        // Act
        Iterable<UserManagementResponse> result = userService.userSearch("", 0);

        // Assert
        assertNotNull(result);
        verify(userRepository).findAllByFullNameContainingIgnoreCase("", PageRequest.of(0, 10));
    }

    @Test
    void shouldDefaultToPageZeroWhenNegativePageProvided() {
        // Arrange
        Page<User> searchPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);

        when(userRepository.findAllByFullNameContainingIgnoreCase("John", PageRequest.of(0, 10)))
                .thenReturn(searchPage);

        // Act
        userService.userSearch("John", -5);

        // Assert
        verify(userRepository).findAllByFullNameContainingIgnoreCase("John", PageRequest.of(0, 10));
    }

    @Test
    void shouldHandleValidPageNumberInSearch() {
        // Arrange
        Page<User> searchPage = new PageImpl<>(List.of(testUser), PageRequest.of(2, 10), 1);

        when(userRepository.findAllByFullNameContainingIgnoreCase("John", PageRequest.of(2, 10)))
                .thenReturn(searchPage);

        // Act
        userService.userSearch("John", 2);

        // Assert
        verify(userRepository).findAllByFullNameContainingIgnoreCase("John", PageRequest.of(2, 10));
    }

    // ==================== getUserById Tests ====================

    @Test
    void shouldReturnUserWhenUserExistsById() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.userId());
        assertEquals("John Doe", result.fullName());
        assertEquals("john.doe@example.com", result.email());
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(999L)
        );
        assertEquals("User not found with id: 999", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNullUserIdProvidedForGetById() {
        // Arrange
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(null));
    }

    // ==================== updateUser Tests ====================

    @Test
    void shouldUpdateUserWithAllFieldsChanged() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        UserResponse result = userService.updateUser(1L, testUpdateRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(Profile.class));
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        
        assertEquals("Jane Doe", updatedUser.getFullName());
        assertEquals("jane.doe@example.com", updatedUser.getEmail());
        assertFalse(updatedUser.isActive());
        assertEquals("9876543210", updatedUser.getProfile().getPhoneNumber());
        assertEquals("456 New Avenue", updatedUser.getProfile().getAddress());
    }

    @Test
    void shouldUpdateOnlyChangedFields() {
        // Arrange
        UserUpdateRequest partialUpdate = new UserUpdateRequest(
                "John Doe",  // Same as original
                "new.email@example.com",  // Changed
                "1234567890",  // Same as original
                "123 Test Street",  // Same as original
                true  // Same as original
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        UserResponse result = userService.updateUser(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        
        assertEquals("new.email@example.com", updatedUser.getEmail());
        assertEquals("John Doe", updatedUser.getFullName());  // Unchanged
    }

    @Test
    void shouldNotUpdateWhenNoFieldsChanged() {
        // Arrange
        UserUpdateRequest sameDataRequest = new UserUpdateRequest(
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                "123 Test Street",
                true
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        UserResponse result = userService.updateUser(1L, sameDataRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(999L, testUpdateRequest)
        );
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void shouldThrowExceptionWhenNullUserIdProvidedForUpdate() {
        // Arrange
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> userService.updateUser(null, testUpdateRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldUpdateUserStatusFromTrueToFalse() {
        // Arrange
        testUser.setActive(true);
        UserUpdateRequest statusChangeRequest = new UserUpdateRequest(
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                "123 Test Street",
                false
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        userService.updateUser(1L, statusChangeRequest);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().isActive());
    }

    // ==================== filterUsers Tests ====================

    @Test
    void shouldFilterUsersByRoleAndStatus() {
        // Arrange
        List<User> users = List.of(testUser);
        Page<User> filterPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(filterPage);

        // Act
        Page<UserManagementResponse> result = userService.filterUsers(UserRole.ATTENDEE, true, 0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldFilterUsersByRoleOnly() {
        // Arrange
        List<User> users = List.of(testUser);
        Page<User> filterPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(filterPage);

        // Act
        Page<UserManagementResponse> result = userService.filterUsers(UserRole.ORGANISER, null, 0);

        // Assert
        assertNotNull(result);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldFilterUsersByStatusOnly() {
        // Arrange
        List<User> users = List.of(testUser);
        Page<User> filterPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(filterPage);

        // Act
        Page<UserManagementResponse> result = userService.filterUsers(null, false, 0);

        // Assert
        assertNotNull(result);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldReturnEmptyPageWhenNoUsersMatchFilter() {
        // Arrange
        Page<User> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<UserManagementResponse> result = userService.filterUsers(UserRole.ORGANISER, true, 0);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void shouldDefaultToPageZeroWhenNegativePageProvidedInFilter() {
        // Arrange
        Page<User> filterPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(filterPage);

        // Act
        userService.filterUsers(UserRole.ATTENDEE, true, -3);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
    }

    @Test
    void shouldHandleValidPageNumberInFilter() {
        // Arrange
        Page<User> filterPage = new PageImpl<>(List.of(testUser), PageRequest.of(3, 10), 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(filterPage);

        // Act
        userService.filterUsers(UserRole.ATTENDEE, true, 3);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(3, pageableCaptor.getValue().getPageNumber());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void shouldFilterWithBothNullRoleAndStatus() {
        // Arrange
        Page<User> filterPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(filterPage);

        // Act
        Page<UserManagementResponse> result = userService.filterUsers(null, null, 0);

        // Assert
        assertNotNull(result);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldFilterInactiveOrganizers() {
        // Arrange
        User inactiveOrganizer = User.builder()
                .id(2L)
                .fullName("Inactive Organizer")
                .email("inactive@example.com")
                .password("password")
                .role(UserRole.ORGANISER)
                .userEventStats(testUserEventStats)
                .isActive(false)
                .profile(testProfile)
                .build();

        Page<User> filterPage = new PageImpl<>(List.of(inactiveOrganizer), PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(filterPage);

        // Act
        Page<UserManagementResponse> result = userService.filterUsers(UserRole.ORGANISER, false, 0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}