package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password) {
        // Check if user already exists
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Hash password for better security
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword);
        userRepository.save(user);
        return user;
    }

    public String login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        User user = userOpt.get();
        String hashedPassword = hashPassword(password);
        if (!user.getPassword().equals(hashedPassword)) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Generate secure token with UUID
        String token = generateSecureToken();
        userRepository.updateToken(username, token);
        return token;
    }

    public Optional<User> validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByToken(token);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Generates a secure token with UUID
    private String generateSecureToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    // Hashes password with SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}

