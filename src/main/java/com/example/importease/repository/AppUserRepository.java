package com.example.importease.repository;

import com.example.importease.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    java.util.Optional<AppUser> findByUsername(String username);

    @Query("SELECT u FROM AppUser u WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)")
    Optional<AppUser> findByUsernameOrEmail(@Param("identifier") String identifier);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM AppUser u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<AppUser> searchUsers(@Param("query") String query);

    List<AppUser> findByRoleOrderByCreatedAtDesc(String role);

    List<AppUser> findAllByOrderByCreatedAtDesc();
}