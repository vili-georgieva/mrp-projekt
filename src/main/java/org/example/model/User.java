package org.example.model;

import java.util.ArrayList;
import java.util.List;

// Domain Model für User (Benutzer der Plattform)
// Enthält Benutzerdaten und Session-Token für Authentifizierung
public class User {
    private String username;  // Eindeutiger Benutzername (Primary Key)
    private String password;  // Gehashtes Passwort (SHA-256)
    private String token;  // Session-Token für authentifizierte Requests
    private List<MediaEntry> createdMedia;  // Media-Einträge vom User erstellt
    private List<Rating> ratings;  // Ratings vom User erstellt

    // Standard-Konstruktor (für Jackson JSON-Mapping)
    public User() {
        this.createdMedia = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }

    // Konstruktor mit Username und Password
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.createdMedia = new ArrayList<>();
        this.ratings = new ArrayList<>();
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<MediaEntry> getCreatedMedia() {
        return createdMedia;
    }

    public void setCreatedMedia(List<MediaEntry> createdMedia) {
        this.createdMedia = createdMedia;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }
}

