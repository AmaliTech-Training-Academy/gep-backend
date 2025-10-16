package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.UserCreationResponse;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.exception.DuplicateEmailException;
import com.example.auth_service.exception.InactiveAccountException;
import com.example.auth_service.exception.PasswordMismatchException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.AuthUser;
import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

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
            return new UserCreationResponse(savedUser.getId(), savedUser.getFullName());
    }

    public AuthResponse loginUser(UserLoginRequest loginRequest){
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );

            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            if(!authUser.getUser().isActive()){
                throw new InactiveAccountException("User account is inactive");
            }
            String accessToken = jwtUtil.generateAccessToken(authUser.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(authUser.getUsername());

            return new AuthResponse(
                    accessToken,
                    refreshToken
            );

        }catch(BadCredentialsException e){
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}
