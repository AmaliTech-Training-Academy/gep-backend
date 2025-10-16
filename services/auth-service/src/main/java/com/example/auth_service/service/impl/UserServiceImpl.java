package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.UserUpdateRequest;
import com.example.auth_service.dto.response.UserManagementResponse;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.dto.response.UserSummaryReport;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.exception.ResourceNotFoundException;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ProfileRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.UserService;
import com.example.auth_service.utils.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    /**
     * Generates a summary report of user-related metrics including total users, total organizers,
     * total attendees, total deactivated users, and a paginated list of users.
     *
     * @return a UserSummaryReport object containing the user summary metrics and a paginated
     *         list of UserManagementResponse objects.
     */
    @Override
    public UserSummaryReport getUserSummaryReport() {
        // Fetch paginated user data
        int page = 0;
        int paginateBy = 10;

        Sort sort = Sort.by("fullName");
        Pageable pageable = PageRequest.of(page, paginateBy, sort);
        Page<User> users = userRepository.findAll(pageable);

        Page<UserManagementResponse> userResponse = users.map(UserMapper::toUserManagementResponse);

        // Overview Summary metrics
        long totalUsers = userRepository.count();
        long totalOrganizers = userRepository.countAllByRole(UserRole.ORGANISER);
        long totalAttendees = userRepository.countAllByRole(UserRole.ATTENDEE);
        long totalDeactivatedUsers = userRepository.countAllByIsActive(false);
        return UserMapper.toUserSummary(userResponse,totalUsers,totalOrganizers,totalAttendees,totalDeactivatedUsers);
    }

    /**
     * Updates the activation status of a user. If the user is currently active,
     * it deactivates the user, and vice versa.
     *
     * @param userId the ID of the user whose active status needs to be updated
     * @throws ResourceNotFoundException if the user with the given ID does not exist
     */
    @Override
    public void updateUserStatus(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        boolean currentStatus = user.isActive();
        boolean newStatus = !currentStatus;
        user.setActive(newStatus);
        userRepository.save(user);
    }

    /**
     * Searches for users whose full names contain the specified keyword, ignoring case sensitivity.
     * Results are returned in a paginated format.
     *
     * @param keyword the keyword to search for in user full names
     * @param page the page number to retrieve; if the provided page is less than 0, it defaults to 0
     * @return an Iterable containing UserManagementResponse objects representing the search results
     */
    @Override
    public Iterable<UserManagementResponse> userSearch(String keyword, int page) {
        page = Math.max(page, 0);
        Pageable pageable = PageRequest.of(page, 10);
        Page<User> searchResults = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);

        return searchResults.map(UserMapper::toUserManagementResponse);
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId the ID of the user to retrieve
     * @return a UserResponse object containing the user's details
     * @throws ResourceNotFoundException if no user is found with the specified ID
     */
    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserMapper.toUserResponse(user);
    }

    /**
     * Updates the details and profile information of an existing user based on the provided request.
     * Fields in the user object are updated only if there are changes.
     *
     * @param userId the ID of the user to update
     * @param request an instance of {@code UserUpdateRequest} containing the new user details
     * @return a {@code UserResponse} object representing the updated user data
     * @throws ResourceNotFoundException if the user with the given ID is not found
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // User updates
        if(!Objects.equals(request.fullName(), userToUpdate.getFullName())){
            userToUpdate.setFullName(request.fullName());
        }
        if(!Objects.equals(request.email(), userToUpdate.getEmail())){
            userToUpdate.setEmail(request.email());
        }
        if(request.status() != userToUpdate.isActive()){
            userToUpdate.setActive(request.status());
        }

        // Profile updates
        if(!Objects.equals(request.phone(), userToUpdate.getProfile().getPhoneNumber())){
            userToUpdate.getProfile().setPhoneNumber(request.phone());
        }
        if(!Objects.equals(request.address(), userToUpdate.getProfile().getAddress())){
            userToUpdate.getProfile().setAddress(request.address());
        }

        // save user updates
        userRepository.save(userToUpdate);
        // save profile updates
        profileRepository.save(userToUpdate.getProfile());
        return UserMapper.toUserResponse(userToUpdate);
    }

    /**
     * Filters users based on their role and status and returns the results
     * in a paginated format.
     *
     * @param role the role of the users to filter (e.g., ORGANIZER, ATTENDEE)
     * @param status the activation status of the users to filter (true for active, false for inactive)
     * @param page the page number of the results to retrieve; if less than 0, it defaults to 0
     * @return a Page containing filtered UserManagementResponse objects
     */
    @Override
    public Page<UserManagementResponse> filterUsers(UserRole role, Boolean status, int page) {
        int pageNumber = Math.max(page,0);
        Pageable pageable = PageRequest.of(pageNumber,10);

        // generate filter specification
        Specification<User> spec = UserSpecifications
                .hasRole(role)
                .and(UserSpecifications.isActive(status));

        Page<User> filterResults = userRepository.findAll(spec,pageable);

        return filterResults.map(UserMapper::toUserManagementResponse);
    }
}
