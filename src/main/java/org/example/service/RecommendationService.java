package org.example.service;

import org.example.repository.UserRepository;

import java.util.List;
import java.util.Map;

// Service für Empfehlungen basierend auf User-Ratings
public class RecommendationService {
    private final UserRepository userRepository;

    public RecommendationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Gibt Empfehlungen basierend auf genre-Ähnlichkeit zu hoch bewerteten Media zurück
    public List<Map<String, Object>> getRecommendations(String username, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        return userRepository.getRecommendations(username, limit);
    }
}
