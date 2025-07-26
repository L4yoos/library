package com.library.common.security;

import com.library.common.dto.UserAuthDTO;
import com.library.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final WebClient webClient;
    private final static String userServiceInternalAuthUrl = "http://USER-SERVICE/api/users/";

    @Value("${internal.api-key.header-name}")
    private String apiKeyHeaderName;

    @Value("${internal.api-key.value}")
    private String apiKeyValue;

    @Override
    public UserDetails loadUserByUsername(String email) throws UserNotFoundException {
        try {
            logger.info("Attempting to fetch user authentication data for email: {}", email);
            UserAuthDTO userAuthDTO = webClient.get()
                    .uri(userServiceInternalAuthUrl + "internal/auth-data/" + email)
                    .header(apiKeyHeaderName, apiKeyValue)
                    .retrieve()
                    .bodyToMono(UserAuthDTO.class)
                    .block();

            if (userAuthDTO == null) {
                logger.warn("User authentication data not found (null response) for email: {}", email);
                throw new UserNotFoundException("User auth data not found for email: " + email);
            }
            logger.info("Successfully fetched user authentication data for email: {}", email);
            return CustomUserDetails.build(userAuthDTO);
        } catch (WebClientResponseException.NotFound ex) {
            logger.warn("User not found by email: {} - Details: {}", email, ex.getMessage());
            throw new UserNotFoundException("User auth data not found for email: " + email);
        } catch (Exception ex) {
            logger.error("Error communicating with user-service to fetch user auth data for email: {}", email, ex);
            throw new RuntimeException("Error communicating with user-service to fetch user auth data: " + email, ex);
        }
    }
}