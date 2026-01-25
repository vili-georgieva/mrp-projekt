package org.example.util;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.function.Function;

// Utility-Klasse für Datenbankverbindung und Transaction-Management
// Lädt DB-Config aus application.properties und verwaltet Connections
public class DatabaseConnection {
    private static final String dbUrl;  // Datenbank URL (z.B. jdbc:postgresql://localhost:5432/mrp)
    private static final String dbUsername;  // DB Username
    private static final String dbPassword;  // DB Passwort

    static { // static because we want to load it once when the class is first used
        try {
            // Load DB configuration
            Properties props = new Properties();
            // Lädt application.properties aus resources-Ordner
            try (InputStream input = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {

                if (input == null) {
                    throw new RuntimeException("application.properties not found!");
                }
                props.load(input);
            }

            // Liest DB-Konfiguration aus Properties-Datei
            dbUrl = props.getProperty("db.url");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");

            System.out.println("Database configuration loaded");
        } catch (Exception e) {
            throw new RuntimeException("Error loading DB configuration: " + e.getMessage(), e);
        }
    }

    // Erstellt neue Datenbankverbindung
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    // Executes a transaction and returns a result (use null for void operations)
    // Transaction: Alle DB-Operationen werden zusammen ausgeführt oder komplett zurückgerollt
    public static <T> T executeInTransaction(Function<Connection, T> operation) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);  // Manuelles Transaction-Management
            try {
                T result = operation.apply(conn);  // Führt übergebene Operation aus
                conn.commit();  // Bei Erfolg: Speichern der Änderungen
                return result;
            } catch (Exception e) {
                conn.rollback();  // Bei Fehler: Rückgängig machen aller Änderungen
                throw e instanceof RuntimeException ? (RuntimeException) e
                    : new RuntimeException("Transaction failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }
}

