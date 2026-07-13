package com.example.importease.repository;

import com.example.importease.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    java.util.Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
}