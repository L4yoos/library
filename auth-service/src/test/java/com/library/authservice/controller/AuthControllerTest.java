package com.library.authservice.controller;

import com.library.authservice.config.SecurityConfig;
import com.library.authservice.dto.JwtResponse;
import com.library.authservice.dto.LoginRequest;
import com.library.authservice.dto.RegistrationRequest;
import com.library.authservice.exception.RegistrationConflictException;
import com.library.authservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient webClient;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegistrationRequest validRegistrationRequest;
    private LoginRequest validLoginRequest;
    private JwtResponse jwtResponse;

    @BeforeEach
    void setUp() {
        validRegistrationRequest = new RegistrationRequest();
        validRegistrationRequest.setEmail("test@example.com");
        validRegistrationRequest.setPassword("Password123!");
        validRegistrationRequest.setFirstName("John");
        validRegistrationRequest.setLastName("Doe");
        validRegistrationRequest.setPhoneNumber("999999999");
        validRegistrationRequest.setAddress("Zduńska Wola, ul. Zielona");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("Password123!");

        jwtResponse = new JwtResponse("mockedToken", "test@example.com", "user", null);
    }

    @Test
    @DisplayName("registerUser should return 201 Created on successful registration")
    void registerUser_shouldReturn201Created_onSuccess() throws Exception {
        doNothing().when(authService).registerUser(any(RegistrationRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));

        verify(authService, times(1)).registerUser(any(RegistrationRequest.class));
    }

    @Test
    @DisplayName("registerUser should return 400 Bad Request on validation error")
    void registerUser_shouldReturn400BadRequest_onValidationError() throws Exception {
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("short");

        mockMvc.perform(post("/api/auth/register")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));

        verify(authService, never()).registerUser(any(RegistrationRequest.class));
    }

    @Test
    @DisplayName("registerUser should return 409 Conflict if user already exists")
    void registerUser_shouldReturn409Conflict_onUserAlreadyExists() throws Exception {
        doThrow(new RegistrationConflictException("User with this email already exists."))
                .when(authService).registerUser(any(RegistrationRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User with this email already exists."))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));

        verify(authService, times(1)).registerUser(any(RegistrationRequest.class));
    }

    @Test
    @DisplayName("registerUser should return 500 Internal Server Error on unexpected service error")
    void registerUser_shouldReturn500InternalServerError_onServiceError() throws Exception {
        doThrow(new RuntimeException("Something went wrong."))
                .when(authService).registerUser(any(RegistrationRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));

        verify(authService, times(1)).registerUser(any(RegistrationRequest.class));
    }


    @Test
    @DisplayName("authenticateUser should return 200 OK and set cookie on successful login")
    void authenticateUser_shouldReturn200Ok_onSuccess() throws Exception {
        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockedToken"))
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().httpOnly("token", true))
                .andExpect(cookie().secure("token", true))
                .andExpect(cookie().maxAge("token", 3600000))
                .andExpect(cookie().path("token", "/"));

        verify(authService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("authenticateUser should return 400 Bad Request on validation error")
    void authenticateUser_shouldReturn400BadRequest_onValidationError() throws Exception {
        LoginRequest invalidLoginRequest = new LoginRequest();
        invalidLoginRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/login")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));

        verify(authService, never()).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("authenticateUser should return 401 Unauthorized on bad credentials")
    void authenticateUser_shouldReturn401Unauthorized_onBadCredentials() throws Exception {
        doThrow(new BadCredentialsException("Bad credentials."))
                .when(authService).authenticateUser(any(LoginRequest.class));

        mockMvc.perform(post("/api/auth/login")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Bad credentials."))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));

        verify(authService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("authenticateUser should return 500 Internal Server Error on unexpected service error")
    void authenticateUser_shouldReturn500InternalServerError_onServiceError() throws Exception {
        doThrow(new RuntimeException("Database connection failed."))
                .when(authService).authenticateUser(any(LoginRequest.class));

        mockMvc.perform(post("/api/auth/login")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));

        verify(authService, times(1)).authenticateUser(any(LoginRequest.class));
    }
}