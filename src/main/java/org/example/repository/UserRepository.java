package org.example.repository;

import org.example.model.User;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Data Access Layer für User
public class UserRepository {

    // Erstellt User-Tabelle beim Server-Start (wenn nicht vorhanden)
    public void createTable() {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(255) PRIMARY KEY," +
                    "password_hash VARCHAR(255) NOT NULL," +
                    "token VARCHAR(500)" +
                    ")";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Speichert neuen User in DB
    public void save(User user) {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "INSERT INTO users (username, password_hash, token) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());  // Passwort ist bereits gehasht
                stmt.setString(3, user.getToken());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Sucht User nach Username (gibt Optional.empty() zurück wenn nicht gefunden)
    public Optional<User> findByUsername(String username) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setToken(rs.getString("token"));
                    return Optional.of(user);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return Optional.empty();
        });
    }

    // Sucht User nach Token (für Authentifizierung)
    public Optional<User> findByToken(String token) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE token = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, token);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setToken(rs.getString("token"));
                    return Optional.of(user);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return Optional.empty();
        });
    }

    // Aktualisiert den Session-Token eines Users (nach Login)
    public void updateToken(String username, String token) {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "UPDATE users SET token = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, token);
                stmt.setString(2, username);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Aktualisiert User-Passwort
    public void updatePassword(String username, String hashedPassword) {
        DatabaseConnection.executeInTransaction(conn -> {
            String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, hashedPassword);  // Passwort muss bereits gehasht sein
                stmt.setString(2, username);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    // Lädt alle User aus DB (z.B. für Admin-Zwecke)
    public List<User> findAll() {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT * FROM users";
            List<User> users = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setToken(rs.getString("token"));
                    users.add(user);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return users;
        });
    }

    // Zählt vom User erstellte Media
    public int getMediaCount(String username) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT COUNT(*) FROM media_entries WHERE creator = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 0;
        });
    }

    // Zählt vom User erstellte Ratings
    public int getRatingCount(String username) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT COUNT(*) FROM ratings WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 0;
        });
    }

    // Zählt vom User markierte Favorites
    public int getFavoriteCount(String username) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT COUNT(*) FROM favorites WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 0;
        });
    }

    // Berechnet durchschnittliche Stars vom User vergeben
    public double getAverageStars(String username) {
        return DatabaseConnection.executeInTransaction(conn -> {
            String sql = "SELECT AVG(stars) FROM ratings WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 0.0;
        });
    }

    // Holt Leaderboard (top User nach Rating-Count)
    // left join user und ratings, gruppiert nach user, zählt ratings, sortiert absteigend
    public List<java.util.Map<String, Object>> getLeaderboard(int limit) {
        return DatabaseConnection.executeInTransaction(conn -> {
            // JOIN-Query: User + Anzahl Ratings, sortiert nach Anzahl
            String sql = "SELECT u.username, COUNT(r.id) as rating_count " +
                        "FROM users u " +
                        "LEFT JOIN ratings r ON u.username = r.username " +
                        "GROUP BY u.username " +
                        "ORDER BY rating_count DESC " +
                        "LIMIT ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                List<java.util.Map<String, Object>> leaderboard = new ArrayList<>();
                int rank = 1;
                while (rs.next()) {
                    java.util.Map<String, Object> entry = new java.util.HashMap<>();
                    entry.put("rank", rank++);  // Rang-Position (1, 2, 3, ...)
                    entry.put("username", rs.getString("username"));
                    entry.put("ratingCount", rs.getInt("rating_count"));
                    leaderboard.add(entry);
                }
                return leaderboard;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Holt Empfehlungen für User basierend auf Genres von hoch bewerteten Media
    public List<java.util.Map<String, Object>> getRecommendations(String username, int limit) {
        return DatabaseConnection.executeInTransaction(conn -> {
            // Findet Genres von hoch bewerteten Media des Users (4-5 Sterne)
            // Komplexe Query: Findet Media mit ähnlichen Genres wie die vom User hoch bewerteten
            String sql = "SELECT DISTINCT m.id, m.title, m.media_type, m.genres, m.average_rating " +
                        "FROM media_entries m " +
                        "WHERE m.id NOT IN (SELECT media_id FROM ratings WHERE username = ?) " +  // Noch nicht bewertet
                        "AND EXISTS (" +
                        "  SELECT 1 FROM ratings r " +
                        "  JOIN media_entries m2 ON r.media_id = m2.id " +
                        "  WHERE r.username = ? AND r.stars >= 4 " +  // User hat andere Media >= 4 Sterne bewertet
                        "  AND (" +
                        "    m.genres LIKE '%' || SUBSTRING(m2.genres, 1, POSITION(',' IN m2.genres || ',') - 1) || '%' " +
                        "    OR m2.genres LIKE '%' || SUBSTRING(m.genres, 1, POSITION(',' IN m.genres || ',') - 1) || '%'" +
                        "  )" +
                        ") " +
                        "ORDER BY m.average_rating DESC " +
                        "LIMIT ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, username);
                stmt.setInt(3, limit);
                ResultSet rs = stmt.executeQuery();
                List<java.util.Map<String, Object>> recommendations = new ArrayList<>();
                while (rs.next()) {
                    java.util.Map<String, Object> entry = new java.util.HashMap<>();
                    entry.put("id", rs.getInt("id"));
                    entry.put("title", rs.getString("title"));
                    entry.put("mediaType", rs.getString("media_type"));
                    entry.put("genres", rs.getString("genres"));
                    entry.put("averageRating", rs.getDouble("average_rating"));
                    recommendations.add(entry);
                }
                return recommendations;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

