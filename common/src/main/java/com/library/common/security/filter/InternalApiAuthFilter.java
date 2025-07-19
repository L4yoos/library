package com.library.common.security.filter;

import com.library.common.exception.InvalidAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class InternalApiAuthFilter extends OncePerRequestFilter {

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
            filterChain.doFilter(request, response);
            return;
        }

        String actualApiKey = request.getHeader(apiKeyHeaderName);

        if (actualApiKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!actualApiKey.equals(apiKey)) {
            throw new InvalidAuthenticationException("Invalid API key provided for internal access.");
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "internal-service-user", null, AuthorityUtils.createAuthorityList("ROLE_INTERNAL_SERVICE"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}