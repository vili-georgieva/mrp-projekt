package org.example.repository;

import org.example.model.User;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public void createTable() {
        DatabaseConnection.executeInTransactionVoid(conn -> {
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(255) PRIMARY KEY," +
                    "password_hash VARCHAR(255) NOT NULL," +
                    "token VARCHAR(500)" +
                    ")";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        });
    }

    public void save(User user) {
        DatabaseConnection.executeInTransactionVoid(conn -> {
            String sql = "INSERT INTO users (username, password_hash, token) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getToken());
                stmt.executeUpdate();
            }
        });
    }

    public Optional<User> findByUsername(String username) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setToken(rs.getString("token"));
                    return Optional.of(user);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return Optional.empty();
        });
    }

    public Optional<User> findByToken(String token) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE token = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, token);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setToken(rs.getString("token"));
                    return Optional.of(user);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return Optional.empty();
        });
    }

    public void updateToken(String username, String token) {
        DatabaseConnection.executeInTransactionVoid(conn -> {
            String sql = "UPDATE users SET token = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, token);
                stmt.setString(2, username);
                stmt.executeUpdate();
            }
        });
    }

    public List<User> findAll() {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM users";
            List<User> users = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setToken(rs.getString("token"));
                    users.add(user);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return users;
        });
    }
}

