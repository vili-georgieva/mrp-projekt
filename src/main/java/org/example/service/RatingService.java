package org.example.service;

import org.example.model.Rating;
import org.example.repository.MediaRepository;
import org.example.repository.RatingRepository;

import java.sql.SQLException;
import java.util.List;

public class RatingService {

    private final RatingRepository ratingRepository;
    private final MediaRepository mediaRepository;

    public RatingService() {
        this.ratingRepository = new RatingRepository();
        this.mediaRepository = new MediaRepository();
    }

    // Create or update a rating for a media entry
    // Business Logic: One user can only have ONE rating per media
    // If rating exists, it will be updated. Otherwise, a new one is created
    public Rating createOrUpdateRating(int mediaId, String username, int stars, String comment) throws SQLException {
        // Validate stars range
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }

        // Check if rating already exists
        Rating existingRating = ratingRepository.getRatingByMediaAndUser(mediaId, username);

        Rating rating = new Rating();
        rating.setMediaId(mediaId);
        rating.setUsername(username);
        rating.setStars(stars);
        rating.setComment(comment);
        rating.setConfirmed(false); // New comments need moderation
        rating.setLikes(0);

        if (existingRating != null) {
            // Update existing rating
            rating.setId(existingRating.getId());
            rating.setLikes(existingRating.getLikes()); // Keep existing likes
            ratingRepository.updateRating(existingRating.getId(), stars, comment);
            rating = ratingRepository.getRatingById(existingRating.getId());
        } else {
            // Create new rating
            rating = ratingRepository.createRating(rating);
        }

        // Recalculate and update average rating for the media
        updateMediaAverageRating(mediaId);

        return rating;
    }

    // Delete a rating. Only the owner can delete their rating
    public boolean deleteRating(int ratingId, String username) throws SQLException {
        Rating rating = ratingRepository.getRatingById(ratingId);

        if (rating == null) {
            return false;
        }

        // Check ownership
        if (!rating.getUsername().equals(username)) {
            throw new SecurityException("You can only delete your own ratings");
        }

        boolean deleted = ratingRepository.deleteRating(ratingId);

        if (deleted) {
            // Recalculate average rating after deletion
            updateMediaAverageRating(rating.getMediaId());
        }

        return deleted;
    }

    // Update only the comment of a rating (owner only)
    public boolean updateComment(int ratingId, String username, String newComment) throws SQLException {
        Rating rating = ratingRepository.getRatingById(ratingId);

        if (rating == null) {
            return false;
        }

        // Check ownership
        if (!rating.getUsername().equals(username)) {
            throw new SecurityException("You can only update your own comments");
        }

        return ratingRepository.updateComment(ratingId, newComment);
    }

    // Delete only the comment of a rating (keeps the stars, owner only)
    public boolean deleteComment(int ratingId, String username) throws SQLException {
        Rating rating = ratingRepository.getRatingById(ratingId);

        if (rating == null) {
            return false;
        }

        // Check ownership
        if (!rating.getUsername().equals(username)) {
            throw new SecurityException("You can only delete your own comments");
        }

        // Delete comment by setting it to empty string
        return ratingRepository.updateComment(ratingId, "");
    }

    // Like a rating (increment like counter)
    public boolean likeRating(int ratingId) throws SQLException {
        return ratingRepository.incrementLikes(ratingId);
    }

    // Confirm a rating (moderation - set confirmed=true)
    public boolean confirmRating(int ratingId) throws SQLException {
        boolean confirmed = ratingRepository.confirmRating(ratingId);

        if (confirmed) {
            // Recalculate average since confirmed ratings affect the average
            Rating rating = ratingRepository.getRatingById(ratingId);
            if (rating != null) {
                updateMediaAverageRating(rating.getMediaId());
            }
        }

        return confirmed;
    }

    // Get all ratings for a specific media
    public List<Rating> getRatingsByMediaId(int mediaId) throws SQLException {
        return ratingRepository.getRatingsByMediaId(mediaId);
    }

    // Get only confirmed ratings for a specific media
    public List<Rating> getConfirmedRatingsByMediaId(int mediaId) throws SQLException {
        return ratingRepository.getConfirmedRatingsByMediaId(mediaId);
    }

    // Get rating history for a user
    public List<Rating> getRatingHistory(String username) throws SQLException {
        return ratingRepository.getRatingsByUser(username);
    }

    // Get a specific rating by ID
    public Rating getRatingById(int ratingId) throws SQLException {
        return ratingRepository.getRatingById(ratingId);
    }

    // Check if a user has already rated a media
    public Rating getUserRatingForMedia(int mediaId, String username) throws SQLException {
        return ratingRepository.getRatingByMediaAndUser(mediaId, username);
    }

    // Calculate and update the average rating for a media entry
    // Only confirmed ratings are included in the average
    private void updateMediaAverageRating(int mediaId) throws SQLException {
        double avgRating = ratingRepository.getAverageRating(mediaId);
        mediaRepository.updateAverageRating(mediaId, avgRating);
    }
}
