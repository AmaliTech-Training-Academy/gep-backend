package com.example.auth_service.controller;

import com.example.auth_service.dto.request.*;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.common_libraries.dto.UserCreationResponse;
import com.example.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.common_libraries.dto.CustomApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<CustomApiResponse<UserCreationResponse>> register(@Valid @RequestBody UserRegistrationRequest registrationRequest){
        UserCreationResponse creationResponse = authService.registerNewUser(registrationRequest);
        return ResponseEntity.ok(CustomApiResponse.success("User registered successfully", creationResponse));
    }

    @PostMapping("/register-invitee")
    public UserCreationResponse registerInvitee(@Valid @RequestBody InviteeAccountCreationRequest inviteeRequest){
        return authService.registerInvitee(inviteeRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<CustomApiResponse<?>> login(@Valid @RequestBody UserLoginRequest loginRequest){
        authService.loginUser(loginRequest);
        return ResponseEntity.ok(CustomApiResponse.success("OTP sent to user's email"));
    }

    @PostMapping("/admin-login")
    public ResponseEntity<CustomApiResponse<AuthResponse>> adminLogin(@Valid @RequestBody UserLoginRequest request, HttpServletResponse response){
        AuthResponse authResponse = authService.adminLogin(request, response);
        return ResponseEntity.ok(CustomApiResponse.success("Login Successful", authResponse));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<CustomApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request, HttpServletResponse response){
        AuthResponse authResponse = authService.verifyOtp(request, response);
        return ResponseEntity.ok(CustomApiResponse.success("OTP verified successfully", authResponse));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<CustomApiResponse<?>> resendOtp(@RequestBody EmailRequest request){
        authService.resendOtp(request.email());
        return ResponseEntity.ok(CustomApiResponse.success("OTP resent successfully"));
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

    @PostMapping("/forgot-password")
    public ResponseEntity<CustomApiResponse<?>> forgotPassword(@Valid @RequestBody EmailRequest request){
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok(CustomApiResponse.success("Password reset email sent"));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomApiResponse<AuthResponse>> getAuthenticatedUser(){
        AuthResponse authResponse = authService.loggedInUser();
        return ResponseEntity.ok(CustomApiResponse.success("Authenticated user fetched successfully", authResponse));
    }



    @PostMapping("/reset-password")
    public ResponseEntity<CustomApiResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        authService.resetPassword(request);
        return ResponseEntity.ok(CustomApiResponse.success("Password has been reset successfully"));
    }

}
