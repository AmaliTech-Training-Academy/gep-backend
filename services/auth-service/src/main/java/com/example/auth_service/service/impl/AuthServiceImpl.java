package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.OtpVerificationRequest;
import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.UserCreationResponse;
import com.example.auth_service.enums.UserRole;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import org.springframework.beans.factory.annotation.Value;

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

    @Transactional
    public UserCreationResponse registerNewUser(UserRegistrationRequest registrationRequest) {
            if(!registrationRequest.confirmPassword().equals(registrationRequest.password())){
                throw new PasswordMismatchException("Passwords do not match");
            }

            if(userRepository.existsByEmail(registrationRequest.email())){
                throw new DuplicateEmailException("Email already registered");
            }

            User newUser = User.builder()
                    .fullName(registrationRequest.fullName())
                    .email(registrationRequest.email())
                    .password(passwordEncoder.encode(registrationRequest.password()))
                    .role(UserRole.ATTENDEE)
                    .isActive(true)
                    .build();

            User savedUser = userRepository.save(newUser);
            UserRegisteredEvent event = new UserRegisteredEvent(savedUser.getId(), savedUser.getFullName(), savedUser.getEmail());
            sendRegistrationMessageToQueue(event);
            Profile userProfile = Profile.builder().user(savedUser).build();
            profileRepository.save(userProfile);
            UserEventStats userEventStats = UserEventStats.builder().user(savedUser).build();
            userEventStatsRepository.save(userEventStats);
            return new UserCreationResponse(savedUser.getId(), savedUser.getFullName());
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
            otpService.generateOtp(loginRequest.email());
        }catch(BadCredentialsException e){
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public AuthResponse verifyOtp(OtpVerificationRequest request){
        boolean isValid = otpService.verifyOtp(request.email(), request.otp());
        if(!isValid){
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if(!user.isActive()){
            throw new InactiveAccountException("User account is inactive");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken
        );
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken){
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

        return new AuthResponse(
                newAccessToken,
                newRefreshToken
        );
    }

    public void sendRegistrationMessageToQueue(UserRegisteredEvent event){
        try{
        log.info("Sending event to SQS queue");
        String messageBody = objectMapper.writeValueAsString(event);
        sqsClient.sendMessage(builder -> builder.queueUrl(userRegistrationQueueUrl).messageBody(messageBody));
        log.info("Message sent to SQS queue");
        }catch (Exception e){
            log.error("Error sending message to SQS: {}", e.getMessage());
        }
    }

}
