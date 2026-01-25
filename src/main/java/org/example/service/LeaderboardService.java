package org.example.service;

import org.example.repository.UserRepository;

import java.util.List;
import java.util.Map;

// Service für Leaderboard (aktivste User nach Anzahl Ratings)
public class LeaderboardService {
    private final UserRepository userRepository;

    public LeaderboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Gibt Leaderboard sortiert nach Anzahl Ratings zurück
    public List<Map<String, Object>> getLeaderboard(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        return userRepository.getLeaderboard(limit);
    }
}
