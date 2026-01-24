package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import org.example.model.Rating;
import org.example.model.User;
import org.example.service.RatingService;
import org.example.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

// Controller for rating operations (create, read, update, delete ratings)
public class RatingController {
    private final RatingService ratingService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public RatingController(RatingService ratingService, UserService userService) {
        this.ratingService = ratingService;
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // Handler for /api/media/{mediaId}/ratings
    public void handleMediaRatings(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        try {
            int mediaId = Integer.parseInt(parts[3]);

            switch (method) {
                case "GET":
                    handleGetRatings(exchange, mediaId);
                    break;
                case "POST":
                    Optional<User> user = authenticateRequest(exchange);
                    if (user.isEmpty()) {
                        sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                        return;
                    }
                    handleCreateRating(exchange, mediaId, user.get());
                    break;
                default:
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid media ID\"}");
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    // Handler for /api/ratings/{ratingId}
    public void handleRating(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        try {
            Optional<User> user = authenticateRequest(exchange);
            if (user.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            int ratingId = Integer.parseInt(parts[3]);

            // Sub-paths: /api/ratings/{id}/comment, /api/ratings/{id}/like, /api/ratings/{id}/confirm
            if (parts.length == 5) {
                String action = parts[4];
                switch (action) {
                    case "comment":
                        if (method.equals("PATCH")) {
                            handleUpdateComment(exchange, ratingId, user.get());
                        } else if (method.equals("DELETE")) {
                            handleDeleteComment(exchange, ratingId, user.get());
                        } else {
                            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                        }
                        return;
                    case "like":
                        if (method.equals("POST")) {
                            handleLikeRating(exchange, ratingId);
                        } else {
                            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                        }
                        return;
                    case "confirm":
                        if (method.equals("POST")) {
                            handleConfirmRating(exchange, ratingId);
                        } else {
                            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                        }
                        return;
                }
            }

            // DELETE /api/ratings/{id}
            if (method.equals("DELETE")) {
                handleDeleteRating(exchange, ratingId, user.get());
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }

        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid rating ID\"}");
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    // Handler for /api/users/{username}/rating-history
    public void handleRatingHistory(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath();
            String username = path.split("/")[3];

            List<Rating> ratings = ratingService.getRatingHistory(username);
            String response = objectMapper.writeValueAsString(ratings);
            sendResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    // GET /api/media/{mediaId}/ratings
    private void handleGetRatings(HttpExchange exchange, int mediaId) throws IOException {
        List<Rating> ratings = ratingService.getRatingsByMediaId(mediaId);
        String response = objectMapper.writeValueAsString(ratings);
        sendResponse(exchange, 200, response);
    }

    // POST /api/media/{mediaId}/ratings
    private void handleCreateRating(HttpExchange exchange, int mediaId, User user) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Rating input = objectMapper.readValue(body, Rating.class);

            Rating rating = ratingService.createOrUpdateRating(mediaId, user.getUsername(), input.getStars(), input.getComment());
            String response = objectMapper.writeValueAsString(rating);
            sendResponse(exchange, 201, response);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // DELETE /api/ratings/{ratingId}
    private void handleDeleteRating(HttpExchange exchange, int ratingId, User user) throws IOException {
        try {
            boolean deleted = ratingService.deleteRating(ratingId, user.getUsername());
            if (deleted) {
                sendResponse(exchange, 200, "{\"message\":\"Rating deleted\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Rating not found\"}");
            }
        } catch (SecurityException e) {
            sendResponse(exchange, 403, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PATCH /api/ratings/{ratingId}/comment
    private void handleUpdateComment(HttpExchange exchange, int ratingId, User user) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Rating input = objectMapper.readValue(body, Rating.class);

            boolean updated = ratingService.updateComment(ratingId, user.getUsername(), input.getComment());
            if (updated) {
                Rating rating = ratingService.getRatingById(ratingId);
                String response = objectMapper.writeValueAsString(rating);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Rating not found\"}");
            }
        } catch (SecurityException e) {
            sendResponse(exchange, 403, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // DELETE /api/ratings/{ratingId}/comment
    private void handleDeleteComment(HttpExchange exchange, int ratingId, User user) throws IOException {
        try {
            boolean deleted = ratingService.deleteComment(ratingId, user.getUsername());
            if (deleted) {
                Rating rating = ratingService.getRatingById(ratingId);
                String response = objectMapper.writeValueAsString(rating);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Rating not found\"}");
            }
        } catch (SecurityException e) {
            sendResponse(exchange, 403, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // POST /api/ratings/{ratingId}/like
    private void handleLikeRating(HttpExchange exchange, int ratingId) throws IOException {
        boolean success = ratingService.likeRating(ratingId);
        if (success) {
            Rating rating = ratingService.getRatingById(ratingId);
            String response = objectMapper.writeValueAsString(rating);
            sendResponse(exchange, 200, response);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Rating not found\"}");
        }
    }

    // POST /api/ratings/{ratingId}/confirm
    private void handleConfirmRating(HttpExchange exchange, int ratingId) throws IOException {
        boolean success = ratingService.confirmRating(ratingId);
        if (success) {
            sendResponse(exchange, 200, "{\"message\":\"Rating confirmed\"}");
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Rating not found\"}");
        }
    }

    // Token validation
    private Optional<User> authenticateRequest(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring(7);
        return userService.validateToken(token);
    }

    // HTTP Response
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
