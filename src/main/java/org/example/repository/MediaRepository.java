package org.example.repository;

import org.example.model.MediaEntry;
import org.example.model.MediaType;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// Data Access Layer für Media-Einträge
// Verwaltet CRUD-Operationen für media_entries Tabelle
public class MediaRepository {

    // Erstellt Tabelle für Media-Einträge
    public void createTable() {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "CREATE TABLE IF NOT EXISTS media_entries (" +
                    "id SERIAL PRIMARY KEY," +  // SERIAL = Auto-Increment ID
                    "title VARCHAR(255) NOT NULL," +
                    "description TEXT," +
                    "media_type VARCHAR(50) NOT NULL," +
                    "release_year INTEGER," +
                    "genres TEXT," +  // Gespeichert als Komma-separierte Liste
                    "age_restriction INTEGER," +
                    "creator VARCHAR(255) NOT NULL," +
                    "FOREIGN KEY (creator) REFERENCES users(username)" +  // Foreign Key Constraint
                    ")";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Speichert neuen Media-Eintrag
    public int save(MediaEntry media) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "INSERT INTO media_entries (title, description, media_type, release_year, genres, age_restriction, creator) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";  // RETURNING id gibt die generierte ID zurück
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, media.getTitle());
                stmt.setString(2, media.getDescription());
                stmt.setString(3, media.getMediaType().name());  // Enum -> String
                stmt.setInt(4, media.getReleaseYear());
                stmt.setString(5, String.join(",", media.getGenres()));  // List -> "Action,Drama"
                stmt.setInt(6, media.getAgeRestriction());
                stmt.setString(7, media.getCreator());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id"); // Gibt generierte ID zurück
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
                    return Optional.of(mapResultSetToMedia(rs));  // Konvertiert DB-Zeile zu Java Object
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return Optional.empty();
        });
    }

    // Holt Media nach ID (gibt null zurück wenn nicht gefunden)
    public MediaEntry getMediaById(int id) {
        return findById(id).orElse(null);
    }

    // Lädt alle Media-Einträge aus Datenbank
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

    // Aktualisiert bestehenden Media-Eintrag (Creator bleibt unverändert)
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

    // Löscht Media-Eintrag aus Datenbank
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

    // Aktualisiert durchschnittliche Bewertung für Media-Eintrag
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

    // Konvertiert Datenbank-Zeile (ResultSet) zu MediaEntry Object
    // Helper-Methode um DB-Zeile in Java-Object zu konvertieren
    private MediaEntry mapResultSetToMedia(ResultSet rs) throws SQLException {
        MediaEntry media = new MediaEntry();
        media.setId(rs.getInt("id"));
        media.setTitle(rs.getString("title"));
        media.setDescription(rs.getString("description"));
        media.setMediaType(MediaType.valueOf(rs.getString("media_type")));  // String -> Enum
        media.setReleaseYear(rs.getInt("release_year"));
        String genresStr = rs.getString("genres");
        if (genresStr != null && !genresStr.isEmpty()) {
            media.setGenres(Arrays.asList(genresStr.split(",")));  // "Action,Drama" -> ["Action", "Drama"]
        }
        media.setAgeRestriction(rs.getInt("age_restriction"));
        media.setCreator(rs.getString("creator"));
        return media;
    }

    // Sucht Media mit optionalen Filtern (null = Filter ignorieren)
    public List<MediaEntry> searchMedia(String title, String genre, MediaType mediaType,
                                        Integer minRating, Integer ageRestriction) {
        return DatabaseConnection.executeInTransaction(conn -> {
            // Dynamischer SQL-Query: Fügt WHERE-Bedingungen nur hinzu wenn Filter gesetzt
            StringBuilder sql = new StringBuilder("SELECT * FROM media_entries WHERE 1=1");
            List<Object> params = new ArrayList<>();

            // Filtert nach Titel (case-insensitive Teilstring-Match)
            if (title != null && !title.trim().isEmpty()) {
                sql.append(" AND LOWER(title) LIKE LOWER(?)");  // LIKE für Teilstring-Suche
                params.add("%" + title + "%");  // % = Wildcard (beliebige Zeichen)
            }

            // Filtert nach Genre (Teilstring-Match in komma-separierter Liste)
            if (genre != null && !genre.trim().isEmpty()) {
                sql.append(" AND LOWER(genres) LIKE LOWER(?)");
                params.add("%" + genre + "%");
            }

            // Filtert nach Media-Typ
            if (mediaType != null) {
                sql.append(" AND media_type = ?");
                params.add(mediaType.name());
            }

            // Filtert nach Altersfreigabe (maximales Alter)
            if (ageRestriction != null) {
                sql.append(" AND age_restriction <= ?");  // <= um alle bis zur Altersgrenze zu finden
                params.add(ageRestriction);
            }

            sql.append(" ORDER BY title");  // Sortiert alphabetisch nach Titel

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                // Setzt alle Parameter in PreparedStatement ein
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));  // i+1 weil PreparedStatement bei 1 beginnt
                }

                ResultSet rs = stmt.executeQuery();
                List<MediaEntry> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapResultSetToMedia(rs));
                }
                return results;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
