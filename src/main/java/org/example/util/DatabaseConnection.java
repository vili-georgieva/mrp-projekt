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

            // DB-Konfiguration laden
            Properties props = new Properties();
            InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties");

            if (input == null) {
                throw new RuntimeException("application.properties nicht gefunden!");
            }

            props.load(input);
            dbUrl = props.getProperty("db.url");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");
            input.close();

            System.out.println("Datenbank-Konfiguration geladen");
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Laden der DB-Konfiguration: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() {
        try {
            // Erstelle NEUE Connection für jeden Request
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (Exception e) {
            throw new RuntimeException("Datenbankverbindung fehlgeschlagen: " + e.getMessage(), e);
        }
    }

    /**
     * Führt eine Transaktion aus und gibt ein Ergebnis zurück.
     */
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
            throw new RuntimeException("Transaktion fehlgeschlagen: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Führt eine Transaktion ohne Rückgabewert aus.
     */
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

