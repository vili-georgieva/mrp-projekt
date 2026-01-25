package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;

import java.util.HexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

// Business Logic Layer für User-Management
// Enthält Authentifizierung, Registrierung, Statistiken
public class UserService {
    private final UserRepository userRepository;

    // Constructor mit Dependency Injection (für Tests)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Registriert neuen User mit Username und Passwort
    // Passwort wird gehasht gespeichert
    public User register(String username, String password) {
        // Prüft ob User bereits existiert
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Validierung: Username und Passwort dürfen nicht leer sein
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Hasht Passwort für bessere Sicherheit
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword);
        userRepository.save(user);
        return user;
    }

    // Login: prüft Username/Passwort und gibt sicheren Token zurück
    public String login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        User user = userOpt.get();
        String hashedPassword = hashPassword(password);
        // Vergleicht gehashtes Passwort mit gespeichertem Hash
        if (!user.getPassword().equals(hashedPassword)) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Generiert sicheren Token mit UUID
        String token = generateSecureToken();
        userRepository.updateToken(username, token);  // Speichert Token in DB
        return token;
    }

    // Prüft ob Token gültig ist und gibt zugehörigen User zurück
    public Optional<User> validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByToken(token);
    }

    // Lädt User-Daten nach Username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Aktualisiert User-Passwort
    public void updatePassword(String username, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        String hashedPassword = hashPassword(newPassword);
        userRepository.updatePassword(username, hashedPassword);
    }

    // Generiert einen sicheren Token mit UUID
    // UUID = Universal Unique Identifier (garantiert eindeutig)
    private String generateSecureToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    // Hasht Passwort mit SHA-256
    // SHA-256 = Kryptographischer Hash-Algorithmus (nicht umkehrbar)
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);  // Bytes -> Hexadezimal String
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // Gibt User-Statistiken zurück (Media-Count, Rating-Count, Favorites, durchschnittliche Stars)
    public Map<String, Object> getUserStatistics(String username) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("mediaCount", userRepository.getMediaCount(username));
        stats.put("ratingCount", userRepository.getRatingCount(username));
        stats.put("favoriteCount", userRepository.getFavoriteCount(username));
        // Rundet auf 2 Dezimalstellen
        stats.put("averageStars", Math.round(userRepository.getAverageStars(username) * 100.0) / 100.0);
        return stats;
    }

    // Gibt Leaderboard zurück (top User nach Rating-Count)
    public List<Map<String, Object>> getLeaderboard(int limit) {
        return userRepository.getLeaderboard(limit);
    }

    // Gibt Empfehlungen für User basierend auf Genre-Ähnlichkeit zurück
    public List<Map<String, Object>> getRecommendations(String username, int limit) {
        return userRepository.getRecommendations(username, limit);
    }
}

