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

    // Erstellt die Tabelle für Media-Einträge beim Server-Start
    public void createTable() {
        DatabaseConnection.executeInTransactionVoid(conn -> {
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
            }
        });
    }

    // Speichert neuen Media-Eintrag
    public int save(MediaEntry media) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "INSERT INTO media_entries (title, description, media_type, release_year, genres, age_restriction, creator) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, media.getTitle());
                stmt.setString(2, media.getDescription());
                stmt.setString(3, media.getMediaType().name());
                stmt.setInt(4, media.getReleaseYear());
                stmt.setString(5, String.join(",", media.getGenres()));  // Liste -> "Action,Drama"
                stmt.setInt(6, media.getAgeRestriction());
                stmt.setString(7, media.getCreator());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");// Gibt die generierte ID zurück
                }
                throw new SQLException("Failed to create media entry");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Sucht Media-Eintrag nach ID (leer wenn nicht gefunden)
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

    // Holt alle Media-Einträge aus der Datenbank
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

    // Aktualisiert existierenden Media-Eintrag (creator bleibt unverändert)
    public void update(MediaEntry media) {
        DatabaseConnection.executeInTransactionVoid(conn -> {
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
            }
        });
    }

    // Löscht Media-Eintrag aus der DB
    public void delete(int id) {
        DatabaseConnection.executeInTransactionVoid(conn -> {
            String sql = "DELETE FROM media_entries WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
        });
    }

    /**
     * Update average rating for a media entry
     */
    public void updateAverageRating(int mediaId, double avgRating) {
        DatabaseConnection.executeInTransactionVoid(conn -> {
            String sql = "UPDATE media_entries SET average_rating = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, avgRating);
                stmt.setInt(2, mediaId);
                stmt.executeUpdate();
            }
        });
    }

    //Konvertiert Datenbank-Zeile (ResultSet) zu MediaEntry-Objekt
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
