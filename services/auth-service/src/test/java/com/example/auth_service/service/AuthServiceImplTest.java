package com.example.auth_service.service;

import com.example.auth_service.dto.request.OtpVerificationRequest;
import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.exception.InactiveAccountException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.repository.ProfileRepository;
import com.example.auth_service.repository.UserEventStatsRepository;
import com.example.auth_service.security.AuthUser;
import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.service.impl.AuthServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserEventStatsRepository userEventStatsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- LOGIN TESTS ---
    @Test
    void testLoginUser_Successful() {
        String email = "user@example.com";
        String password = "password";

        User user = User.builder()
                .email(email)
                .password("encoded")
                .fullName("Test User")
                .role(UserRole.ATTENDEE)
                .isActive(true)
                .build();

        AuthUser authUser = new AuthUser(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(authUser);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        authService.loginUser(new UserLoginRequest(email, password));

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(otpService, times(1)).generateOtp(email);
    }

    @Test
    void testLoginUser_InactiveAccount_ThrowsException() {
        String email = "inactive@example.com";
        String password = "password";

        User user = User.builder()
                .email(email)
                .password("encoded")
                .isActive(false)
                .build();

        AuthUser authUser = new AuthUser(user);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(authUser);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        InactiveAccountException ex = assertThrows(
                InactiveAccountException.class,
                () -> authService.loginUser(new UserLoginRequest(email, password))
        );

        assertEquals("User account is inactive", ex.getMessage());
        verify(otpService, never()).generateOtp(anyString());
    }

    @Test
    void testLoginUser_InvalidCredentials_ThrowsException() {
        String email = "wrong@example.com";
        String password = "wrongpass";

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.loginUser(new UserLoginRequest(email, password))
        );

        assertEquals("Invalid credentials", ex.getMessage());
        verify(otpService, never()).generateOtp(anyString());
    }

    // --- VERIFY OTP TESTS ---
    @Test
    void testVerifyOtp_Successful() {
        String email = "user@example.com";
        String otp = "123456";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        User user = User.builder()
                .email(email)
                .isActive(true)
                .build();

        when(otpService.verifyOtp(email, otp)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(email)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(email)).thenReturn(refreshToken);

        authService.verifyOtp(new OtpVerificationRequest(email, otp), response);

        verify(otpService, times(1)).verifyOtp(email, otp);
        verify(userRepository, times(1)).findByEmail(email);
        verify(jwtUtil, times(1)).generateAccessToken(email);
        verify(jwtUtil, times(1)).generateRefreshToken(email);
        verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void testVerifyOtp_InvalidOtp_ThrowsException() {
        String email = "user@example.com";
        String otp = "000000";

        when(otpService.verifyOtp(email, otp)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.verifyOtp(new OtpVerificationRequest(email, otp), response)
        );

        assertEquals("Invalid or expired OTP", ex.getMessage());
        verify(jwtUtil, never()).generateAccessToken(anyString());
        verify(response, never()).addHeader(anyString(), anyString());
    }

    @Test
    void testVerifyOtp_UserNotFound_ThrowsException() {
        String email = "notfound@example.com";
        String otp = "123456";

        when(otpService.verifyOtp(email, otp)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.verifyOtp(new OtpVerificationRequest(email, otp), response)
        );

        assertEquals("User not found", ex.getMessage());
        verify(jwtUtil, never()).generateAccessToken(anyString());
        verify(response, never()).addHeader(anyString(), anyString());
    }

    @Test
    void testVerifyOtp_InactiveUser_ThrowsException() {
        String email = "inactive@example.com";
        String otp = "123456";

        User user = User.builder().email(email).isActive(false).build();

        when(otpService.verifyOtp(email, otp)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        InactiveAccountException ex = assertThrows(
                InactiveAccountException.class,
                () -> authService.verifyOtp(new OtpVerificationRequest(email, otp), response)
        );

        assertEquals("User account is inactive", ex.getMessage());
        verify(jwtUtil, never()).generateAccessToken(anyString());
        verify(response, never()).addHeader(anyString(), anyString());
    }

    // --- REFRESH TOKEN TESTS ---
    @Test
    void testRefreshAccessToken_Successful() {
        String email = "user@example.com";
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        User user = User.builder()
                .email(email)
                .isActive(true)
                .build();

        when(jwtUtil.extractUsername(oldRefreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken(oldRefreshToken)).thenReturn(true);
        when(jwtUtil.generateAccessToken(email)).thenReturn(newAccessToken);
        when(jwtUtil.generateRefreshToken(email)).thenReturn(newRefreshToken);

        authService.refreshAccessToken(oldRefreshToken, response);

        verify(jwtUtil, times(1)).extractUsername(oldRefreshToken);
        verify(jwtUtil, times(1)).validateToken(oldRefreshToken);
        verify(jwtUtil, times(1)).generateAccessToken(email);
        verify(jwtUtil, times(1)).generateRefreshToken(email);
        verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void testRefreshAccessToken_UserNotFound_ThrowsException() {
        String email = "notfound@example.com";
        String refreshToken = "refresh-token";

        when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.refreshAccessToken(refreshToken, response)
        );

        assertEquals("User not found", ex.getMessage());
        verify(response, never()).addHeader(anyString(), anyString());
    }

    @Test
    void testRefreshAccessToken_InactiveUser_ThrowsException() {
        String email = "inactive@example.com";
        String refreshToken = "refresh-token";

        User user = User.builder()
                .email(email)
                .isActive(false)
                .build();

        when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        InactiveAccountException ex = assertThrows(
                InactiveAccountException.class,
                () -> authService.refreshAccessToken(refreshToken, response)
        );

        assertEquals("User account is inactive", ex.getMessage());
        verify(response, never()).addHeader(anyString(), anyString());
    }

    @Test
    void testRefreshAccessToken_InvalidToken_ThrowsException() {
        String email = "user@example.com";
        String refreshToken = "invalid-token";

        User user = User.builder()
                .email(email)
                .isActive(true)
                .build();

        when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.refreshAccessToken(refreshToken, response)
        );

        assertEquals("Invalid refresh token", ex.getMessage());
        verify(jwtUtil, never()).generateAccessToken(anyString());
        verify(response, never()).addHeader(anyString(), anyString());
    }

    // --- LOGOUT TEST ---
    @Test
    void testLogout_Successful() {
        authService.logout(response);

        // Verify that cookies are cleared (addHeader called twice for both cookies)
        verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }
}