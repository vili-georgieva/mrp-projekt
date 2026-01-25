package org.example.service;

import org.example.model.MediaEntry;
import org.example.model.MediaType;
import org.example.model.User;
import org.example.repository.MediaRepository;

import java.util.List;
import java.util.Optional;

// Business Logic Layer für Media-Management
// Verwaltet CRUD-Operationen für Media-Einträge
public class MediaService {
    private final MediaRepository mediaRepository;

    // Constructor
    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    // Erstellt neuen Media-Eintrag (nur für eingeloggte User)
    public MediaEntry createMedia(MediaEntry media, User creator) {

        // Validierung: Pflichtfelder prüfen
        if (media.getTitle() == null || media.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (media.getMediaType() == null) {
            throw new IllegalArgumentException("Media type cannot be empty");
        }

        // Creator-Username wird aus eingeloggtem User gesetzt
        media.setCreator(creator.getUsername());
        int id = mediaRepository.save(media);  // Speichert in DB und gibt ID zurück
        media.setId(id);
        return media;
    }

    // Aktualisiert Media-Eintrag (nur Creator darf updaten)
    public MediaEntry updateMedia(int id, MediaEntry updatedMedia, User user) {
        Optional<MediaEntry> existingOpt = mediaRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Media entry not found");
        }

        MediaEntry existing = existingOpt.get();
        // Prüft Besitzrechte: Nur Creator darf updaten
        if (!existing.getCreator().equals(user.getUsername())) {
            throw new IllegalArgumentException("You can only update your own media entries");
        }

        // Setzt ID und Creator (dürfen nicht geändert werden)
        updatedMedia.setId(id);
        updatedMedia.setCreator(existing.getCreator());
        mediaRepository.update(updatedMedia);
        return updatedMedia;
    }

    // Löscht Media-Eintrag (nur Creator darf löschen)
    public void deleteMedia(int id, User user) {
        Optional<MediaEntry> existingOpt = mediaRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Media entry not found");
        }

        MediaEntry existing = existingOpt.get();
        // Prüft Besitzrechte
        if (!existing.getCreator().equals(user.getUsername())) {
            throw new IllegalArgumentException("You can only delete your own media entries");
        }

        mediaRepository.delete(id);
    }

    // Lädt einzelnen Media-Eintrag nach ID
    public Optional<MediaEntry> getMediaById(int id) {
        return mediaRepository.findById(id);
    }

    // Lädt alle Media-Einträge
    public List<MediaEntry> getAllMedia() {
        return mediaRepository.findAll();
    }

    // Searches media with optional filters (null = no filter)
    public List<MediaEntry> searchMedia(String title, String genre, MediaType mediaType,
                                        Integer minRating, Integer ageRestriction) {
        return mediaRepository.searchMedia(title, genre, mediaType, minRating, ageRestriction);
    }
}

