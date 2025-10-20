package com.example.auth_service.service;

import com.example.auth_service.dto.request.OtpVerificationRequest;
import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.exception.InactiveAccountException;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.AuthUser;
import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private OtpService otpService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;

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

    @Test
    void testVerifyOtp_InvalidOtp_ThrowsException() {
        String email = "user@example.com";
        String otp = "000000";
        when(otpService.verifyOtp(email, otp)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.verifyOtp(new OtpVerificationRequest(email, otp))
        );

        assertEquals("Invalid or expired OTP", ex.getMessage());
        verify(jwtUtil, never()).generateAccessToken(anyString());
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
                () -> authService.verifyOtp(new OtpVerificationRequest(email, otp))
        );

        assertEquals("User account is inactive", ex.getMessage());
        verify(jwtUtil, never()).generateAccessToken(anyString());
    }
}
