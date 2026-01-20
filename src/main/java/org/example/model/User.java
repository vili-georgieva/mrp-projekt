package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String token;
    private List<MediaEntry> createdMedia;
    private List<Rating> ratings;

    public User() {
        this.createdMedia = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }

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

