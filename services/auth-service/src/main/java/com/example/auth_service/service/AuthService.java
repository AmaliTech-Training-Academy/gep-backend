package com.example.auth_service.service;

import com.example.auth_service.dto.request.*;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.UserCreationResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    @Transactional
    UserCreationResponse registerNewUser(UserRegistrationRequest registrationRequest);

    UserCreationResponse registerInvitee(InviteeAccountCreationRequest inviteeRequest);

    void loginUser(UserLoginRequest loginRequest);

    AuthResponse adminLogin(UserLoginRequest loginRequest, HttpServletResponse response);

    AuthResponse verifyOtp(OtpVerificationRequest request, HttpServletResponse response);

    void resendOtp(String email);

    void refreshAccessToken(String refreshToken, HttpServletResponse response);

    void requestPasswordReset(String email);

    void resetPassword(ResetPasswordRequest resetPasswordEvent);

    void logout(HttpServletResponse response);
}
