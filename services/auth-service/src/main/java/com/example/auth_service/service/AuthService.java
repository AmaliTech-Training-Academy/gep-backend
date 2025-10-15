package com.example.auth_service.service;

import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.model.User;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    @Transactional
    void registerNewUser(UserRegistrationRequest registrationRequest);

    AuthResponse loginUser(UserLoginRequest loginRequest);
}
