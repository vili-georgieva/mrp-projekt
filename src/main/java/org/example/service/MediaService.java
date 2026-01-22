package org.example.service;

import org.example.model.MediaEntry;
import org.example.model.MediaType;
import org.example.model.User;
import org.example.repository.MediaRepository;

import java.util.List;
import java.util.Optional;

public class MediaService {
    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public MediaEntry createMedia(MediaEntry media, User creator) {

        if (media.getTitle() == null || media.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (media.getMediaType() == null) {
            throw new IllegalArgumentException("Media type cannot be empty");
        }

        media.setCreator(creator.getUsername());
        int id = mediaRepository.save(media);
        media.setId(id);
        return media;
    }

    public MediaEntry updateMedia(int id, MediaEntry updatedMedia, User user) {
        Optional<MediaEntry> existingOpt = mediaRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Media entry not found");
        }

        MediaEntry existing = existingOpt.get();
        if (!existing.getCreator().equals(user.getUsername())) {
            throw new IllegalArgumentException("You can only update your own media entries");
        }

        updatedMedia.setId(id);
        updatedMedia.setCreator(existing.getCreator());
        mediaRepository.update(updatedMedia);
        return updatedMedia;
    }

    public void deleteMedia(int id, User user) {
        Optional<MediaEntry> existingOpt = mediaRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Media entry not found");
        }

        MediaEntry existing = existingOpt.get();
        if (!existing.getCreator().equals(user.getUsername())) {
            throw new IllegalArgumentException("You can only delete your own media entries");
        }

        mediaRepository.delete(id);
    }

    public Optional<MediaEntry> getMediaById(int id) {
        return mediaRepository.findById(id);
    }

    public List<MediaEntry> getAllMedia() {
        return mediaRepository.findAll();
    }

    // Searches media with optional filters (null = no filter)
    public List<MediaEntry> searchMedia(String title, String genre, MediaType mediaType,
                                        Integer minRating, Integer ageRestriction) {
        return mediaRepository.searchMedia(title, genre, mediaType, minRating, ageRestriction);
    }
}

