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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final PasswordEncoder passwordEncoder;
    private final WebClient webClient;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Override
    public void registerUser(RegistrationRequest request) {
        logger.info("Attempting to register new user with email: {}", request.getEmail());
        CreateUserRequest userToCreate = new CreateUserRequest(
                request.getFirstName(),
                request.getLastName(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getAddress()
        );

        try {
            webClient.post()
                    .uri(userServiceUrl)
                    .bodyValue(userToCreate)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .onErrorResume(WebClientResponseException.class, e -> {
                        if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                            logger.warn("Bad request when registering user {}: {}", request.getEmail(), e.getMessage());
                            return Mono.error(new IllegalArgumentException(e.getMessage()));
                        } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                            logger.warn("Registration conflict for user {}: {}", request.getEmail(), e.getMessage());
                            return Mono.error(new RegistrationConflictException(e.getMessage()));
                        } else {
                            logger.error("Error communicating with User Service during registration for user {}: Status {} - {}",
                                    request.getEmail(), e.getStatusCode(), e.getMessage());
                            return Mono.error(new ServiceCommunicationException("User Service",
                                    "Received error from user service: " + e.getStatusCode() + " - " + e.getMessage()));
                        }
                    })
                    .block();
            logger.info("User with email {} registered successfully.", request.getEmail());
        } catch (RuntimeException e) {
            logger.error("Runtime exception during user registration for {}: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during user registration for {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred during registration.", e);
        }
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest request) {
        logger.info("Attempting to authenticate user with email: {}", request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(authentication);
        logger.info("User with email {} authenticated successfully. JWT token generated.", request.getEmail());
        return new JwtResponse(jwt, userDetails.getFirstname(), userDetails.getLastname(), userDetails.getEmail());
    }
}