package org.example.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.MediaEntry;
import org.example.repository.UserRepository;
import org.example.service.FavoriteService;
import org.example.service.UserService;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class FavoriteController implements HttpHandler {
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public FavoriteController() {
        this.favoriteService = new FavoriteService();
        this.userService = new UserService(new UserRepository());
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String username = getAuthenticatedUser(exchange);
            if (username == null) return;

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/"); // Split: ["", "api", "users", "{username}", "favorites", ...]

            // Validate path structure: /api/users/{username}/favorites
            if (parts.length < 5 || !parts[1].equals("api") || !parts[2].equals("users") || !parts[4].equals("favorites")) {
                sendError(exchange, 400, "Invalid path");
                return;
            }

            // Check access
            String pathUsername = parts[3];
            if (!username.equals(pathUsername)) {
                sendError(exchange, 403, "Access denied");
                return;
            }

            // Route based on path length and method
            if (parts.length == 5) {
                // /api/users/{username}/favorites
                if (method.equals("GET")) {
                    handleGetFavorites(exchange, username);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            } else if (parts.length == 6) {
                // /api/users/{username}/favorites/{mediaId}
                int mediaId = parseMediaId(parts[5]);
                if (mediaId == -1) {
                    sendError(exchange, 400, "Invalid media ID");
                    return;
                }

                if (method.equals("POST")) {
                    handleAddFavorite(exchange, username, mediaId);
                } else if (method.equals("DELETE")) {
                    handleRemoveFavorite(exchange, username, mediaId);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            } else if (parts.length == 7) {
                // /api/users/{username}/favorites/{...}/{...}
                if (parts[5].equals("check")) {
                    // /api/users/{username}/favorites/check/{mediaId}
                    int mediaId = parseMediaId(parts[6]);
                    if (mediaId == -1) {
                        sendError(exchange, 400, "Invalid media ID");
                        return;
                    }
                    if (method.equals("GET")) {
                        handleCheckFavorite(exchange, username, mediaId);
                    } else {
                        sendError(exchange, 405, "Method not allowed");
                    }
                } else {
                    // /api/users/{username}/favorites/{mediaId}/toggle
                    int mediaId = parseMediaId(parts[5]);
                    if (mediaId == -1 || !parts[6].equals("toggle")) {
                        sendError(exchange, 400, "Invalid path");
                        return;
                    }
                    if (method.equals("POST")) {
                        handleToggleFavorite(exchange, username, mediaId);
                    } else {
                        sendError(exchange, 405, "Method not allowed");
                    }
                }
            } else {
                sendError(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error");
        }
    }

    private int parseMediaId(String idString) {
        try {
            return Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String getAuthenticatedUser(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(exchange, 401, "Missing Authorization header");
            return null;
        }
        var userOpt = userService.validateToken(authHeader.substring(7));
        if (userOpt.isEmpty()) {
            sendError(exchange, 401, "Invalid token");
            return null;
        }
        return userOpt.get().getUsername();
    }

    private void handleAddFavorite(HttpExchange exchange, String username, int mediaId) throws IOException {
        try {
            favoriteService.addFavorite(username, mediaId);
            sendSuccess(exchange, 201, "Added to favorites");
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(exchange, 404, e.getMessage());
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error");
        }
    }

    private void handleRemoveFavorite(HttpExchange exchange, String username, int mediaId) throws IOException {
        try {
            favoriteService.removeFavorite(username, mediaId);
            sendSuccess(exchange, 200, "Removed from favorites");
        } catch (IllegalStateException e) {
            sendError(exchange, 404, e.getMessage());
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error");
        }
    }

    private void handleToggleFavorite(HttpExchange exchange, String username, int mediaId) throws IOException {
        try {
            boolean added = favoriteService.toggleFavorite(username, mediaId);
            sendJson(exchange, 200, Map.of(
                "success", true,
                "favorited", added,
                "message", added ? "Added" : "Removed"
            ));
        } catch (IllegalArgumentException e) {
            sendError(exchange, 404, e.getMessage());
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error");
        }
    }

    private void handleGetFavorites(HttpExchange exchange, String username) throws IOException {
        try {
            List<MediaEntry> favorites = favoriteService.getFavorites(username);
            sendJson(exchange, 200, Map.of(
                "success", true, 
                "count", favorites.size(), 
                "favorites", favorites
            ));
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error");
        }
    }

    private void handleCheckFavorite(HttpExchange exchange, String username, int mediaId) throws IOException {
        try {
            boolean isFavorite = favoriteService.isFavorite(username, mediaId);
            sendJson(exchange, 200, Map.of("success", true, "isFavorite", isFavorite));
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error");
        }
    }
    private void sendSuccess(HttpExchange exchange, int code, String message) throws IOException {
        sendJson(exchange, code, Map.of("success", true, "message", message));
    }
    private void sendError(HttpExchange exchange, int code, String error) throws IOException {
        sendJson(exchange, code, Map.of("success", false, "error", error));
    }
    private void sendJson(HttpExchange exchange, int code, Map<String, Object> data) throws IOException {
        byte[] bytes = objectMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
