package com.example.auth_service.service;

import com.example.auth_service.AuthServiceApplication;
import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = AuthServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"user-login-topic"})
@ActiveProfiles("test")
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private OtpService otpService;

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String VERIFY_URL = "/api/auth/verify-otp";

    private String testEmail = "adamsmichael46@gmail.com";
    private String correctOtp;

    @BeforeEach
    void setup() {
        // Clean redis before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        // Generate a fresh OTP for test user
        correctOtp = otpService.generateOtp(testEmail);
    }

    @Test
    void testSuccessfulLogin_AndOtpVerification() throws Exception {
        // Step 1: simulate user login
        UserLoginRequest loginRequest = new UserLoginRequest(testEmail, "password123");

        ResultActions loginResult = mockMvc.perform(
                post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "adamsmichael46@gmail.com",
                              "password": "password123"
                            }
                        """)
        );

        loginResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("correct credentials, proceed to enter otp"));

        // Step 2: verify OTP
        ResultActions verifyResult = mockMvc.perform(
                post(VERIFY_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "adamsmichael46@gmail.com",
                              "otp": "%s"
                            }
                        """.formatted(correctOtp))
        );

        verifyResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP verified successfully"));
    }
}
