package com.example.ImportEase.services;

import com.example.ImportEase.dtos.AuthDtos.RegisterRequest;
import com.example.ImportEase.dtos.AuthDtos.LoginRequest;
import com.example.ImportEase.dtos.AuthDtos.AuthResponse;
import com.example.ImportEase.models.User;
import com.example.ImportEase.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Dependency Injection: Spring automatically provides these required tools
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // 2. Create the new user entity
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password())); // Hash the password immediately!
        user.setPhoneNumber(request.phoneNumber());

        // 3. Map the requested role string to our strict Enum
        try {
            user.setRole(User.Role.valueOf(request.role().toUpperCase()));
        } catch (IllegalArgumentException e) {
            user.setRole(User.Role.IMPORTER); // Default fallback if they send a weird role
        }

        // 4. Save to PostgreSQL
        userRepository.save(user);

        // 5. Generate their token and return it
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", "Registration successful");
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Find the user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // 2. Verify the hashed password matches
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // 3. Generate token and let them in
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", "Login successful");
    }
}