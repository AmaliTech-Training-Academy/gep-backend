package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.*;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.UserCreationResponse;
import com.example.auth_service.enums.UserRole;
import com.example.common_libraries.exception.BadRequestException;
import com.example.common_libraries.exception.DuplicateResourceException;
import com.example.common_libraries.exception.InactiveAccountException;
import com.example.auth_service.model.Profile;
import com.example.auth_service.model.User;
import com.example.auth_service.model.UserEventStats;
import com.example.auth_service.repository.ProfileRepository;
import com.example.auth_service.repository.UserEventStatsRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.AuthUser;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.OtpService;
import com.example.common_libraries.dto.queue_events.UserRegisteredEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import org.springframework.beans.factory.annotation.Value;
import com.example.common_libraries.utils.JWTUtil;

import java.time.Duration;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserEventStatsRepository userEventStatsRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final JWTUtil jwtUtil;

    @Value("${sqs.user-registration-queue-url}")
    private String userRegistrationQueueUrl;

    @Value("${application.security.jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public UserCreationResponse registerNewUser(UserRegistrationRequest registrationRequest) {
            validateRequest(registrationRequest);
            User savedUser = createAndSaveUser(registrationRequest);

            createUserRelatedEntities(savedUser);
            sendRegistrationMessageToQueue(savedUser);
            return new UserCreationResponse(savedUser.getId(), savedUser.getFullName());
    }

    @Override
    @Transactional
    public UserCreationResponse registerInvitee(InviteeAccountCreationRequest inviteeRequest) {
        String email = inviteeRequest.email().toLowerCase().trim();
        if(userRepository.existsByEmail(email)){
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .fullName(inviteeRequest.fullName())
                .email(inviteeRequest.email().toLowerCase().trim())
                .password(passwordEncoder.encode(inviteeRequest.password()))
                .role(inviteeRequest.role())
                .isActive(true)
                .build();
        userRepository.save(user);

        createUserRelatedEntities(user);
        return new UserCreationResponse(user.getId(), user.getFullName());
    }

    @Transactional
    protected void createUserRelatedEntities(User user) {
        Profile userProfile = Profile.builder().user(user).build();
        profileRepository.save(userProfile);

        UserEventStats userEventStats = UserEventStats.builder().user(user).build();
        userEventStatsRepository.save(userEventStats);
    }

    private void validateRequest(UserRegistrationRequest registrationRequest){
        if(!registrationRequest.password().equals(registrationRequest.confirmPassword())){
            throw new BadRequestException("Passwords do not match");
        }
        String email = normalizeEmail(registrationRequest.email());
        if(userRepository.existsByEmail(email)){
            throw new DuplicateResourceException("Email already registered");
        }
    }

    private User createAndSaveUser(UserRegistrationRequest registrationRequest){
        User user = User.builder()
                .fullName(registrationRequest.fullName())
                .email(registrationRequest.email().toLowerCase().trim())
                .password(passwordEncoder.encode(registrationRequest.password()))
                .role(UserRole.ORGANISER)
                .isActive(true)
                .build();
        return userRepository.save(user);
    }

    public void sendRegistrationMessageToQueue(User savedUser){
        try{
            UserRegisteredEvent event = new UserRegisteredEvent(savedUser.getId(), savedUser.getFullName(), savedUser.getEmail());
            String messageBody = objectMapper.writeValueAsString(event);
            sqsClient.sendMessage(builder -> builder.queueUrl(userRegistrationQueueUrl).messageBody(messageBody));
            log.info("Message sent to SQS queue");
        }catch (Exception e){
            log.error("Error sending message to SQS: {}", e.getMessage());
        }
    }


    @Override
    public void loginUser(UserLoginRequest loginRequest){
        AuthUser authUser = getAuthenticatedUser(loginRequest);
        if(!authUser.getUser().isActive()){
            throw new InactiveAccountException("User account is inactive");
        }
        otpService.requestLoginOtp(loginRequest.email());
    }

    public AuthResponse adminLogin(UserLoginRequest loginRequest, HttpServletResponse response){
        AuthUser authUser = getAuthenticatedUser(loginRequest);
        User user = getActiveUserByEmail(authUser.getUsername());
        if(user.getRole() != UserRole.ADMIN){
            throw new AuthorizationDeniedException("You are not allow to login as admin");
        }
        setAuthCookies(response, user);
        return new AuthResponse(user.getId(), user.getEmail(), user.getRole());
    }

    private AuthUser getAuthenticatedUser(UserLoginRequest loginRequest){
        try{
            String normalizedEmail = normalizeEmail(loginRequest.email());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, loginRequest.password())
            );
            return (AuthUser) authentication.getPrincipal();
        }catch(BadCredentialsException e){
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public AuthResponse verifyOtp(OtpVerificationRequest request, HttpServletResponse response){
        boolean isValid = otpService.verifyOtp(request.email(), request.otp());
        if(!isValid){
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = getActiveUserByEmail(request.email());
        setAuthCookies(response, user);
        return new AuthResponse(user.getId(), user.getEmail(), user.getRole());
    }

    @Override
    public void resendOtp(String email) {
        User user = getActiveUserByEmail(normalizeEmail(email));
        otpService.requestLoginOtp(user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email != null ? email.toLowerCase().trim() : null;
    }

    @Override
    public void refreshAccessToken(String refreshToken, HttpServletResponse response){
        jwtUtil.validateToken(refreshToken); // Throws InvalidTokenException if invalid

        String email = jwtUtil.extractUsername(refreshToken);
        User user = getActiveUserByEmail(email);
        setAuthCookies(response, user);
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = getActiveUserByEmail(email);
        otpService.requestResetPasswordOtp(email, user.getFullName());
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        boolean isValid = otpService.verifyOtp(resetPasswordRequest.email(), resetPasswordRequest.otp());
        if(!isValid){
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = getActiveUserByEmail(resetPasswordRequest.email());
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.password()));
        userRepository.save(user);
    }

    private User getActiveUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        if(!user.isActive()){
            throw new InactiveAccountException("User account is inactive");
        }
        return user;
    }

    @Override
    public void logout(HttpServletResponse response){
        clearAuthCookies(response);
    }


    private void setAuthCookies(HttpServletResponse response, User user){
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofMillis(accessTokenExpiration))
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    private void clearAuthCookies(HttpServletResponse response){
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

}
