package org.example.repository;

import org.example.model.Rating;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingRepository {

    // Creates ratings table on server start
    public void createTable() {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "CREATE TABLE IF NOT EXISTS ratings (" +
                    "id SERIAL PRIMARY KEY," +
                    "media_id INTEGER NOT NULL," +
                    "username VARCHAR(255) NOT NULL," +
                    "stars INTEGER NOT NULL CHECK (stars >= 1 AND stars <= 5)," +
                    "comment TEXT," +
                    "confirmed BOOLEAN DEFAULT false," +
                    "likes INTEGER DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE(media_id, username)," +
                    "FOREIGN KEY (media_id) REFERENCES media_entries(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE" +
                    ")";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Create a new rating or update existing one
    public Rating createRating(Rating rating) throws SQLException {
        String sql = "INSERT INTO ratings (media_id, username, stars, comment, confirmed, likes) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (media_id, username) DO UPDATE SET " +
                     "stars = EXCLUDED.stars, comment = EXCLUDED.comment, updated_at = CURRENT_TIMESTAMP " +
                     "RETURNING id, created_at, updated_at";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rating.getMediaId());
            pstmt.setString(2, rating.getUsername());
            pstmt.setInt(3, rating.getStars());
            pstmt.setString(4, rating.getComment());
            pstmt.setBoolean(5, rating.isConfirmed());
            pstmt.setInt(6, rating.getLikes());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    rating.setId(rs.getInt("id"));
                    rating.setTimestamp(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
            return rating;
        }
    }

    // Update an existing rating
    public boolean updateRating(int ratingId, int stars, String comment) throws SQLException {
        String sql = "UPDATE ratings SET stars = ?, comment = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, stars);
            pstmt.setString(2, comment);
            pstmt.setInt(3, ratingId);

            return pstmt.executeUpdate() > 0;
        }
    }

    // Update only the comment of a rating
    public boolean updateComment(int ratingId, String comment) throws SQLException {
        String sql = "UPDATE ratings SET comment = ?, updated_at = CURRENT_TIMESTAMP, confirmed = false " +
                     "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, comment);
            pstmt.setInt(2, ratingId);

            return pstmt.executeUpdate() > 0;
        }
    }

    // Delete a rating by ID
    public boolean deleteRating(int ratingId) throws SQLException {
        String sql = "DELETE FROM ratings WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ratingId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Get rating by ID
    public Rating getRatingById(int id) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRating(rs);
                }
            }
        }
        return null;
    }

    // Get all ratings for a specific media
    public List<Rating> getRatingsByMediaId(int mediaId) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE media_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mediaId);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Rating> ratings = new ArrayList<>();
                while (rs.next()) {
                    ratings.add(mapResultSetToRating(rs));
                }
                return ratings;
            }
        }
    }

    // Get confirmed ratings for a specific media
    public List<Rating> getConfirmedRatingsByMediaId(int mediaId) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE media_id = ? AND confirmed = true ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mediaId);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Rating> ratings = new ArrayList<>();
                while (rs.next()) {
                    ratings.add(mapResultSetToRating(rs));
                }
                return ratings;
            }
        }
    }

    // Get rating by media and user (check if exists)
    public Rating getRatingByMediaAndUser(int mediaId, String username) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE media_id = ? AND username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mediaId);
            pstmt.setString(2, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRating(rs);
                }
            }
        }
        return null;
    }

    // Get all ratings by a user
    public List<Rating> getRatingsByUser(String username) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE username = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Rating> ratings = new ArrayList<>();
                while (rs.next()) {
                    ratings.add(mapResultSetToRating(rs));
                }
                return ratings;
            }
        }
    }

    // Increment likes for a rating
    public boolean incrementLikes(int ratingId) throws SQLException {
        String sql = "UPDATE ratings SET likes = likes + 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ratingId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Confirm a rating (moderation)
    public boolean confirmRating(int ratingId) throws SQLException {
        String sql = "UPDATE ratings SET confirmed = true WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ratingId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Calculate average rating for a media
    public double getAverageRating(int mediaId) throws SQLException {
        String sql = "SELECT AVG(stars) as avg_rating FROM ratings WHERE media_id = ? AND confirmed = true";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mediaId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        }
        return 0.0;
    }

    // Helper method to map ResultSet to Rating object
    private Rating mapResultSetToRating(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getInt("id"));
        rating.setMediaId(rs.getInt("media_id"));
        rating.setUsername(rs.getString("username"));
        rating.setStars(rs.getInt("stars"));
        rating.setComment(rs.getString("comment"));
        rating.setConfirmed(rs.getBoolean("confirmed"));
        rating.setLikes(rs.getInt("likes"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            rating.setTimestamp(created.toLocalDateTime());
        }

        return rating;
    }
}
