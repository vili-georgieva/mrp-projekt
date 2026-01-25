package org.example.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.MediaEntry;
import org.example.service.FavoriteService;
import org.example.service.UserService;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class FavoriteController implements HttpHandler {
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    // Constructor mit Dependency Injection
    public FavoriteController(FavoriteService favoriteService, UserService userService) {
        this.favoriteService = favoriteService;
        this.userService = userService;
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
            String[] parts = path.split("/");

            // Path: /api/users/{username}/favorites[/{mediaId}[/toggle]] or /favorites/check/{mediaId}
            if (parts.length < 5 || !isValidFavoritesPath(parts)) {
                sendError(exchange, 400, "Invalid path");
                return;
            }

            String pathUsername = parts[3];
            if (!username.equals(pathUsername)) {
                sendError(exchange, 403, "Access denied");
                return;
            }

            routeRequest(exchange, method, parts, username);
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error");
        }
    }

    private boolean isValidFavoritesPath(String[] parts) {
        return parts[1].equals("api") && parts[2].equals("users") && parts[4].equals("favorites");
    }

    private void routeRequest(HttpExchange exchange, String method, String[] parts, String username) throws IOException {
        int pathLength = parts.length;

        // GET /api/users/{username}/favorites
        if (pathLength == 5 && method.equals("GET")) {
            handleGetFavorites(exchange, username);
            return;
        }

        // /api/users/{username}/favorites/{mediaId}
        if (pathLength == 6) {
            int mediaId = parseMediaId(parts[5]);
            if (mediaId == -1) {
                sendError(exchange, 400, "Invalid media ID");
                return;
            }
            if (method.equals("POST")) handleAddFavorite(exchange, username, mediaId);
            else if (method.equals("DELETE")) handleRemoveFavorite(exchange, username, mediaId);
            else sendError(exchange, 405, "Method not allowed");
            return;
        }

        // /api/users/{username}/favorites/check/{mediaId} oder /{mediaId}/toggle
        if (pathLength == 7) {
            if (parts[5].equals("check") && method.equals("GET")) {
                int mediaId = parseMediaId(parts[6]);
                if (mediaId == -1) {
                    sendError(exchange, 400, "Invalid media ID");
                    return;
                }
                handleCheckFavorite(exchange, username, mediaId);
            } else if (parts[6].equals("toggle") && method.equals("POST")) {
                int mediaId = parseMediaId(parts[5]);
                if (mediaId == -1) {
                    sendError(exchange, 400, "Invalid media ID");
                    return;
                }
                handleToggleFavorite(exchange, username, mediaId);
            } else {
                sendError(exchange, 400, "Invalid path");
            }
            return;
        }

        sendError(exchange, 404, "Endpoint not found");
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
        }
    }

    private void handleRemoveFavorite(HttpExchange exchange, String username, int mediaId) throws IOException {
        try {
            favoriteService.removeFavorite(username, mediaId);
            sendSuccess(exchange, 200, "Removed from favorites");
        } catch (IllegalStateException e) {
            sendError(exchange, 404, e.getMessage());
        }
    }
    // Toggle Favorite: fügt hinzu wenn nicht vorhanden, entfernt wenn vorhanden
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
        }
    }

    private void handleGetFavorites(HttpExchange exchange, String username) throws IOException {
        List<MediaEntry> favorites = favoriteService.getFavorites(username);
        sendJson(exchange, 200, Map.of(
            "success", true,
            "count", favorites.size(),
            "favorites", favorites
        ));
    }

    private void handleCheckFavorite(HttpExchange exchange, String username, int mediaId) throws IOException {
        boolean isFavorite = favoriteService.isFavorite(username, mediaId);
        sendJson(exchange, 200, Map.of("success", true, "isFavorite", isFavorite));
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
