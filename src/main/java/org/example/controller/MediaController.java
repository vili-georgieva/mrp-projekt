package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.example.model.MediaEntry;
import org.example.model.User;
import org.example.service.MediaService;
import org.example.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

// Controller for media management (CRUD operations for movies, series, games)
public class MediaController {
    private final MediaService mediaService;
    private final UserService userService;
    private final ObjectMapper objectMapper;  // Jackson: JSON <-> Java object conversion

    public MediaController(MediaService mediaService, UserService userService) {
        this.mediaService = mediaService;
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }
    // Main handler for /api/media endpoint
    // exchange = HTTP request object (automatically passed by HttpServer)
    public void handleMedia(HttpExchange exchange) throws IOException {
        Optional<User> user = authenticateRequest(exchange);  // Check token (optional for GET)

        String method = exchange.getRequestMethod(); // GET, POST, PUT, DELETE
        String path = exchange.getRequestURI().getPath(); // /api/media or /api/media/{id}

        try {
            switch (method) {
                case "GET":
                    handleGetMedia(exchange, path);  // No token required
                    break;
                case "POST":
                    if (user.isEmpty()) {
                        sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                        return;
                    }
                    handleCreateMedia(exchange, user.get());
                    break;
                case "PUT":
                    if (user.isEmpty()) {
                        sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                        return;
                    }
                    handleUpdateMedia(exchange, path, user.get());
                    break;
                case "DELETE":
                    if (user.isEmpty()) {
                        sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                        return;
                    }
                    handleDeleteMedia(exchange, path, user.get());
                    break;
                default:
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    // Get all or specific media
    private void handleGetMedia(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");

        if (parts.length == 3) {
            // GET /api/media - get all media
            List<MediaEntry> media = mediaService.getAllMedia();
            String response = objectMapper.writeValueAsString(media);  // List -> JSON
            sendResponse(exchange, 200, response);
        } else if (parts.length == 4) {
            // GET /api/media/{id} - get specific media
            try {
                int id = Integer.parseInt(parts[3]);  // Extract ID from URL
                Optional<MediaEntry> media = mediaService.getMediaById(id);
                if (media.isPresent()) {
                    String response = objectMapper.writeValueAsString(media.get());
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Media not found\"}");  // 404 = Not Found
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid media ID\"}");  // ID is not a number
            }
        } else {
            sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
        }
    }

    // POST /api/media - Creates new media entry
    private void handleCreateMedia(HttpExchange exchange, User user) throws IOException {
        try {
            // Read JSON from request body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            MediaEntry media = objectMapper.readValue(body, MediaEntry.class);  // JSON -> MediaEntry

            MediaEntry created = mediaService.createMedia(media, user);  // Save to DB
            String response = objectMapper.writeValueAsString(created);  // MediaEntry -> JSON
            sendResponse(exchange, 201, response);  // 201 = Created
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PUT /api/media/{id} - Updates media entry (only creator can update)
    private void handleUpdateMedia(HttpExchange exchange, String path, User user) throws IOException {
        String[] parts = path.split("/");
        if (parts.length != 4) {  // Must be /api/media/{id}
            sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            return;
        }

        try {
            int id = Integer.parseInt(parts[3]);  // Extract ID from URL
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            MediaEntry media = objectMapper.readValue(body, MediaEntry.class);
            MediaEntry updated = mediaService.updateMedia(id, media, user);
            String response = objectMapper.writeValueAsString(updated);
            sendResponse(exchange, 200, response);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid media ID\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 403, "{\"error\":\"" + e.getMessage() + "\"}");  // 403 = Forbidden
        }
    }

    // DELETE /api/media/{id} (only creator can delete)
    private void handleDeleteMedia(HttpExchange exchange, String path, User user) throws IOException {
        String[] parts = path.split("/");
        if (parts.length != 4) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            return;
        }

        try {
            int id = Integer.parseInt(parts[3]);
            mediaService.deleteMedia(id, user);  // Service checks if user = creator
            sendResponse(exchange, 204, "");  // 204 = No Content (successfully deleted)
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid media ID\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 403, "{\"error\":\"" + e.getMessage() + "\"}");  // Not authorized
        }
    }

    // Authenticates request via token in Authorization header
    private Optional<User> authenticateRequest(HttpExchange exchange) {
        try {
            // Read Authorization header: "Bearer {token}"
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            System.out.println("Authentication header: " + authHeader);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Optional.empty();  // No token present
            }

            String token = authHeader.substring(7);  // Remove "Bearer ", get only token
            return userService.validateToken(token);  // Check token in DB
        } catch (RuntimeException e) {
            return Optional.empty();  // Not authenticated
        }
    }

    // Sends HTTP response with JSON content
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");  // Set JSON header
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);  // Status + Content-Length
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);  // Write response body
        }
    }
}

