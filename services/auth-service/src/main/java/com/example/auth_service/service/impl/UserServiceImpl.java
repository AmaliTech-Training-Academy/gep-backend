package com.example.auth_service.service.impl;

import com.example.auth_service.dto.projection.TopOrganizerProjection;
import com.example.auth_service.dto.request.UserUpdateRequest;
import com.example.auth_service.dto.response.*;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.repository.UserEventStatsRepository;
import com.example.auth_service.utils.AuthUserUtil;
import com.example.common_libraries.dto.TopOrganizerResponse;
import com.example.common_libraries.dto.UserCreationResponse;
import com.example.common_libraries.dto.UserInfoResponse;
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
    private final AuthUserUtil authUserUtil;


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


    @Override
    public void updateUserStatus(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        boolean currentStatus = user.isActive();
        boolean newStatus = !currentStatus;
        user.setActive(newStatus);
        userRepository.save(user);
    }



    @Override
    public Page<UserManagementResponse> userSearch(String keyword,UserRole role, Boolean status, int page) {
        Page<User> searchResults = filterUserList(page,keyword, role,status);
        return searchResults.map(UserMapper::toUserManagementResponse);
    }

    @Override
    public Page<UserListResponse> getAdminUsers(String keyword, Boolean status, int page) {
        UserRole role = UserRole.ADMIN;
        Page<User> searchResults = filterUserList(page,keyword, role,status);
        return searchResults.map(UserMapper::toUserListResponse);
    }

    private Page<User> filterUserList(int page, String keyword, UserRole role, Boolean status) {
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
        return userRepository.findAll(spec, pageable);
    }



    @Override
    public UserResponse getUserById(Long userId) {
        User currentUser = authUserUtil.getAuthenticatedUser();
        if(!Objects.equals(currentUser.getRole().name(), "ADMIN") && !Objects.equals(currentUser.getId(), userId)){
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserMapper.toUserResponse(user);
    }


    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request, MultipartFile profilePicture) {
        User currentUser = authUserUtil.getAuthenticatedUser();
        if(!Objects.equals(currentUser.getRole().name(), "ADMIN") && !Objects.equals(currentUser.getId(), userId)){
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if(request != null) {
            Optional<User> userWithEmail = userRepository.findByEmail(request.email());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)) {
                throw new DuplicateResourceException("Email already in use");
            }

            // User updates
            if (!Objects.equals(request.fullName(), userToUpdate.getFullName())) {
                userToUpdate.setFullName(request.fullName());
            }
            if (!Objects.equals(request.email(), userToUpdate.getEmail())) {
                userToUpdate.setEmail(request.email());
            }
            if (request.status() != userToUpdate.isActive()) {
                userToUpdate.setActive(request.status());
            }


            // Profile updates
            if (!Objects.equals(request.phone(), userToUpdate.getProfile().getPhoneNumber())) {
                userToUpdate.getProfile().setPhoneNumber(request.phone());
            }
            if (!Objects.equals(request.address(), userToUpdate.getProfile().getAddress())) {
                userToUpdate.getProfile().setAddress(request.address());
            }
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
    public List<UserInfoResponse> getActiveAdmins() {
        List<User> activeAdmins = userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN);
        return activeAdmins.stream()
                .map(UserMapper::toAppUser)
                .toList();
    }

    @Override
    public UserCreationResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserMapper::toUserCreationResponse)
                .orElse(null);
    }
}
