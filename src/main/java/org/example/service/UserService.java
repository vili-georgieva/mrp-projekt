package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;

import java.sql.SQLException;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password) throws SQLException {
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

        User user = new User(username, password);
        userRepository.save(user);
        return user;
    }

    public String login(String username, String password) throws SQLException {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Generate token
        String token = username + "-mrpToken";
        userRepository.updateToken(username, token);
        return token;
    }

    public Optional<User> validateToken(String token) throws SQLException {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByToken(token);
    }

    public Optional<User> getUserByUsername(String username) throws SQLException {
        return userRepository.findByUsername(username);
    }
}

