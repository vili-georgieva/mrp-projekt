package org.example.util;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.function.Function;

public class DatabaseConnection {
    private static final String dbUrl;
    private static final String dbUsername;
    private static final String dbPassword;

    static {
        try {
            Class.forName("org.postgresql.Driver");

            // Load DB configuration
            Properties props = new Properties();
            InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties");

            if (input == null) {
                throw new RuntimeException("application.properties not found!");
            }

            props.load(input);
            dbUrl = props.getProperty("db.url");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");
            input.close();

            System.out.println("Database configuration loaded");
        } catch (Exception e) {
            throw new RuntimeException("Error loading DB configuration: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() {
        try {
            // Create NEW connection for each request
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (Exception e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    // Executes a transaction and returns a result
    public static <T> T executeInTransaction(Function<Connection, T> operation) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            T result = operation.apply(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // Executes a transaction without return value
    public static void executeInTransactionVoid(TransactionConsumer operation) {
        executeInTransaction(conn -> {
            try {
                operation.accept(conn);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @FunctionalInterface
    public interface TransactionConsumer {
        void accept(Connection conn) throws SQLException;
    }
}

