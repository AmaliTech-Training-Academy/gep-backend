package com.example.auth_service.controller;

import com.example.auth_service.dto.request.OtpVerificationRequest;
import com.example.auth_service.dto.request.RefreshAccessTokenRequest;
import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.CustomApiResponse;
import com.example.auth_service.dto.response.UserCreationResponse;
import com.example.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<CustomApiResponse<?>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request, HttpServletResponse response){
        authService.verifyOtp(request, response);
        return ResponseEntity.ok(CustomApiResponse.success("Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<CustomApiResponse<?>> refreshToken(@CookieValue(name="refreshToken") String refreshToken, HttpServletResponse response){
        authService.refreshAccessToken(refreshToken, response);
        return ResponseEntity.ok(CustomApiResponse.success("Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<CustomApiResponse<?>> logout(HttpServletResponse response){
        authService.logout(response);
        return ResponseEntity.ok(CustomApiResponse.success("Logged out successfully"));
    }

}
