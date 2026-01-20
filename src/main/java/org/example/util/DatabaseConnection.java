package org.example.util;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.function.Function;

public class DatabaseConnection {
    private static final String dbUrl;
    private static final String dbUsername;
    private static final String dbPassword;

    static { // static because we want to load it once when the class is first used
        try {
            // Load DB configuration
            Properties props = new Properties();
            try (InputStream input = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {

                if (input == null) {
                    throw new RuntimeException("application.properties not found!");
                }
                props.load(input);
            }

            dbUrl = props.getProperty("db.url");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");

            System.out.println("Database configuration loaded");
        } catch (Exception e) {
            throw new RuntimeException("Error loading DB configuration: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    // Executes a transaction and returns a result (use null for void operations)
    public static <T> T executeInTransaction(Function<Connection, T> operation) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = operation.apply(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e instanceof RuntimeException ? (RuntimeException) e
                    : new RuntimeException("Transaction failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }
}

