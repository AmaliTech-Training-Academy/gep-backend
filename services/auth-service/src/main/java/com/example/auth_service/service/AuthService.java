package com.example.auth_service.service;

import com.example.auth_service.dto.request.OtpVerificationRequest;
import com.example.auth_service.dto.request.ResetPasswordRequest;
import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.UserCreationResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    @Transactional
    UserCreationResponse registerNewUser(UserRegistrationRequest registrationRequest);

    void loginUser(UserLoginRequest loginRequest);

    AuthResponse adminLogin(UserLoginRequest loginRequest, HttpServletResponse response);

    AuthResponse verifyOtp(OtpVerificationRequest request, HttpServletResponse response);

    void resendOtp(String email);

    void refreshAccessToken(String refreshToken, HttpServletResponse response);

    void requestPasswordReset(String email);

    void resetPassword(ResetPasswordRequest resetPasswordEvent);

    void logout(HttpServletResponse response);
}
