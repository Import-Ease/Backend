package com.example.importease.config;

import com.example.importease.service.JwtService;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.model.AppUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserRepository userRepository;

    // Correctly closed constructor initializing both final service beans
    public JwtAuthenticationFilter(JwtService jwtService, AppUserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userIdentifier;

        // 1. Check if the Authorization header exists and has the Bearer prefix
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract JWT token (skip "Bearer " prefix which is 7 characters)
        jwt = authHeader.substring(7);
        userIdentifier = jwtService.extractUsername(jwt);

        // 3. Process authentication if user identifier exists and security context is unauthenticated
        if (userIdentifier != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Query database to check if user exists by email OR username (case-insensitive)
            AppUser appUser = userRepository.findByUsernameOrEmail(userIdentifier)
                    .orElse(null);

            if (appUser != null) {
                if (!appUser.isEnabled()) {
                    filterChain.doFilter(request, response);
                    return;
                }
                String jwtSubject = appUser.getEmail() != null ? appUser.getEmail() : appUser.getUsername();
                if (jwtService.isTokenValid(jwt, org.springframework.security.core.userdetails.User
                        .withUsername(jwtSubject)
                        .password("")
                        .authorities(appUser.getRole())
                        .build())) {

                    // Create Spring UserDetails with designated database roles
                    UserDetails userDetails = User.withUsername(userIdentifier)
                            .password("")
                            .authorities(appUser.getRole())
                            .build();

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        // Pass request and response forward to subsequent filters
        filterChain.doFilter(request, response);
    }
}