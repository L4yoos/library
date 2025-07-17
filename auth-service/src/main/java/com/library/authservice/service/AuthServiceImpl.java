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

    private final PasswordEncoder passwordEncoder;
    private final WebClient webClient;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Override
    public void registerUser(RegistrationRequest request) {
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
                    .uri(userServiceUrl + "/internal")
                    .bodyValue(userToCreate)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .onErrorResume(WebClientResponseException.class, e -> {
                        if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                            return Mono.error(new IllegalArgumentException(e.getMessage()));
                        } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                            return Mono.error(new RegistrationConflictException(e.getMessage()));
                        } else {
                            return Mono.error(new ServiceCommunicationException("User Service",
                                    "Received error from user service: " + e.getStatusCode() + " - " + e.getMessage()));
                        }
                    })
                    .block();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred during registration.", e);
        }
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(authentication);
        return new JwtResponse(jwt, userDetails.getFirstname(), userDetails.getLastname(), userDetails.getEmail());
    }
}