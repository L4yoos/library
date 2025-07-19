package com.library.common.security.filter;

import com.library.common.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (jwt == null) {
                logger.debug("No JWT token found in request. Continuing filter chain.");
                filterChain.doFilter(request, response);
                return;
            }

            logger.debug("Attempting to validate JWT token.");
            if (!jwtTokenProvider.validateToken(jwt)) {
                logger.warn("Invalid or expired JWT token provided.");
                throw new BadCredentialsException("Invalid or expired JWT token provided.");
            }

            String username = jwtTokenProvider.getUserEmailFromJwtToken(jwt);
            logger.debug("JWT token is valid. Extracting username: {}", username);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            logger.debug("User details loaded for username: {}", username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("User '{}' authenticated successfully with JWT.", username);

        } catch (AuthenticationException e) {
            logger.error("Authentication error during token processing: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during token processing: {}", e.getMessage(), e);
            throw new ServletException("An unexpected error occurred during token processing.", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            logger.debug("Found JWT in Authorization header.");
            return headerAuth.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    logger.debug("Found JWT in 'token' cookie.");
                    return cookie.getValue();
                }
            }
        }
        logger.debug("No JWT found in Authorization header or 'token' cookie.");
        return null;
    }
}