package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.User;
import org.example.service.RecommendationService;
import org.example.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Controller für Empfehlungen
public class RecommendationController implements HttpHandler {
    private final RecommendationService recommendationService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public RecommendationController(RecommendationService recommendationService, UserService userService) {
        this.recommendationService = recommendationService;
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

        handleGetRecommendations(exchange);
    }

    // GET /api/recommendations?username={username}&limit={limit}
    private void handleGetRecommendations(HttpExchange exchange) throws IOException {
        try {
            // Authentifizierung prüfen
            Optional<User> userOpt = authenticateRequest(exchange);
            if (userOpt.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String username = parseQueryParam(query, "username");
            int limit = parseLimitParam(query, 10);

            if (username == null || username.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Username parameter required\"}");
                return;
            }

            List<Map<String, Object>> recommendations = recommendationService.getRecommendations(username, limit);
            String response = objectMapper.writeValueAsString(recommendations);
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

    // Parst Query Parameter
    private String parseQueryParam(String query, String paramName) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith(paramName + "=")) {
                return param.substring(paramName.length() + 1);
            }
        }
        return null;
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
