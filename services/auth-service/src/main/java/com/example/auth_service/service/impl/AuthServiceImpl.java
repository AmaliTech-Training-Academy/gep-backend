package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.exception.DuplicateEmailException;
import com.example.auth_service.exception.PasswordMismatchException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerNewUser(UserRegistrationRequest registrationRequest) {
            if(!registrationRequest.passwordConfirmation().equals(registrationRequest.password())){
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

            return userRepository.save(newUser);
    }
}
