package org.example.dto;

// Data Transfer Object für Registrierung Request
// Empfängt Username und Passwort vom Client für neue User
public class RegisterRequest {
    private String username;
    private String password;

    // Standard-Konstruktor (für Jackson JSON-Mapping)
    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

