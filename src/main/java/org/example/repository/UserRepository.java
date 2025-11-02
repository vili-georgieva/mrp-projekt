package org.example.repository;

import org.example.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository  {
    private final Connection connection;

    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "username VARCHAR(255) PRIMARY KEY," +
                "password VARCHAR(255) NOT NULL," +
                "token VARCHAR(255)" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, token) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getToken());
            stmt.executeUpdate();
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setToken(rs.getString("token"));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByToken(String token) throws SQLException {
        String sql = "SELECT * FROM users WHERE token = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setToken(rs.getString("token"));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public void updateToken(String username, String token) throws SQLException {
        String sql = "UPDATE users SET token = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setToken(rs.getString("token"));
                users.add(user);
            }
        }
        return users;
    }
}

