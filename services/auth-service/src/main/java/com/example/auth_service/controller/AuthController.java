package com.example.auth_service.controller;

import com.example.auth_service.dto.request.OtpVerificationRequest;
import com.example.auth_service.dto.request.RefreshAccessTokenRequest;
import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.UserCreationResponse;
import com.example.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CustomApiResponse<?>> login(@Valid @RequestBody UserLoginRequest loginRequest){
        authService.loginUser(loginRequest);
        return ResponseEntity.ok(CustomApiResponse.success("OTP sent to user's email"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request){
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshAccessTokenRequest request){
        AuthResponse authResponse = authService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

}
