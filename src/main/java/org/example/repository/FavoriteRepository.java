package org.example.repository;

import org.example.model.MediaEntry;
import org.example.model.MediaType;
import org.example.util.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Data Access Layer für Favorites
public class FavoriteRepository {

    // Erstellt Favorites-Tabelle beim Server-Start
    public void createTable() {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "CREATE TABLE IF NOT EXISTS favorites (" +
                    "username VARCHAR(255) NOT NULL," +
                    "media_id INTEGER NOT NULL," +
                    "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +  // Zeitpunkt wann favorisiert
                    "PRIMARY KEY (username, media_id)," +  // Composite Primary Key (beide zusammen eindeutig)
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

    // Fügt ein Media zu den Favorites eines Users hinzu
    public boolean addFavorite(String username, int mediaId) {
        return DatabaseConnection.executeInTransaction(conn -> {
            // ON CONFLICT DO NOTHING: Ignoriert Fehler wenn schon favorisiert (statt Exception)
            String sql = "INSERT INTO favorites (username, media_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setInt(2, mediaId);
                return stmt.executeUpdate() > 0;  // true wenn Zeile eingefügt wurde
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Entfernt ein Media aus den Favorites eines Users
    public boolean removeFavorite(String username, int mediaId) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "DELETE FROM favorites WHERE username = ? AND media_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setInt(2, mediaId);
                return stmt.executeUpdate() > 0;  // true wenn Zeile gelöscht wurde
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Prüft ob ein Media in den Favorites eines Users ist
    public boolean isFavorite(String username, int mediaId) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT COUNT(*) FROM favorites WHERE username = ? AND media_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setInt(2, mediaId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;  // true wenn mindestens 1 Zeile gefunden
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return false;
        });
    }

    // Holt alle Favorites eines Users (gibt vollständige MediaEntry Objects zurück)
    public List<MediaEntry> getFavoritesByUser(String username) {
        return DatabaseConnection.executeInTransaction(conn -> {
            // INNER JOIN: Verbindet favorites mit media_entries Tabelle
            String sql = "SELECT m.* FROM media_entries m " +
                        "INNER JOIN favorites f ON m.id = f.media_id " +
                        "WHERE f.username = ? " +
                        "ORDER BY f.added_at DESC";  // Neueste zuerst

            List<MediaEntry> favorites = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return favorites;
        });
    }

    // Holt alle Favorite Media-IDs eines Users
    public List<Integer> getFavoriteIdsByUser(String username) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT media_id FROM favorites WHERE username = ? ORDER BY added_at DESC";

            List<Integer> favoriteIds = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        favoriteIds.add(rs.getInt("media_id"));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return favoriteIds;
        });
    }

    // Holt die Anzahl der User die ein spezifisches Media favorisiert haben
    public int getFavoriteCount(int mediaId) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT COUNT(*) FROM favorites WHERE media_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, mediaId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 0;
        });
    }
}
