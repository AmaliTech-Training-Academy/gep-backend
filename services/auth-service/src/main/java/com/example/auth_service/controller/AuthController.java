package com.example.auth_service.controller;

import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.CustomApiResponse;
import com.example.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<CustomApiResponse<?>> register(@Valid @RequestBody UserRegistrationRequest registrationRequest){
        authService.registerNewUser(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CustomApiResponse.success("User registered successfully")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<CustomApiResponse<AuthResponse>> login(@Valid @RequestBody UserLoginRequest loginRequest){
        AuthResponse authResponse = authService.loginUser(loginRequest);
        return ResponseEntity.ok(CustomApiResponse.success("Login successful", authResponse));
    }

}
