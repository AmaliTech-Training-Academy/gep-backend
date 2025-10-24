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
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    private OtpService otpService;

    @Mock
    private AuthenticationManager authenticationManager;

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
                .role(UserRole.ORGANISER)
                .isActive(true)
                .build();

        AuthUser authUser = new AuthUser(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(authUser);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        authService.loginUser(new UserLoginRequest(email, password));

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(otpService, times(1)).requestLoginOtp(email);
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
        verify(otpService, never()).requestLoginOtp(anyString());
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
        verify(otpService, never()).requestLoginOtp(anyString());
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

    // ========== DEACTIVATED USER COMPREHENSIVE TESTS ==========
    @Nested
    @DisplayName("Deactivated User - Login Phase Tests")
    class DeactivatedUserLoginTests {

        @Test
        @DisplayName("Should prevent deactivated user from logging in with valid credentials")
        void testDeactivatedUser_CannotLogin_ValidCredentials() {
            // Arrange
            String email = "deactivated@example.com";
            String password = "ValidPassword123!";

            User deactivatedUser = User.builder()
                    .email(email)
                    .password("encoded_password")
                    .fullName("Deactivated User")
                    .role(UserRole.ORGANISER)
                    .isActive(false)
                    .build();

            AuthUser authUser = new AuthUser(deactivatedUser);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(authUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);

            UserLoginRequest loginRequest = new UserLoginRequest(email, password);

            // Act & Assert
            InactiveAccountException exception = assertThrows(
                    InactiveAccountException.class,
                    () -> authService.loginUser(loginRequest)
            );

            assertEquals("User account is inactive", exception.getMessage());
            verify(authenticationManager, times(1)).authenticate(any());
            verify(otpService, never()).requestLoginOtp(anyString());
        }

        @Test
        @DisplayName("Should not generate OTP for deactivated user")
        void testDeactivatedUser_NoOtpGeneration() {
            // Arrange
            String email = "deactivated@example.com";
            User deactivatedUser = User.builder()
                    .email(email)
                    .isActive(false)
                    .build();

            AuthUser authUser = new AuthUser(deactivatedUser);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(authUser);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);

            // Act & Assert
            assertThrows(InactiveAccountException.class,
                    () -> authService.loginUser(new UserLoginRequest(email, "password")));

            verify(otpService, never()).requestLoginOtp(email);
        }

        @Test
        @DisplayName("Should block deactivated organizer from logging in")
        void testDeactivatedOrganizer_CannotLogin() {
            // Arrange
            String email = "organizer@example.com";
            User deactivatedOrganizer = User.builder()
                    .email(email)
                    .password("encoded")
                    .role(UserRole.ORGANISER)
                    .isActive(false)
                    .build();

            AuthUser authUser = new AuthUser(deactivatedOrganizer);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(authUser);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);

            // Act & Assert
            assertThrows(InactiveAccountException.class,
                    () -> authService.loginUser(new UserLoginRequest(email, "password")));

            verify(otpService, never()).requestLoginOtp(anyString());
        }
    }

    @Nested
    @DisplayName("Deactivated User - OTP Verification Phase Tests")
    class DeactivatedUserOtpVerificationTests {

        @Test
        @DisplayName("Should reject OTP verification for deactivated user")
        void testDeactivatedUser_CannotVerifyOtp() {
            // Arrange
            String email = "deactivated@example.com";
            String otp = "123456";

            User deactivatedUser = User.builder()
                    .email(email)
                    .fullName("Deactivated User")
                    .role(UserRole.ORGANISER)
                    .isActive(false)
                    .build();

            when(otpService.verifyOtp(email, otp)).thenReturn(true);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));

            OtpVerificationRequest request = new OtpVerificationRequest(email, otp);

            // Act & Assert
            InactiveAccountException exception = assertThrows(
                    InactiveAccountException.class,
                    () -> authService.verifyOtp(request, response)
            );

            assertEquals("User account is inactive", exception.getMessage());
            verify(otpService, times(1)).verifyOtp(email, otp);
            verify(jwtUtil, never()).generateAccessToken(anyString());
            verify(jwtUtil, never()).generateRefreshToken(anyString());
            verify(response, never()).addHeader(anyString(), anyString());
        }

        @Test
        @DisplayName("Should not issue tokens to deactivated user even with valid OTP")
        void testDeactivatedUser_NoTokensIssued() {
            // Arrange
            String email = "deactivated@example.com";
            String validOtp = "999999";

            User deactivatedUser = User.builder()
                    .email(email)
                    .isActive(false)
                    .build();

            when(otpService.verifyOtp(email, validOtp)).thenReturn(true);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));

            // Act & Assert
            assertThrows(InactiveAccountException.class,
                    () -> authService.verifyOtp(new OtpVerificationRequest(email, validOtp), response));

            verify(jwtUtil, never()).generateAccessToken(email);
            verify(jwtUtil, never()).generateRefreshToken(email);
        }

        @Test
        @DisplayName("Should not set authentication cookies for deactivated user")
        void testDeactivatedUser_NoCookiesSet() {
            // Arrange
            String email = "deactivated@example.com";
            String otp = "123456";

            User deactivatedUser = User.builder()
                    .email(email)
                    .isActive(false)
                    .build();

            when(otpService.verifyOtp(email, otp)).thenReturn(true);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));

            // Act & Assert
            assertThrows(InactiveAccountException.class,
                    () -> authService.verifyOtp(new OtpVerificationRequest(email, otp), response));

            verify(response, never()).addHeader(eq("Set-Cookie"), anyString());
        }
    }

    @Nested
    @DisplayName("Deactivated User - Token Refresh Phase Tests")
    class DeactivatedUserTokenRefreshTests {

        @Test
        @DisplayName("Should prevent deactivated user from refreshing token")
        void testDeactivatedUser_CannotRefreshToken() {
            // Arrange
            String email = "deactivated@example.com";
            String refreshToken = "valid-refresh-token";

            User deactivatedUser = User.builder()
                    .email(email)
                    .fullName("Deactivated User")
                    .role(UserRole.ORGANISER)
                    .isActive(false)
                    .build();

            when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));

            // Act & Assert
            InactiveAccountException exception = assertThrows(
                    InactiveAccountException.class,
                    () -> authService.refreshAccessToken(refreshToken, response)
            );

            assertEquals("User account is inactive", exception.getMessage());
            verify(jwtUtil, times(1)).extractUsername(refreshToken);
            verify(jwtUtil, never()).validateToken(anyString());
            verify(jwtUtil, never()).generateAccessToken(anyString());
            verify(jwtUtil, never()).generateRefreshToken(anyString());
            verify(response, never()).addHeader(anyString(), anyString());
        }

        @Test
        @DisplayName("Should check user status before validating refresh token")
        void testDeactivatedUser_UserStatusCheckedBeforeTokenValidation() {
            // Arrange
            String email = "deactivated@example.com";
            String refreshToken = "some-token";

            User deactivatedUser = User.builder()
                    .email(email)
                    .isActive(false)
                    .build();

            when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));

            // Act & Assert
            assertThrows(InactiveAccountException.class,
                    () -> authService.refreshAccessToken(refreshToken, response));

            // Verify that user status is checked before token validation
            verify(userRepository, times(1)).findByEmail(email);
            verify(jwtUtil, never()).validateToken(refreshToken);
        }

        @Test
        @DisplayName("Should not generate new tokens for deactivated user")
        void testDeactivatedUser_NoNewTokensGenerated() {
            // Arrange
            String email = "deactivated@example.com";
            String refreshToken = "refresh-token";

            User deactivatedUser = User.builder()
                    .email(email)
                    .isActive(false)
                    .build();

            when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));

            // Act & Assert
            assertThrows(InactiveAccountException.class,
                    () -> authService.refreshAccessToken(refreshToken, response));

            verify(jwtUtil, never()).generateAccessToken(anyString());
            verify(jwtUtil, never()).generateRefreshToken(anyString());
        }
    }

    @Nested
    @DisplayName("User Deactivation Timing Scenarios")
    class UserDeactivationTimingTests {

        @Test
        @DisplayName("Should reject user deactivated between login and OTP verification")
        void testUserDeactivated_BetweenLoginAndOtpVerification() {
            // Arrange
            String email = "user@example.com";
            String otp = "123456";

            // User was active during login
            User activeUser = User.builder()
                    .email(email)
                    .password("encoded")
                    .isActive(true)
                    .build();

            // But deactivated before OTP verification
            User deactivatedUser = User.builder()
                    .email(email)
                    .password("encoded")
                    .isActive(false)
                    .build();

            when(otpService.verifyOtp(email, otp)).thenReturn(true);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));

            // Act & Assert
            InactiveAccountException exception = assertThrows(
                    InactiveAccountException.class,
                    () -> authService.verifyOtp(new OtpVerificationRequest(email, otp), response)
            );

            assertEquals("User account is inactive", exception.getMessage());
            verify(response, never()).addHeader(anyString(), anyString());
        }

        @Test
        @DisplayName("Should reject token refresh when user deactivated after token issuance")
        void testUserDeactivated_AfterTokenIssuance() {
            // Arrange
            String email = "user@example.com";
            String refreshToken = "valid-token-from-when-active";

            // User now deactivated (was active when token was issued)
            User deactivatedUser = User.builder()
                    .email(email)
                    .isActive(false)
                    .build();

            when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));

            // Act & Assert
            InactiveAccountException exception = assertThrows(
                    InactiveAccountException.class,
                    () -> authService.refreshAccessToken(refreshToken, response)
            );

            assertEquals("User account is inactive", exception.getMessage());
            verify(jwtUtil, never()).validateToken(anyString());
            verify(response, never()).addHeader(anyString(), anyString());
        }

        @Test
        @DisplayName("Should perform real-time status check on every authentication operation")
        void testRealTimeStatusCheck_OnEveryOperation() {
            // Arrange
            String email = "user@example.com";
            User deactivatedUser = User.builder()
                    .email(email)
                    .isActive(false)
                    .build();

            // Test all three authentication flows check user status

            // 1. Login flow
            AuthUser authUser = new AuthUser(deactivatedUser);
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(authUser);
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            assertThrows(InactiveAccountException.class,
                    () -> authService.loginUser(new UserLoginRequest(email, "password")));

            // 2. OTP verification flow
            when(otpService.verifyOtp(email, "123456")).thenReturn(true);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));

            assertThrows(InactiveAccountException.class,
                    () -> authService.verifyOtp(new OtpVerificationRequest(email, "123456"), response));

            // 3. Token refresh flow
            when(jwtUtil.extractUsername("token")).thenReturn(email);

            assertThrows(InactiveAccountException.class,
                    () -> authService.refreshAccessToken("token", response));

            // Verify no tokens or cookies were ever issued
            verify(response, never()).addHeader(anyString(), anyString());
            verify(jwtUtil, never()).generateAccessToken(anyString());
            verify(jwtUtil, never()).generateRefreshToken(anyString());
        }
    }

    @Nested
    @DisplayName("Security and Consistency Tests")
    class SecurityConsistencyTests {

        @Test
        @DisplayName("Should throw same exception type for all deactivated user scenarios")
        void testConsistentExceptionType_ForDeactivatedUsers() {
            // Arrange
            String email = "deactivated@example.com";
            User deactivatedUser = User.builder()
                    .email(email)
                    .isActive(false)
                    .build();

            // Test 1: Login
            AuthUser authUser = new AuthUser(deactivatedUser);
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(authUser);
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            Exception loginException = assertThrows(InactiveAccountException.class,
                    () -> authService.loginUser(new UserLoginRequest(email, "password")));

            // Test 2: OTP Verification
            when(otpService.verifyOtp(email, "123456")).thenReturn(true);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));

            Exception otpException = assertThrows(InactiveAccountException.class,
                    () -> authService.verifyOtp(new OtpVerificationRequest(email, "123456"), response));

            // Test 3: Token Refresh
            when(jwtUtil.extractUsername("token")).thenReturn(email);

            Exception refreshException = assertThrows(InactiveAccountException.class,
                    () -> authService.refreshAccessToken("token", response));

            // Assert all throw same exception with same message
            assertEquals("User account is inactive", loginException.getMessage());
            assertEquals("User account is inactive", otpException.getMessage());
            assertEquals("User account is inactive", refreshException.getMessage());
        }

        @Test
        @DisplayName("Should prevent any form of authentication for deactivated users")
        void testCompleteAuthenticationBlock_ForDeactivatedUsers() {
            // Arrange
            String email = "deactivated@example.com";
            User deactivatedUser = User.builder()
                    .id(100L)
                    .email(email)
                    .password("encoded_password")
                    .fullName("Deactivated User")
                    .role(UserRole.ORGANISER)
                    .isActive(false)
                    .build();

            // Setup mocks
            AuthUser authUser = new AuthUser(deactivatedUser);
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(authUser);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(otpService.verifyOtp(anyString(), anyString())).thenReturn(true);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(deactivatedUser));
            when(jwtUtil.extractUsername(anyString())).thenReturn(email);

            // Act & Assert - All authentication attempts should fail
            assertThrows(InactiveAccountException.class,
                    () -> authService.loginUser(new UserLoginRequest(email, "password")));

            assertThrows(InactiveAccountException.class,
                    () -> authService.verifyOtp(new OtpVerificationRequest(email, "123456"), response));

            assertThrows(InactiveAccountException.class,
                    () -> authService.refreshAccessToken("any-token", response));

            // Verify no authentication artifacts were created
            verify(otpService, never()).requestLoginOtp(email);
            verify(jwtUtil, never()).generateAccessToken(email);
            verify(jwtUtil, never()).generateRefreshToken(email);
            verify(response, never()).addHeader(eq("Set-Cookie"), anyString());
        }
    }
}