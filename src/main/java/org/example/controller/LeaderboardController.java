package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.User;
import org.example.service.LeaderboardService;
import org.example.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Controller für Leaderboard (public leaderboard der aktivsten User)
public class LeaderboardController implements HttpHandler {
    private final LeaderboardService leaderboardService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public LeaderboardController(LeaderboardService leaderboardService, UserService userService) {
        this.leaderboardService = leaderboardService;
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (!"GET".equals(method)) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        handleGetLeaderboard(exchange);
    }

    // GET /api/leaderboard?limit=10
    private void handleGetLeaderboard(HttpExchange exchange) throws IOException {
        // Authentifizierung prüfen
        Optional<User> user = authenticateRequest(exchange);
        if (user.isEmpty()) {
            sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        try {
            String query = exchange.getRequestURI().getQuery();
            int limit = parseLimitParam(query, 10);

            List<Map<String, Object>> leaderboard = leaderboardService.getLeaderboard(limit);
            String response = objectMapper.writeValueAsString(leaderboard);
            sendResponse(exchange, 200, response);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    // Authentifizierung via Bearer Token
    private Optional<User> authenticateRequest(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring(7);
        return userService.validateToken(token);
    }

    // Parst limit Parameter
    private int parseLimitParam(String query, int defaultValue) {
        if (query == null) return defaultValue;
        for (String param : query.split("&")) {
            if (param.startsWith("limit=")) {
                try {
                    return Integer.parseInt(param.substring(6));
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    // Sendet HTTP Response
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
