package org.example.repository;

import org.example.model.MediaEntry;
import org.example.model.MediaType;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoriteRepository {

    // Creates favorites table on server start
    public void createTable() {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "CREATE TABLE IF NOT EXISTS favorites (" +
                    "username VARCHAR(255) NOT NULL," +
                    "media_id INTEGER NOT NULL," +
                    "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (username, media_id)," +
                    "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE," +
                    "FOREIGN KEY (media_id) REFERENCES media_entries(id) ON DELETE CASCADE" +
                    ")";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Adds a media to a user's favorites
    public boolean addFavorite(String username, int mediaId) throws SQLException {
        String sql = "INSERT INTO favorites (username, media_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, mediaId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Removes a media from a user's favorites
    public boolean removeFavorite(String username, int mediaId) throws SQLException {
        String sql = "DELETE FROM favorites WHERE username = ? AND media_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, mediaId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Checks if a media is in a user's favorites
    public boolean isFavorite(String username, int mediaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM favorites WHERE username = ? AND media_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, mediaId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Gets all favorites of a user (returns complete MediaEntry objects)
    public List<MediaEntry> getFavoritesByUser(String username) throws SQLException {
        String sql = "SELECT m.* FROM media_entries m " +
                    "INNER JOIN favorites f ON m.id = f.media_id " +
                    "WHERE f.username = ? " +
                    "ORDER BY f.added_at DESC";

        List<MediaEntry> favorites = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MediaEntry media = new MediaEntry();
                    media.setId(rs.getInt("id"));
                    media.setTitle(rs.getString("title"));
                    media.setDescription(rs.getString("description"));

                    String typeStr = rs.getString("type");
                    if (typeStr != null) {
                        media.setMediaType(MediaType.valueOf(typeStr.toUpperCase()));
                    }

                    String genresStr = rs.getString("genres");
                    if (genresStr != null && !genresStr.isEmpty()) {
                        media.setGenres(Arrays.asList(genresStr.split(",")));
                    }

                    media.setAgeRestriction(rs.getInt("age_restriction"));
                    media.setCreator(rs.getString("creator_username"));

                    favorites.add(media);
                }
            }
        }

        return favorites;
    }

    // Gets all favorite media IDs of a user
    public List<Integer> getFavoriteIdsByUser(String username) throws SQLException {
        String sql = "SELECT media_id FROM favorites WHERE username = ? ORDER BY added_at DESC";

        List<Integer> favoriteIds = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    favoriteIds.add(rs.getInt("media_id"));
                }
            }
        }

        return favoriteIds;
    }

    // Gets the number of users who favorited a specific media
    public int getFavoriteCount(int mediaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM favorites WHERE media_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mediaId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
