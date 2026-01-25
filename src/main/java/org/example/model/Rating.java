package org.example.model;

import java.time.LocalDateTime;

// Domain Model für Rating (Bewertung von Media durch User)
// Ein User kann pro Media nur ein Rating abgeben
public class Rating {
    private int id;  // Eindeutige ID (Auto-generiert von DB)
    private int mediaId;  // Referenz zum bewerteten Media
    private String username;  // User der das Rating erstellt hat
    private int stars;  // Bewertung 1-5 Sterne
    private String comment;  // Optionaler Kommentar
    private LocalDateTime timestamp;  // Zeitpunkt der Erstellung
    private int likes;  // Anzahl Likes für dieses Rating
    private boolean confirmed;  // Moderation Status (true = freigegeben)

    // Standard-Konstruktor (für Jackson JSON-Mapping)
    public Rating() {
        this.timestamp = LocalDateTime.now();
        this.likes = 0;
        this.confirmed = false;
    }

    // Konstruktor für neues Rating
    public Rating(int id, int mediaId, String username, int stars, String comment) {
        this.id = id;
        this.mediaId = mediaId;
        this.username = username;
        this.stars = stars;
        this.comment = comment;
        this.timestamp = LocalDateTime.now();
        this.likes = 0;
        this.confirmed = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}

