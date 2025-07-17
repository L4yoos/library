package com.library.common.security;

import com.library.common.dto.UserAuthDTO;
import com.library.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final WebClient webClient;

    @Value("${user-service.url}")
    private String userServiceInternalAuthUrl;

    @Override
    public UserDetails loadUserByUsername(String email) throws UserNotFoundException {
        try {
            UserAuthDTO userAuthDTO = webClient.get()
                    .uri(userServiceInternalAuthUrl + "/internal/auth-data/" + email)
                    .retrieve()
                    .bodyToMono(UserAuthDTO.class) // Oczekujemy UserAuthDTO
                    .block();

            if (userAuthDTO == null) {
                throw new UserNotFoundException("User auth data not found for email: " + email);
            }

            return CustomUserDetails.build(userAuthDTO);
        } catch (WebClientResponseException.NotFound ex) {
            throw new UserNotFoundException("User auth data not found for email: " + email);
        } catch (Exception ex) {
            throw new RuntimeException("Error communicating with user-service to fetch user auth data: " + email, ex);
        }
    }
}