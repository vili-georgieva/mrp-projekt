package org.example.service;

import org.example.model.Rating;
import org.example.repository.MediaRepository;
import org.example.repository.RatingRepository;

import java.util.List;

// Business Logic Layer für Rating-Management
// Verwaltet Bewertungen von Media durch User
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MediaRepository mediaRepository;

    // Constructor
    public RatingService(RatingRepository ratingRepository, MediaRepository mediaRepository) {
        this.ratingRepository = ratingRepository;
        this.mediaRepository = mediaRepository;
    }

    // Default Constructor
    public RatingService() {
        this(new RatingRepository(), new MediaRepository());
    }

    // Erstellt oder aktualisiert Rating für Media-Eintrag
    // Business Logic: Ein User kann nur EIN Rating pro Media haben
    // Wenn Rating existiert, wird es aktualisiert. Sonst wird ein neues erstellt
    public Rating createOrUpdateRating(int mediaId, String username, int stars, String comment) {
        // Validiert Stars-Bereich
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }

        // Prüft ob Rating bereits existiert
        Rating existingRating = ratingRepository.getRatingByMediaAndUser(mediaId, username);

        Rating rating = new Rating();
        rating.setMediaId(mediaId);
        rating.setUsername(username);
        rating.setStars(stars);
        rating.setComment(comment);
        rating.setConfirmed(false); // Neue Kommentare benötigen Moderation
        rating.setLikes(0);

        if (existingRating != null) {
            // Aktualisiert bestehendes Rating
            rating.setId(existingRating.getId());
            rating.setLikes(existingRating.getLikes()); // Behält bestehende Likes
            ratingRepository.updateRating(existingRating.getId(), stars, comment);
            rating = ratingRepository.getRatingById(existingRating.getId());
        } else {
            // Erstellt neues Rating
            rating = ratingRepository.createRating(rating);
        }

        // Berechnet und aktualisiert durchschnittliche Bewertung für das Media
        updateMediaAverageRating(mediaId);

        return rating;
    }

    // Löscht ein Rating. Nur der Owner kann sein Rating löschen
    public boolean deleteRating(int ratingId, String username) {
        Rating rating = ratingRepository.getRatingById(ratingId);

        if (rating == null) {
            return false;
        }

        // Prüft Besitzrecht
        if (!rating.getUsername().equals(username)) {
            throw new SecurityException("You can only delete your own ratings");
        }

        boolean deleted = ratingRepository.deleteRating(ratingId);

        if (deleted) {
            // Berechnet durchschnittliche Bewertung nach Löschung neu
            updateMediaAverageRating(rating.getMediaId());
        }

        return deleted;
    }

    // Aktualisiert nur den Kommentar eines Ratings (nur Owner)
    public boolean updateComment(int ratingId, String username, String newComment) {
        Rating rating = ratingRepository.getRatingById(ratingId);

        if (rating == null) {
            return false;
        }

        // Prüft Besitzrecht
        if (!rating.getUsername().equals(username)) {
            throw new SecurityException("You can only update your own comments");
        }

        return ratingRepository.updateComment(ratingId, newComment);
    }

    // Löscht nur den Kommentar eines Ratings (behält die Stars, nur Owner)
    public boolean deleteComment(int ratingId, String username) {
        Rating rating = ratingRepository.getRatingById(ratingId);

        if (rating == null) {
            return false;
        }

        // Prüft Besitzrecht
        if (!rating.getUsername().equals(username)) {
            throw new SecurityException("You can only delete your own comments");
        }

        // Löscht Kommentar durch Setzen auf leeren String
        return ratingRepository.updateComment(ratingId, "");
    }

    // Liked ein Rating (erhöht Like-Counter)
    public boolean likeRating(int ratingId) {
        return ratingRepository.incrementLikes(ratingId);
    }

    // Bestätigt ein Rating (Moderation - setzt confirmed=true)
    // Nur bestätigte Ratings werden für Durchschnitt verwendet
    public boolean confirmRating(int ratingId) {
        boolean confirmed = ratingRepository.confirmRating(ratingId);

        if (confirmed) {
            // Berechnet Durchschnitt neu da bestätigte Ratings den Durchschnitt beeinflussen
            Rating rating = ratingRepository.getRatingById(ratingId);
            if (rating != null) {
                updateMediaAverageRating(rating.getMediaId());
            }
        }

        return confirmed;
    }

    // Holt alle Ratings für ein spezifisches Media
    public List<Rating> getRatingsByMediaId(int mediaId) {
        return ratingRepository.getRatingsByMediaId(mediaId);
    }

    // Holt nur bestätigte Ratings für ein spezifisches Media
    public List<Rating> getConfirmedRatingsByMediaId(int mediaId) {
        return ratingRepository.getConfirmedRatingsByMediaId(mediaId);
    }

    // Holt Rating-Historie für einen User
    public List<Rating> getRatingHistory(String username) {
        return ratingRepository.getRatingsByUser(username);
    }

    // Holt ein spezifisches Rating nach ID
    public Rating getRatingById(int ratingId) {
        return ratingRepository.getRatingById(ratingId);
    }

    // Prüft ob ein User bereits ein Media bewertet hat
    public Rating getUserRatingForMedia(int mediaId, String username) {
        return ratingRepository.getRatingByMediaAndUser(mediaId, username);
    }

    // Berechnet und aktualisiert die durchschnittliche Bewertung für einen Media-Eintrag
    // Nur bestätigte Ratings sind im Durchschnitt enthalten
    // Private weil nur intern verwendet
    private void updateMediaAverageRating(int mediaId) {
        double avgRating = ratingRepository.getAverageRating(mediaId);
        mediaRepository.updateAverageRating(mediaId, avgRating);
    }
}
