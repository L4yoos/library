package com.library.common.security.filter;

import com.library.common.exception.InvalidAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class InternalApiAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(InternalApiAuthFilter.class);

    private final String apiKeyHeaderName;
    private final String apiKey;

    public InternalApiAuthFilter(String apiKeyHeaderName, String apiKey) {
        this.apiKeyHeaderName = apiKeyHeaderName;
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            logger.debug("User is already authenticated in SecurityContext. Skipping internal API key check.");
            filterChain.doFilter(request, response);
            return;
        }

        String actualApiKey = request.getHeader(apiKeyHeaderName);

        if (actualApiKey == null) {
            logger.debug("No API key header '{}' found in request. Continuing filter chain.", apiKeyHeaderName);
            filterChain.doFilter(request, response);
            return;
        }

        if (!actualApiKey.equals(apiKey)) {
            logger.warn("Invalid API key provided for internal access from {}. Header: '{}'", request.getRemoteAddr(), apiKeyHeaderName);
            throw new InvalidAuthenticationException("Invalid API key provided for internal access.");
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "internal-service-user", null, AuthorityUtils.createAuthorityList("ROLE_INTERNAL_SERVICE"));
        SecurityContextHolder.getContext().setAuthentication(auth);
        logger.info("Internal service user authenticated successfully via API key.");

        filterChain.doFilter(request, response);
    }
}