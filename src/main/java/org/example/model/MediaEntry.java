package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class MediaEntry {
    private int id;
    private String title;
    private String description;
    private MediaType mediaType; // movie, series, game
    private int releaseYear;
    private List<String> genres; //action, drama, comedy...
    private int ageRestriction;
    private String creator;
    private List<Rating> ratings;
    private double averageScore;

    public MediaEntry() {
        this.genres = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }

    public MediaEntry(int id, String title, String description, MediaType mediaType,
                      int releaseYear, List<String> genres, int ageRestriction, String creator) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.releaseYear = releaseYear;
        this.genres = genres != null ? genres : new ArrayList<>();
        this.ageRestriction = ageRestriction;
        this.creator = creator;
        this.ratings = new ArrayList<>();
        this.averageScore = 0.0;
    }

    public void calculateAverageScore() {
        if (ratings.isEmpty()) {
            this.averageScore = 0.0;
        } else {
            this.averageScore = ratings.stream()
                    .mapToInt(Rating::getStars)
                    .average()
                    .orElse(0.0);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public int getAgeRestriction() {
        return ageRestriction;
    }

    public void setAgeRestriction(int ageRestriction) {
        this.ageRestriction = ageRestriction;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }
}

