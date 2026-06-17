package com.example.importease.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.importease.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

    boolean existsByEmail(String email);
}
