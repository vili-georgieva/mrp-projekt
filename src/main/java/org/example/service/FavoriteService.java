package org.example.service;

import org.example.model.MediaEntry;
import org.example.repository.FavoriteRepository;
import org.example.repository.MediaRepository;

import java.util.List;

// Business Logic Layer für Favorites-Management
// User können Media als Favoriten markieren
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MediaRepository mediaRepository;

    // Constructor mit Dependency Injection (für Tests und SOLID DIP)
    public FavoriteService(FavoriteRepository favoriteRepository, MediaRepository mediaRepository) {
        this.favoriteRepository = favoriteRepository;
        this.mediaRepository = mediaRepository;
    }

    // Default constructor (production use)
    public FavoriteService() {
        this(new FavoriteRepository(), new MediaRepository());
    }

    // Toggles favorite status (add if not present, remove if present)
    // Returns true if added, false if removed
    public boolean toggleFavorite(String username, int mediaId) {
        // Check if media exists
        MediaEntry media = mediaRepository.getMediaById(mediaId);
        if (media == null) {
            throw new IllegalArgumentException("Media with ID " + mediaId + " does not exist");
        }

        // Check if already marked as favorite
        if (favoriteRepository.isFavorite(username, mediaId)) {
            // Remove favorite
            favoriteRepository.removeFavorite(username, mediaId);
            return false;  // Wurde entfernt
        } else {
            // Add favorite
            favoriteRepository.addFavorite(username, mediaId);
            return true;  // Wurde hinzugefügt
        }
    }

    // Adds a media to a user's favorites
    public void addFavorite(String username, int mediaId) {
        // Check if media exists
        MediaEntry media = mediaRepository.getMediaById(mediaId);
        if (media == null) {
            throw new IllegalArgumentException("Media with ID " + mediaId + " does not exist");
        }

        // Check if already marked as favorite
        if (favoriteRepository.isFavorite(username, mediaId)) {
            throw new IllegalStateException("Media is already in favorites");
        }

        favoriteRepository.addFavorite(username, mediaId);
    }

    // Removes a media from a user's favorites
    public void removeFavorite(String username, int mediaId) {
        // Check if marked as favorite
        if (!favoriteRepository.isFavorite(username, mediaId)) {
            throw new IllegalStateException("Media is not in favorites");
        }

        favoriteRepository.removeFavorite(username, mediaId);
    }

    // Gets all favorites of a user
    public List<MediaEntry> getFavorites(String username) {
        return favoriteRepository.getFavoritesByUser(username);
    }

    // Checks if a media was favorited by the user
    public boolean isFavorite(String username, int mediaId) {
        return favoriteRepository.isFavorite(username, mediaId);
    }

    // Gets the number of users who favorited a specific media
    public int getFavoriteCount(int mediaId) {
        return favoriteRepository.getFavoriteCount(mediaId);
    }

    // Gets all favorite IDs of a user
    public List<Integer> getFavoriteIds(String username) {
        return favoriteRepository.getFavoriteIdsByUser(username);
    }
}
