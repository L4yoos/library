package com.library.authservice.service;

import com.library.authservice.dto.CreateUserRequest;
import com.library.authservice.dto.JwtResponse;
import com.library.authservice.dto.LoginRequest;
import com.library.authservice.dto.RegistrationRequest;
import com.library.authservice.exception.RegistrationConflictException;
import com.library.common.dto.UserDTO;
import com.library.common.exception.ServiceCommunicationException;
import com.library.common.security.CustomUserDetails;
import com.library.common.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WebClient webClient;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field userServiceUrlField = AuthServiceImpl.class.getDeclaredField("userServiceUrl");
        userServiceUrlField.setAccessible(true);
        userServiceUrlField.set(authService, "http://test-user-service");

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void registerUser_Success() {
        RegistrationRequest request = new RegistrationRequest(
                "jan.kowalski@example.com", "password123", "Jan", "Kowalski", "123456789", "ul. Testowa 1"
        );
        UserDTO userDTO = new UserDTO(
                UUID.randomUUID(), "Jan", "Kowalski", "jan.kowalski@example.com", "123456789", "ul. Testowa 1",
                LocalDate.now(), true, new HashSet<>()
        );

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(CreateUserRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserDTO.class)).thenReturn(Mono.just(userDTO));

        assertDoesNotThrow(() -> authService.registerUser(request));

        verify(passwordEncoder).encode("password123");
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("http://test-user-service");
        verify(requestBodySpec).bodyValue(any(CreateUserRequest.class));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(UserDTO.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for BAD_REQUEST from User Service during registration")
    void registerUser_BadRequestFromUserService() {
        RegistrationRequest request = new RegistrationRequest(
                "jan.kowalski@example.com", "password123", "Jan", "Kowalski", "123456789", "ul. Testowa 1"
        );
        String errorMessage = "Invalid input data";

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(CreateUserRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserDTO.class))
                .thenReturn(Mono.error(WebClientResponseException.create(HttpStatus.BAD_REQUEST.value(), errorMessage, null, null, null)));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> authService.registerUser(request));
        assertEquals("400 " + errorMessage, thrown.getMessage());

        verify(passwordEncoder).encode("password123");
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(UserDTO.class);
    }

    @Test
    @DisplayName("Should throw RegistrationConflictException for CONFLICT from User Service during registration")
    void registerUser_ConflictFromUserService() {
        RegistrationRequest request = new RegistrationRequest(
                "jan.kowalski@example.com", "password123", "Jan", "Kowalski", "123456789", "ul. Testowa 1"
        );
        String errorMessage = "User with this email already exists";

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(CreateUserRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserDTO.class))
                .thenReturn(Mono.error(WebClientResponseException.create(HttpStatus.CONFLICT.value(), errorMessage, null, null, null)));

        RegistrationConflictException thrown = assertThrows(RegistrationConflictException.class, () -> authService.registerUser(request));
        assertEquals("409 " + errorMessage, thrown.getMessage());

        verify(passwordEncoder).encode("password123");
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(UserDTO.class);
    }

    @Test
    @DisplayName("Should throw ServiceCommunicationException for other errors from User Service during registration")
    void registerUser_OtherErrorFromUserService() {
        RegistrationRequest request = new RegistrationRequest(
                "jan.kowalski@example.com", "password123", "Jan", "Kowalski", "123456789", "ul. Testowa 1"
        );
        String errorMessage = "Internal server error";

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(CreateUserRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserDTO.class))
                .thenReturn(Mono.error(WebClientResponseException.create(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage, null, null, null)));

        ServiceCommunicationException thrown = assertThrows(ServiceCommunicationException.class, () -> authService.registerUser(request));
        assertTrue(thrown.getMessage().contains("User Service"));
        assertTrue(thrown.getMessage().contains("INTERNAL_SERVER_ERROR"));
        assertTrue(thrown.getMessage().contains(errorMessage));

        verify(passwordEncoder).encode("password123");
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(UserDTO.class);
    }

    @Test
    @DisplayName("Should rethrow RuntimeException during registration")
    void registerUser_RuntimeException() {
        RegistrationRequest request = new RegistrationRequest(
                "jan.kowalski@example.com", "password123", "Jan", "Kowalski", "123456789", "ul. Testowa 1"
        );
        String expectedMessage = "Simulated network error";

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(CreateUserRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserDTO.class))
                .thenReturn(Mono.error(new RuntimeException(expectedMessage)));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> authService.registerUser(request));
        assertEquals(expectedMessage, thrown.getMessage());

        verify(passwordEncoder).encode("password123");
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(UserDTO.class);
    }

    @Test
    @DisplayName("Should successfully authenticate a user and generate JWT")
    void authenticateUser_Success() {
        LoginRequest request = new LoginRequest("jan.kowalski@example.com", "password123");

        Set<String> userRoles = new HashSet<>(Arrays.asList("ROLE_USER"));
        CustomUserDetails userDetails = new CustomUserDetails(
                UUID.randomUUID(), "Jan", "Kowalski", "jan.kowalski@example.com", "password123",
                userRoles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList())
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("mockedJwtToken");

        JwtResponse response = authService.authenticateUser(request);

        assertNotNull(response);
        assertEquals("mockedJwtToken", response.getToken());
        assertEquals("Jan", response.getFirstname());
        assertEquals("Kowalski", response.getLastname());
        assertEquals("jan.kowalski@example.com", response.getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for invalid login credentials")
    void authenticateUser_InvalidCredentials() {
        LoginRequest request = new LoginRequest("jan.kowalski@example.com", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        BadCredentialsException thrown = assertThrows(BadCredentialsException.class, () -> authService.authenticateUser(request));
        assertEquals("Invalid username or password", thrown.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtTokenProvider);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}