package com.example.auth_service.service.impl;

import com.example.auth_service.dto.projection.TopOrganizerProjection;
import com.example.auth_service.dto.request.UserUpdateRequest;
import com.example.auth_service.dto.response.UserManagementResponse;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.dto.response.UserStatistics;
import com.example.auth_service.dto.response.UserSummaryReport;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.repository.UserEventStatsRepository;
import com.example.common_libraries.dto.TopOrganizerResponse;
import com.example.common_libraries.exception.DuplicateResourceException;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ProfileRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.UserService;
import com.example.auth_service.utils.UserSpecifications;
import com.example.common_libraries.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final S3Service s3Service;
    private final UserEventStatsRepository userEventStatsRepository;

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
        UserStatistics userStats = userRepository.getUserStatistics();
        return UserMapper.toUserSummary(userResponse,userStats);
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
     * Searches for users based on the specified criteria such as keyword, role, and status,
     * and returns a paginated list of matching users.
     *
     * @param keyword a string to search for in user attributes like full name or email;
     *                can be null or empty, in which case it is ignored
     * @param role the role of the user (e.g., ATTENDEE, ORGANISER, ADMIN) to filter by;
     *             can be null, in which case it is ignored
     * @param status a boolean indicating whether to search for active or inactive users;
     *               can be null, in which case it is ignored
     * @param page the page number for pagination, where 0 indicates the first page;
     *             values below 0 are treated as 0
     * @return a {@code Page} of {@code UserManagementResponse}, containing user details
     *         that match the search criteria
     */
    @Override
    public Page<UserManagementResponse> userSearch(String keyword,UserRole role, Boolean status, int page) {
        page = Math.max(page, 0);
        Sort sort = Sort.by("fullName");
        Pageable pageable = PageRequest.of(page, 10,sort);

        // build specification based on user input
        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        if(keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(UserSpecifications.hasKeyword(keyword.trim()));
        }
        if(role != null) {
            spec = spec.and(UserSpecifications.hasRole(role));
        }
        if(status != null) {
            spec = spec.and(UserSpecifications.isActive(status));
        }
        Page<User> searchResults = userRepository.findAll(spec, pageable);

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
     * @param userId         the ID of the user to update
     * @param request        an instance of {@code UserUpdateRequest} containing the new user details
     * @param profilePicture
     * @return a {@code UserResponse} object representing the updated user data
     * @throws ResourceNotFoundException if the user with the given ID is not found
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request, MultipartFile profilePicture) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Optional<User> userWithEmail = userRepository.findByEmail(request.email());
        if(userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)){
            throw new DuplicateResourceException("Email already in use");
        }

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
        if(profilePicture != null){
            log.info("Profile picture is not null: {}", profilePicture.getOriginalFilename());
            // upload to s3 bucket
            String profilePictureUrl = s3Service.uploadImage(profilePicture);
            userToUpdate.getProfile().setProfileImageUrl(profilePictureUrl);
        }

        // save user updates
        userRepository.save(userToUpdate);
        // save profile updates
        profileRepository.save(userToUpdate.getProfile());
        return UserMapper.toUserResponse(userToUpdate);
    }

    @Override
    public List<TopOrganizerResponse> getTopOrganizers() {
        List<TopOrganizerProjection> topOrganizerProjections =
                userEventStatsRepository.findTopOrganizers(PageRequest.of(0, 2));

        return topOrganizerProjections.stream()
                .map(proj -> TopOrganizerResponse
                        .builder()
                        .email(proj.getEmail())
                        .name(proj.getFullName())
                        .eventCount(proj.getTotalEventsCreated())
                        .growthPercentage(proj.getGrowthPercentage())
                        .build()
                )
                .toList();
    }

    @Override
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
