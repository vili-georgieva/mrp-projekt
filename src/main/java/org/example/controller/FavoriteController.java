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
import java.util.HashMap;

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
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            // Extract token from Authorization header
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            String username = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                var userOpt = userService.validateToken(token);

                if (userOpt.isEmpty()) {
                    sendResponse(exchange, 401, createErrorResponse("Invalid or expired token"));
                    return;
                }
                username = userOpt.get().getUsername();
            } else {
                sendResponse(exchange, 401, createErrorResponse("Missing Authorization header"));
                return;
            }

            // Route handling
            if (method.equals("POST") && path.matches("/api/users/[^/]+/favorites/\\d+")) {
                handleAddFavorite(exchange, username, path);
            } else if (method.equals("DELETE") && path.matches("/api/users/[^/]+/favorites/\\d+")) {
                handleRemoveFavorite(exchange, username, path);
            } else if (method.equals("POST") && path.matches("/api/users/[^/]+/favorites/\\d+/toggle")) {
                handleToggleFavorite(exchange, username, path);
            } else if (method.equals("GET") && path.matches("/api/users/[^/]+/favorites")) {
                handleGetFavorites(exchange, username, path);
            } else if (method.equals("GET") && path.matches("/api/users/[^/]+/favorites/check/\\d+")) {
                handleCheckFavorite(exchange, username, path);
            } else {
                sendResponse(exchange, 404, createErrorResponse("Endpoint not found"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, createErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    // POST /api/users/{username}/favorites/{mediaId}
    // Adds a media to favorites
    private void handleAddFavorite(HttpExchange exchange, String authenticatedUser, String path) throws IOException {
        try {
            String[] parts = path.split("/");
            String pathUsername = parts[3];
            int mediaId = Integer.parseInt(parts[5]);

            // Check if user is accessing their own favorites
            if (!authenticatedUser.equals(pathUsername)) {
                sendResponse(exchange, 403, createErrorResponse("You can only manage your own favorites"));
                return;
            }

            favoriteService.addFavorite(authenticatedUser, mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Media added to favorites");

            sendResponse(exchange, 201, objectMapper.writeValueAsString(response));

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 404, createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            sendResponse(exchange, 409, createErrorResponse(e.getMessage()));
        } catch (SQLException e) {
            sendResponse(exchange, 500, createErrorResponse("Database error: " + e.getMessage()));
        }
    }

    // DELETE /api/users/{username}/favorites/{mediaId}
    // Removes a media from favorites
    private void handleRemoveFavorite(HttpExchange exchange, String authenticatedUser, String path) throws IOException {
        try {
            String[] parts = path.split("/");
            String pathUsername = parts[3];
            int mediaId = Integer.parseInt(parts[5]);

            // Check if user is accessing their own favorites
            if (!authenticatedUser.equals(pathUsername)) {
                sendResponse(exchange, 403, createErrorResponse("You can only manage your own favorites"));
                return;
            }

            favoriteService.removeFavorite(authenticatedUser, mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Media removed from favorites");

            sendResponse(exchange, 200, objectMapper.writeValueAsString(response));

        } catch (IllegalStateException e) {
            sendResponse(exchange, 404, createErrorResponse(e.getMessage()));
        } catch (SQLException e) {
            sendResponse(exchange, 500, createErrorResponse("Database error: " + e.getMessage()));
        }
    }

    // POST /api/users/{username}/favorites/{mediaId}/toggle
    // Toggles favorite status
    private void handleToggleFavorite(HttpExchange exchange, String authenticatedUser, String path) throws IOException {
        try {
            String[] parts = path.split("/");
            String pathUsername = parts[3];
            int mediaId = Integer.parseInt(parts[5]);

            // Check if user is accessing their own favorites
            if (!authenticatedUser.equals(pathUsername)) {
                sendResponse(exchange, 403, createErrorResponse("You can only manage your own favorites"));
                return;
            }

            boolean added = favoriteService.toggleFavorite(authenticatedUser, mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favorited", added);
            response.put("message", added ? "Media added to favorites" : "Media removed from favorites");

            sendResponse(exchange, 200, objectMapper.writeValueAsString(response));

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 404, createErrorResponse(e.getMessage()));
        } catch (SQLException e) {
            sendResponse(exchange, 500, createErrorResponse("Database error: " + e.getMessage()));
        }
    }

    // GET /api/users/{username}/favorites
    // Gets all favorites of a user
    private void handleGetFavorites(HttpExchange exchange, String authenticatedUser, String path) throws IOException {
        try {
            String[] parts = path.split("/");
            String pathUsername = parts[3];

            // Check if user is accessing their own favorites
            if (!authenticatedUser.equals(pathUsername)) {
                sendResponse(exchange, 403, createErrorResponse("You can only view your own favorites"));
                return;
            }

            List<MediaEntry> favorites = favoriteService.getFavorites(authenticatedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", favorites.size());
            response.put("favorites", favorites);

            sendResponse(exchange, 200, objectMapper.writeValueAsString(response));

        } catch (SQLException e) {
            sendResponse(exchange, 500, createErrorResponse("Database error: " + e.getMessage()));
        }
    }

    // GET /api/users/{username}/favorites/check/{mediaId}
    // Checks if a media is in favorites
    private void handleCheckFavorite(HttpExchange exchange, String authenticatedUser, String path) throws IOException {
        try {
            String[] parts = path.split("/");
            String pathUsername = parts[3];
            int mediaId = Integer.parseInt(parts[6]);

            // Check if user is accessing their own favorites
            if (!authenticatedUser.equals(pathUsername)) {
                sendResponse(exchange, 403, createErrorResponse("You can only check your own favorites"));
                return;
            }

            boolean isFavorite = favoriteService.isFavorite(authenticatedUser, mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isFavorite", isFavorite);

            sendResponse(exchange, 200, objectMapper.writeValueAsString(response));

        } catch (SQLException e) {
            sendResponse(exchange, 500, createErrorResponse("Database error: " + e.getMessage()));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        try {
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            return "{\"success\":false,\"error\":\"" + message + "\"}";
        }
    }
}
