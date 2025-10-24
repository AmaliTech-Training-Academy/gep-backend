package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.OtpVerificationRequest;
import com.example.auth_service.dto.request.ResetPasswordRequest;
import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.UserCreationResponse;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.event.ResetPasswordEvent;
import com.example.auth_service.event.UserRegisteredEvent;
import com.example.auth_service.exception.DuplicateEmailException;
import com.example.auth_service.exception.InactiveAccountException;
import com.example.auth_service.exception.PasswordMismatchException;
import com.example.auth_service.model.Profile;
import com.example.auth_service.model.User;
import com.example.auth_service.model.UserEventStats;
import com.example.auth_service.repository.ProfileRepository;
import com.example.auth_service.repository.UserEventStatsRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.AuthUser;
import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import org.springframework.beans.factory.annotation.Value;

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
    private final JwtUtil jwtUtil;

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

            Profile userProfile = Profile.builder().user(savedUser).build();
            profileRepository.save(userProfile);
            UserEventStats userEventStats = UserEventStats.builder().user(savedUser).build();
            userEventStatsRepository.save(userEventStats);

            sendRegistrationMessageToQueue(savedUser);
            return new UserCreationResponse(savedUser.getId(), savedUser.getFullName());
    }

    private void validateRequest(UserRegistrationRequest registrationRequest){
        if(!registrationRequest.password().equals(registrationRequest.confirmPassword())){
            throw new PasswordMismatchException("Passwords do not match");
        }
        String email = registrationRequest.email().toLowerCase().trim();
        if(userRepository.existsByEmail(email)){
            throw new DuplicateEmailException("Email already registered");
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
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );

            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            if(!authUser.getUser().isActive()){
                throw new InactiveAccountException("User account is inactive");
            }
            otpService.requestLoginOtp(loginRequest.email());
        }catch(BadCredentialsException e){
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public void verifyOtp(OtpVerificationRequest request, HttpServletResponse response){
        boolean isValid = otpService.verifyOtp(request.email(), request.otp());
        if(!isValid){
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = getActiveUserByEmail(request.email());

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        setAuthCookies(response, accessToken, refreshToken);
    }

    @Override
    public void resendOtp(String email) {
        User user = getActiveUserByEmail(email);
        otpService.requestLoginOtp(user.getEmail());
    }

    @Override
    public void refreshAccessToken(String refreshToken, HttpServletResponse response){
        String email = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if(!user.isActive()){
            throw new InactiveAccountException("User account is inactive");
        }

        if(!jwtUtil.validateToken(refreshToken)){
            throw new BadCredentialsException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateAccessToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        setAuthCookies(response, newAccessToken, newRefreshToken);
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


    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken){
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenExpiration))
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
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
