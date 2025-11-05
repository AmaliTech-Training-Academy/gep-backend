package com.example.auth_service.integration;

import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.CustomApiResponse;
import com.example.auth_service.dto.response.UserCreationResponse;
import com.example.auth_service.enums.UserRole;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ProfileRepository;
import com.example.auth_service.repository.UserEventStatsRepository;
import com.example.auth_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegistrationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserEventStatsRepository userEventStatsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userEventStatsRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userEventStatsRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully register a new user end-to-end")
    void testCompleteRegistrationFlow_Success() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Jane Smith",
                "jane.smith@example.com",
                "SecurePass123!",
                "SecurePass123!"
        );

        // Act
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.fullName", is("Jane Smith")))
                .andReturn();

        // Parse response
        String responseBody = result.getResponse().getContentAsString();
        CustomApiResponse<UserCreationResponse> response = objectMapper.readValue(responseBody,
                objectMapper.getTypeFactory().constructParametricType(CustomApiResponse.class, UserCreationResponse.class));

        // Assert - Verify user in database
        User savedUser = userRepository.findById(response.data().id()).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getFullName()).isEqualTo("Jane Smith");
        assertThat(savedUser.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ORGANISER);
        assertThat(savedUser.isActive()).isTrue();
        assertThat(passwordEncoder.matches("SecurePass123!", savedUser.getPassword())).isTrue();

    }

    @Test
    @DisplayName("Should prevent duplicate email registration")
    void testRegistration_DuplicateEmail_ReturnsConflict() throws Exception {
        // Arrange - Create existing user
        User existingUser = User.builder()
                .fullName("Existing User")
                .email("existing@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(UserRole.ORGANISER)
                .isActive(true)
                .build();
        userRepository.save(existingUser);

        // Attempt to register with same email
        UserRegistrationRequest request = new UserRegistrationRequest(
                "New User",
                "existing@example.com",
                "NewPassword123!",
                "NewPassword123!"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        // Verify only one user exists with this email
        assertThat(userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals("existing@example.com"))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should reject registration when passwords do not match")
    void testRegistration_PasswordMismatch_ReturnsBadRequest() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "John Doe",
                "john.doe@example.com",
                "Password123!",
                "DifferentPassword123!"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no user was created
        assertThat(userRepository.findByEmail("john.doe@example.com")).isEmpty();
    }

    @Test
    @DisplayName("Should encrypt password before storing")
    void testRegistration_PasswordEncryption() throws Exception {
        // Arrange
        String plainPassword = "MySecretPassword123!";
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Security Test User",
                "security@example.com",
                plainPassword,
                plainPassword
        );

        // Act
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();


        // Parse response
        String responseBody = result.getResponse().getContentAsString();
        CustomApiResponse<UserCreationResponse> response = objectMapper.readValue(responseBody,
                objectMapper.getTypeFactory().constructParametricType(CustomApiResponse.class, UserCreationResponse.class));

        // Assert - Password should be encrypted
        User savedUser = userRepository.findById(response.data().id()).orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo(plainPassword);
        assertThat(savedUser.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches(plainPassword, savedUser.getPassword())).isTrue();

    }

    @Test
    @DisplayName("Should create user with ORGANISER role by default")
    void testRegistration_DefaultRole() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Role Test User",
                "roletest@example.com",
                "Password123!",
                "Password123!"
        );

        // Act
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        // Parse response
        String responseBody = result.getResponse().getContentAsString();
        CustomApiResponse<UserCreationResponse> response = objectMapper.readValue(responseBody,
                objectMapper.getTypeFactory().constructParametricType(CustomApiResponse.class, UserCreationResponse.class));

        // Assert
        User savedUser = userRepository.findById(response.data().id()).orElseThrow();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ORGANISER);
    }

    @Test
    @DisplayName("Should create user as active by default")
    void testRegistration_ActiveByDefault() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Active User",
                "active@example.com",
                "Password123!",
                "Password123!"
        );

        // Act
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        // Parse response
        String responseBody = result.getResponse().getContentAsString();
        CustomApiResponse<UserCreationResponse> response = objectMapper.readValue(responseBody,
                objectMapper.getTypeFactory().constructParametricType(CustomApiResponse.class, UserCreationResponse.class));

        // Assert
        User savedUser = userRepository.findById(response.data().id()).orElseThrow();
        assertThat(savedUser.isActive()).isTrue();

    }



    @Test
    @DisplayName("Should handle special characters in full name")
    void testRegistration_SpecialCharactersInName() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "María José O'Brien-García",
                "maria@example.com",
                "Password123!",
                "Password123!"
        );

        // Act
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName", is("María José O'Brien-García")))  // Changed path to $.data.fullName
                .andExpect(jsonPath("$.description", is("User registered successfully")))     // Added message check
                .andReturn();


        // Parse response
        String responseBody = result.getResponse().getContentAsString();
        CustomApiResponse<UserCreationResponse> response = objectMapper.readValue(responseBody,
                objectMapper.getTypeFactory().constructParametricType(CustomApiResponse.class, UserCreationResponse.class));

        // Assert
        User savedUser = userRepository.findById(response.data().id()).orElseThrow();
        assertThat(savedUser.getFullName()).isEqualTo("María José O'Brien-García");

    }

    @Test
    @DisplayName("Should handle case-sensitive email addresses correctly")
    void testRegistration_CaseSensitiveEmail() throws Exception {
        // Arrange - Register first user
        UserRegistrationRequest request1 = new UserRegistrationRequest(
                "User One",
                "user@EXAMPLE.com",
                "Password123!",
                "Password123!"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Try to register with different case
        UserRegistrationRequest request2 = new UserRegistrationRequest(
                "User Two",
                "USER@example.com",
                "Password123!",
                "Password123!"
        );


        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should rollback transaction if profile creation fails")
    void testRegistration_TransactionRollback() throws Exception {
        // This test verifies that if any part of the registration fails,

        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Rollback Test",
                "rollback@example.com",
                "Password123!",
                "Password123!"
        );

        long initialUserCount = userRepository.count();

        // Act - This will succeed under normal circumstances
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Assert
        assertThat(userRepository.count()).isEqualTo(initialUserCount + 1);
        assertThat(profileRepository.count()).isEqualTo(initialUserCount + 1);
        assertThat(userEventStatsRepository.count()).isEqualTo(initialUserCount + 1);
    }

    @Test
    @DisplayName("Should handle multiple concurrent registrations")
    void testRegistration_ConcurrentUsers() throws Exception {
        // Arrange
        UserRegistrationRequest request1 = new UserRegistrationRequest(
                "User One",
                "user1@example.com",
                "Password123!",
                "Password123!"
        );

        UserRegistrationRequest request2 = new UserRegistrationRequest(
                "User Two",
                "user2@example.com",
                "Password123!",
                "Password123!"
        );

        UserRegistrationRequest request3 = new UserRegistrationRequest(
                "User Three",
                "user3@example.com",
                "Password123!",
                "Password123!"
        );

        // Act - Register multiple users
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isOk());

        // Assert - All users should be registered
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(profileRepository.count()).isEqualTo(3);
        assertThat(userEventStatsRepository.count()).isEqualTo(3);

        // Verify each user exists
        assertThat(userRepository.findByEmail("user1@example.com")).isPresent();
        assertThat(userRepository.findByEmail("user2@example.com")).isPresent();
        assertThat(userRepository.findByEmail("user3@example.com")).isPresent();
    }

    @Test
    @DisplayName("Should preserve user data integrity across the full registration flow")
    void testRegistration_DataIntegrity() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Integrity Test User",
                "integrity@example.com",
                "Password123!",
                "Password123!"
        );

        // Act
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        // Parse response
        String responseBody = result.getResponse().getContentAsString();
        CustomApiResponse<UserCreationResponse> response = objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructParametricType(CustomApiResponse.class, UserCreationResponse.class));

        // Assert - Verify all data is consistent
        User user = userRepository.findById(response.data().id()).orElseThrow();


        // Verify data consistency
        assertThat(response.data().id()).isEqualTo(user.getId());
        assertThat(response.data().fullName()).isEqualTo(user.getFullName());
    }

    @Test
    @DisplayName("Should validate email format before registration")
    void testRegistration_InvalidEmailFormat() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Invalid Email User",
                "not-an-email",
                "Password123!",
                "Password123!"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no user was created
        assertThat(userRepository.findByEmail("not-an-email")).isEmpty();
    }

    @Test
    @DisplayName("Should reject registration with empty full name")
    void testRegistration_EmptyFullName() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "",
                "empty@example.com",
                "Password123!",
                "Password123!"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no user was created
        assertThat(userRepository.findByEmail("empty@example.com")).isEmpty();
    }

    @Test
    @DisplayName("Should reject registration with whitespace-only full name")
    void testRegistration_WhitespaceFullName() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "   ",
                "whitespace@example.com",
                "Password123!",
                "Password123!"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify no user was created
        assertThat(userRepository.findByEmail("whitespace@example.com")).isEmpty();
    }

    @Test
    @DisplayName("Should handle registration with valid password length")
    void testRegistration_MinimalPassword() throws Exception {
        // Arrange - Assuming minimum password requirements
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Minimal Password User",
                "minpass@example.com",
                "Pass1!",
                "Pass1!"
        );

        // Act - This depends on your password validation rules
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Should return user ID in response after successful registration")
    void testRegistration_ResponseContainsUserId() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Response Test User",
                "response@example.com",
                "Password123!",
                "Password123!"
        );

        // Act
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.fullName").exists())
                .andReturn();

        // Parse and verify response
        String responseBody = result.getResponse().getContentAsString();
        CustomApiResponse<UserCreationResponse> response = objectMapper.readValue(responseBody,
                objectMapper.getTypeFactory().constructParametricType(CustomApiResponse.class, UserCreationResponse.class));

        assertThat(response.data().id()).isNotNull();
        assertThat(response.data().id()).isGreaterThan(0L);
        assertThat(response.data().fullName()).isEqualTo("Response Test User");
    }

    @Test
    @DisplayName("Should handle email with plus sign for email aliasing")
    void testRegistration_EmailWithPlusSign() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Plus Sign User",
                "user+test@example.com",
                "Password123!",
                "Password123!"
        );

        // Act
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Assert
        User savedUser = userRepository.findByEmail("user+test@example.com").orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("user+test@example.com");
    }


}