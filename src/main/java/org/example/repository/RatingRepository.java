package org.example.repository;

import org.example.model.Rating;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Data Access Layer für Ratings
// Verwaltet CRUD-Operationen für ratings Tabelle
public class RatingRepository {

    // Erstellt Ratings-Tabelle beim Server-Start
    public void createTable() {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "CREATE TABLE IF NOT EXISTS ratings (" +
                    "id SERIAL PRIMARY KEY," +
                    "media_id INTEGER NOT NULL," +
                    "username VARCHAR(255) NOT NULL," +
                    "stars INTEGER NOT NULL CHECK (stars >= 1 AND stars <= 5)," +  // CHECK constraint: Nur 1-5 Sterne erlaubt
                    "comment TEXT," +
                    "confirmed BOOLEAN DEFAULT false," +  // Für Moderation (nur confirmed Ratings zählen für Durchschnitt)
                    "likes INTEGER DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +  // Auto-Zeitstempel bei Erstellung
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE(media_id, username)," +  // Ein User kann pro Media nur ein Rating haben
                    "FOREIGN KEY (media_id) REFERENCES media_entries(id) ON DELETE CASCADE," +  // CASCADE: Löscht Ratings wenn Media gelöscht wird
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

    // Erstellt neues Rating oder aktualisiert bestehendes
    public Rating createRating(Rating rating) {
        return DatabaseConnection.executeInTransaction(conn -> {
            // ON CONFLICT: Wenn Rating schon existiert (UNIQUE constraint), dann UPDATE statt INSERT
            String sql = "INSERT INTO ratings (media_id, username, stars, comment, confirmed, likes) " +
                         "VALUES (?, ?, ?, ?, ?, ?) " +
                         "ON CONFLICT (media_id, username) DO UPDATE SET " +  // Upsert: Insert or Update
                         "stars = EXCLUDED.stars, comment = EXCLUDED.comment, updated_at = CURRENT_TIMESTAMP " +
                         "RETURNING id, created_at, updated_at";  // Gibt generierte Werte zurück

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, rating.getMediaId());
                pstmt.setString(2, rating.getUsername());
                pstmt.setInt(3, rating.getStars());
                pstmt.setString(4, rating.getComment());
                pstmt.setBoolean(5, rating.isConfirmed());
                pstmt.setInt(6, rating.getLikes());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        rating.setId(rs.getInt("id"));
                        rating.setTimestamp(rs.getTimestamp("created_at").toLocalDateTime());  // Timestamp -> LocalDateTime
                    }
                }
                return rating;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Aktualisiert ein bestehendes Rating
    public boolean updateRating(int ratingId, int stars, String comment) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "UPDATE ratings SET stars = ?, comment = ?, updated_at = CURRENT_TIMESTAMP " +
                         "WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, stars);
                pstmt.setString(2, comment);
                pstmt.setInt(3, ratingId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Aktualisiert nur den Kommentar eines Ratings
    public boolean updateComment(int ratingId, String comment) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "UPDATE ratings SET comment = ?, updated_at = CURRENT_TIMESTAMP, confirmed = false " +
                         "WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, comment);
                pstmt.setInt(2, ratingId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Löscht Rating nach ID
    public boolean deleteRating(int ratingId) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "DELETE FROM ratings WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, ratingId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Holt Rating nach ID
    public Rating getRatingById(int id) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM ratings WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToRating(rs);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Holt alle Ratings für ein spezifisches Media
    public List<Rating> getRatingsByMediaId(int mediaId) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM ratings WHERE media_id = ? ORDER BY created_at DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, mediaId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    List<Rating> ratings = new ArrayList<>();
                    while (rs.next()) {
                        ratings.add(mapResultSetToRating(rs));
                    }
                    return ratings;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Holt bestätigte Ratings für ein spezifisches Media
    public List<Rating> getConfirmedRatingsByMediaId(int mediaId) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM ratings WHERE media_id = ? AND confirmed = true ORDER BY created_at DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, mediaId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    List<Rating> ratings = new ArrayList<>();
                    while (rs.next()) {
                        ratings.add(mapResultSetToRating(rs));
                    }
                    return ratings;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Holt Rating nach Media und User (prüft ob existiert)
    public Rating getRatingByMediaAndUser(int mediaId, String username) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM ratings WHERE media_id = ? AND username = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, mediaId);
                pstmt.setString(2, username);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToRating(rs);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Holt alle Ratings eines Users
    public List<Rating> getRatingsByUser(String username) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM ratings WHERE username = ? ORDER BY created_at DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);

                try (ResultSet rs = pstmt.executeQuery()) {
                    List<Rating> ratings = new ArrayList<>();
                    while (rs.next()) {
                        ratings.add(mapResultSetToRating(rs));
                    }
                    return ratings;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Erhöht Likes für ein Rating
    public boolean incrementLikes(int ratingId) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "UPDATE ratings SET likes = likes + 1 WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, ratingId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Bestätigt ein Rating (Moderation)
    public boolean confirmRating(int ratingId) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "UPDATE ratings SET confirmed = true WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, ratingId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Berechnet durchschnittliche Bewertung für ein Media
    public double getAverageRating(int mediaId) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT AVG(stars) as avg_rating FROM ratings WHERE media_id = ? AND confirmed = true";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, mediaId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("avg_rating");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 0.0;
        });
    }

    // Helper-Methode zum Konvertieren von ResultSet zu Rating Object
    // Konvertiert DB-Zeile (ResultSet) zu Rating Java-Object
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
            rating.setTimestamp(created.toLocalDateTime());  // SQL Timestamp -> Java LocalDateTime
        }

        return rating;
    }
}
