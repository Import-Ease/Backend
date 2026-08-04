package com.example.importease.config;

import com.example.importease.model.AppUser;
import com.example.importease.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser("admin", "admin@example.com", "Admin123!", "ADMIN", "Admin User");
        seedUser("importer", "importer@example.com", "Importer123!", "IMPORTER", "John Doe");
        seedUser("supplier", "supplier@example.com", "Supplier123!", "SUPPLIER", "Jane Smith");
    }

    private void seedUser(String username, String email, String password, String role, String fullName) {
        if (userRepository.findByEmailIgnoreCase(email).isEmpty()) {
            AppUser user = new AppUser();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setFullName(fullName);
            user.setPasswordSet(true);
            user.setEmailVerified(true);
            userRepository.save(user);
            log.info("Seeded {} user: {} / {}", role.toLowerCase(), email, password);
        } else {
            log.info("{} user already exists, skipping.", email);
        }
    }
}
