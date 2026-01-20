package org.example.service;

import org.example.model.MediaEntry;
import org.example.repository.FavoriteRepository;
import org.example.repository.MediaRepository;

import java.sql.SQLException;
import java.util.List;

public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MediaRepository mediaRepository;

    public FavoriteService() {
        this.favoriteRepository = new FavoriteRepository();
        this.mediaRepository = new MediaRepository();
    }

    // Konstruktor für Tests (Dependency Injection)
    public FavoriteService(FavoriteRepository favoriteRepository, MediaRepository mediaRepository) {
        this.favoriteRepository = favoriteRepository;
        this.mediaRepository = mediaRepository;
    }

    /**
     * Wechselt den Favoriten-Status (hinzufügen wenn nicht vorhanden, entfernen wenn vorhanden)
     * Gibt true zurück wenn hinzugefügt, false wenn entfernt
     */
    public boolean toggleFavorite(String username, int mediaId) throws SQLException {
        // Prüfe ob Medium existiert
        MediaEntry media = mediaRepository.getMediaById(mediaId);
        if (media == null) {
            throw new IllegalArgumentException("Medium mit ID " + mediaId + " existiert nicht");
        }

        // Prüfe ob bereits als Favorit markiert
        if (favoriteRepository.isFavorite(username, mediaId)) {
            // Entferne Favorit
            favoriteRepository.removeFavorite(username, mediaId);
            return false;
        } else {
            // Füge Favorit hinzu
            favoriteRepository.addFavorite(username, mediaId);
            return true;
        }
    }

    /**
     * Fügt ein Medium zu den Favoriten eines Benutzers hinzu
     */
    public void addFavorite(String username, int mediaId) throws SQLException {
        // Prüfe ob Medium existiert
        MediaEntry media = mediaRepository.getMediaById(mediaId);
        if (media == null) {
            throw new IllegalArgumentException("Medium mit ID " + mediaId + " existiert nicht");
        }

        // Prüfe ob bereits als Favorit markiert
        if (favoriteRepository.isFavorite(username, mediaId)) {
            throw new IllegalStateException("Medium ist bereits in den Favoriten");
        }

        favoriteRepository.addFavorite(username, mediaId);
    }

    /**
     * Entfernt ein Medium aus den Favoriten eines Benutzers
     */
    public void removeFavorite(String username, int mediaId) throws SQLException {
        // Prüfe ob als Favorit markiert
        if (!favoriteRepository.isFavorite(username, mediaId)) {
            throw new IllegalStateException("Medium ist nicht in den Favoriten");
        }

        favoriteRepository.removeFavorite(username, mediaId);
    }

    /**
     * Holt alle Favoriten eines Benutzers
     */
    public List<MediaEntry> getFavorites(String username) throws SQLException {
        return favoriteRepository.getFavoritesByUser(username);
    }

    /**
     * Prüft, ob ein Medium vom Benutzer favorisiert wurde
     */
    public boolean isFavorite(String username, int mediaId) throws SQLException {
        return favoriteRepository.isFavorite(username, mediaId);
    }

    /**
     * Holt die Anzahl der Benutzer, die ein bestimmtes Medium favorisiert haben
     */
    public int getFavoriteCount(int mediaId) throws SQLException {
        return favoriteRepository.getFavoriteCount(mediaId);
    }

    /**
     * Holt alle Favoriten-IDs eines Benutzers
     */
    public List<Integer> getFavoriteIds(String username) throws SQLException {
        return favoriteRepository.getFavoriteIdsByUser(username);
    }
}
