package com.example.ImportEase.config;

import com.example.ImportEase.services.JwtService;
import com.example.ImportEase.repositories.AppUserRepository;
import com.example.ImportEase.models.AppUser;
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

            // Query database to check if user exists by either email or phone
            AppUser appUser = userRepository.findByEmail(userIdentifier)
                    .orElse(null);

            if (appUser != null && jwtService.isTokenValid(jwt, org.springframework.security.core.userdetails.User
                    .withUsername(appUser.getEmail() != null ? appUser.getEmail() : appUser.getPhoneNumber())
                    .password("")
                    .authorities(appUser.getRole())
                    .build())) {

                // Create Spring UserDetails with designated database roles
                UserDetails userDetails = User.withUsername(userIdentifier)
                        .password("") // OTP flow is passwordless
                        .authorities(appUser.getRole()) // e.g., "IMPORTER"
                        .build();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authenticated authentication context inside Spring Security Context Holder
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Pass request and response forward to subsequent filters
        filterChain.doFilter(request, response);
    }
}