package org.example.model;

import java.time.LocalDateTime;

public class Rating {
    private int id;
    private int mediaId;
    private String username;
    private int stars;
    private String comment;
    private LocalDateTime timestamp;
    private int likes;
    private boolean confirmed;

    public Rating() {
        this.timestamp = LocalDateTime.now();
        this.likes = 0;
        this.confirmed = false;
    }

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

