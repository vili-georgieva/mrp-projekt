package org.example.repository;

import org.example.model.MediaEntry;
import org.example.model.MediaType;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MediaRepository {

    // Creates table for media entries
    public void createTable() {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "CREATE TABLE IF NOT EXISTS media_entries (" +
                    "id SERIAL PRIMARY KEY," +
                    "title VARCHAR(255) NOT NULL," +
                    "description TEXT," +
                    "media_type VARCHAR(50) NOT NULL," +
                    "release_year INTEGER," +
                    "genres TEXT," +
                    "age_restriction INTEGER," +
                    "creator VARCHAR(255) NOT NULL," +
                    "FOREIGN KEY (creator) REFERENCES users(username)" +
                    ")";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Saves new media entry
    public int save(MediaEntry media) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "INSERT INTO media_entries (title, description, media_type, release_year, genres, age_restriction, creator) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, media.getTitle());
                stmt.setString(2, media.getDescription());
                stmt.setString(3, media.getMediaType().name());
                stmt.setInt(4, media.getReleaseYear());
                stmt.setString(5, String.join(",", media.getGenres()));  // List -> "Action,Drama"
                stmt.setInt(6, media.getAgeRestriction());
                stmt.setString(7, media.getCreator());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id"); // Returns generated ID
                }
                throw new SQLException("Failed to create media entry");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Finds media entry by ID (empty if not found)
    public Optional<MediaEntry> findById(int id) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM media_entries WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToMedia(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return Optional.empty();
        });
    }

    // Get media by ID (returns null if not found)
    public MediaEntry getMediaById(int id) throws SQLException {
        return findById(id).orElse(null);
    }

    // Gets all media entries from database
    public List<MediaEntry> findAll() {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM media_entries";
            List<MediaEntry> entries = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    entries.add(mapResultSetToMedia(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return entries;
        });
    }

    // Updates existing media entry (creator remains unchanged)
    public void update(MediaEntry media) {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "UPDATE media_entries SET title = ?, description = ?, media_type = ?, " +
                    "release_year = ?, genres = ?, age_restriction = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, media.getTitle());
                stmt.setString(2, media.getDescription());
                stmt.setString(3, media.getMediaType().name());
                stmt.setInt(4, media.getReleaseYear());
                stmt.setString(5, String.join(",", media.getGenres()));
                stmt.setInt(6, media.getAgeRestriction());
                stmt.setInt(7, media.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Deletes media entry from database
    public void delete(int id) {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "DELETE FROM media_entries WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Update average rating for a media entry
    public void updateAverageRating(int mediaId, double avgRating) {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "UPDATE media_entries SET average_rating = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, avgRating);
                stmt.setInt(2, mediaId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Converts database row (ResultSet) to MediaEntry object
    private MediaEntry mapResultSetToMedia(ResultSet rs) throws SQLException {
        MediaEntry media = new MediaEntry();
        media.setId(rs.getInt("id"));
        media.setTitle(rs.getString("title"));
        media.setDescription(rs.getString("description"));
        media.setMediaType(MediaType.valueOf(rs.getString("media_type")));  // String -> Enum
        media.setReleaseYear(rs.getInt("release_year"));
        String genresStr = rs.getString("genres");
        if (genresStr != null && !genresStr.isEmpty()) {
            media.setGenres(Arrays.asList(genresStr.split(",")));
        }
        media.setAgeRestriction(rs.getInt("age_restriction"));
        media.setCreator(rs.getString("creator"));
        return media;
    }
}
