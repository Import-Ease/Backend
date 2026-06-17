package com.example.importease.service;

import com.example.importease.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.importease.repository.UserRepository;

@Service

public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        return userRepository.save(user);
    }
}
