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

    // Default Constructor 
    public FavoriteService() {
        this(new FavoriteRepository(), new MediaRepository());
    }

    // Wechselt Favorite-Status (fügt hinzu wenn nicht vorhanden, entfernt wenn vorhanden)
    // Gibt true zurück wenn hinzugefügt, false wenn entfernt
    public boolean toggleFavorite(String username, int mediaId) {
        // Prüft ob Media existiert
        MediaEntry media = mediaRepository.getMediaById(mediaId);
        if (media == null) {
            throw new IllegalArgumentException("Media with ID " + mediaId + " does not exist");
        }

        // Prüft ob bereits als Favorite markiert
        if (favoriteRepository.isFavorite(username, mediaId)) {
            // Entfernt Favorite
            favoriteRepository.removeFavorite(username, mediaId);
            return false;  // Wurde entfernt
        } else {
            // Fügt Favorite hinzu
            favoriteRepository.addFavorite(username, mediaId);
            return true;  // Wurde hinzugefügt
        }
    }

    // Fügt ein Media zu den Favorites eines Users hinzu
    public void addFavorite(String username, int mediaId) {
        // Prüft ob Media existiert
        MediaEntry media = mediaRepository.getMediaById(mediaId);
        if (media == null) {
            throw new IllegalArgumentException("Media with ID " + mediaId + " does not exist");
        }

        // Prüft ob bereits als Favorite markiert
        if (favoriteRepository.isFavorite(username, mediaId)) {
            throw new IllegalStateException("Media is already in favorites");
        }

        favoriteRepository.addFavorite(username, mediaId);
    }

    // Entfernt ein Media aus den Favorites eines Users
    public void removeFavorite(String username, int mediaId) {
        // Prüft ob als Favorite markiert
        if (!favoriteRepository.isFavorite(username, mediaId)) {
            throw new IllegalStateException("Media is not in favorites");
        }

        favoriteRepository.removeFavorite(username, mediaId);
    }

    // Holt alle Favorites eines Users
    public List<MediaEntry> getFavorites(String username) {
        return favoriteRepository.getFavoritesByUser(username);
    }

    // Prüft ob ein Media vom User favorisiert wurde
    public boolean isFavorite(String username, int mediaId) {
        return favoriteRepository.isFavorite(username, mediaId);
    }

    // Holt die Anzahl der User die ein spezifisches Media favorisiert haben
    public int getFavoriteCount(int mediaId) {
        return favoriteRepository.getFavoriteCount(mediaId);
    }

    // Holt alle Favorite-IDs eines Users
    public List<Integer> getFavoriteIds(String username) {
        return favoriteRepository.getFavoriteIdsByUser(username);
    }
}
