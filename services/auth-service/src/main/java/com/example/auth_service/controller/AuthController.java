package com.example.auth_service.controller;

import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.UserCreationResponse;
import com.example.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserCreationResponse> register(@Valid @RequestBody UserRegistrationRequest registrationRequest){
        UserCreationResponse creationResponse = authService.registerNewUser(registrationRequest);
        return ResponseEntity.ok(creationResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest loginRequest){
        AuthResponse authResponse = authService.loginUser(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

}
