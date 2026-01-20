package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import org.example.model.Rating;
import org.example.repository.UserRepository;
import org.example.service.RatingService;
import org.example.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public RatingController() {
        this.ratingService = new RatingService();
        this.userService = new UserService(new UserRepository());
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Handle all rating-related requests
     * Routes:
     * POST /api/media/{mediaId}/ratings - Create/Update rating
     * GET /api/media/{mediaId}/ratings - Get all ratings for media
     * DELETE /api/ratings/{ratingId} - Delete rating
     * PATCH /api/ratings/{ratingId}/comment - Update only comment
     * DELETE /api/ratings/{ratingId}/comment - Delete only comment
     * POST /api/ratings/{ratingId}/like - Like a rating
     * POST /api/ratings/{ratingId}/confirm - Confirm rating (moderation)
     */
    public void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            // POST /api/media/{mediaId}/ratings - Create/Update rating
            if (method.equals("POST") && path.matches("/api/media/\\d+/ratings")) {
                handleCreateOrUpdateRating(exchange);
            }
            // GET /api/media/{mediaId}/ratings - Get ratings for media
            else if (method.equals("GET") && path.matches("/api/media/\\d+/ratings")) {
                handleGetRatingsByMedia(exchange);
            }
            // DELETE /api/ratings/{ratingId} - Delete rating
            else if (method.equals("DELETE") && path.matches("/api/ratings/\\d+$")) {
                handleDeleteRating(exchange);
            }
            // PATCH /api/ratings/{ratingId}/comment - Update only comment
            else if (method.equals("PATCH") && path.matches("/api/ratings/\\d+/comment")) {
                handleUpdateComment(exchange);
            }
            // DELETE /api/ratings/{ratingId}/comment - Delete only comment
            else if (method.equals("DELETE") && path.matches("/api/ratings/\\d+/comment")) {
                handleDeleteComment(exchange);
            }
            // POST /api/ratings/{ratingId}/like - Like rating
            else if (method.equals("POST") && path.matches("/api/ratings/\\d+/like")) {
                handleLikeRating(exchange);
            }
            // POST /api/ratings/{ratingId}/confirm - Confirm rating
            else if (method.equals("POST") && path.matches("/api/ratings/\\d+/confirm")) {
                handleConfirmRating(exchange);
            }
            // GET /api/users/{username}/rating-history - Get user's rating history
            else if (method.equals("GET") && path.matches("/api/users/[^/]+/rating-history")) {
                handleGetRatingHistory(exchange);
            }
            else {
                sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * POST /api/media/{mediaId}/ratings
     * Create or update a rating
     */
    private void handleCreateOrUpdateRating(HttpExchange exchange) throws IOException {
        try {
            // Extract token and validate user
            String token = extractToken(exchange);
            if (token == null) {
                sendResponse(exchange, 401, "{\"error\": \"Missing Authorization header\"}");
                return;
            }

            var userOpt = userService.validateToken(token);
            if (userOpt.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }
            String username = userOpt.get().getUsername();

            // Extract mediaId from path
            String path = exchange.getRequestURI().getPath();
            int mediaId = Integer.parseInt(path.split("/")[3]);

            // Parse request body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode json = objectMapper.readTree(body);

            int stars = json.get("stars").asInt();
            String comment = json.has("comment") ? json.get("comment").asText() : "";

            // Create or update rating
            Rating rating = ratingService.createOrUpdateRating(mediaId, username, stars, comment);

            String response = objectMapper.writeValueAsString(rating);
            sendResponse(exchange, 201, response);

        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"error\": \"Database error: " + e.getMessage() + "\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /api/media/{mediaId}/ratings
     * Get all ratings for a media (with optional filter for confirmed only)
     */
    private void handleGetRatingsByMedia(HttpExchange exchange) throws IOException {
        try {
            // Extract mediaId from path
            String path = exchange.getRequestURI().getPath();
            int mediaId = Integer.parseInt(path.split("/")[3]);

            // Check query parameter for confirmed filter
            String query = exchange.getRequestURI().getQuery();
            boolean confirmedOnly = query != null && query.contains("confirmed=true");

            List<Rating> ratings;
            if (confirmedOnly) {
                ratings = ratingService.getConfirmedRatingsByMediaId(mediaId);
            } else {
                ratings = ratingService.getRatingsByMediaId(mediaId);
            }

            String response = objectMapper.writeValueAsString(ratings);
            sendResponse(exchange, 200, response);

        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * DELETE /api/ratings/{ratingId}
     * Delete a rating (only owner can delete)
     */
    private void handleDeleteRating(HttpExchange exchange) throws IOException {
        try {
            // Extract token and validate user
            String token = extractToken(exchange);
            if (token == null) {
                sendResponse(exchange, 401, "{\"error\": \"Missing Authorization header\"}");
                return;
            }

            var userOpt = userService.validateToken(token);
            if (userOpt.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }
            String username = userOpt.get().getUsername();

            // Extract ratingId from path
            String path = exchange.getRequestURI().getPath();
            int ratingId = Integer.parseInt(path.split("/")[3]);

            // Delete rating
            boolean deleted = ratingService.deleteRating(ratingId, username);

            if (deleted) {
                sendResponse(exchange, 200, "{\"message\": \"Rating deleted successfully\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Rating not found\"}");
            }

        } catch (SecurityException e) {
            sendResponse(exchange, 403, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * PATCH /api/ratings/{ratingId}/comment
     * Update only the comment of a rating (owner only)
     */
    private void handleUpdateComment(HttpExchange exchange) throws IOException {
        try {
            // Extract token and validate user
            String token = extractToken(exchange);
            if (token == null) {
                sendResponse(exchange, 401, "{\"error\": \"Missing Authorization header\"}");
                return;
            }

            var userOpt = userService.validateToken(token);
            if (userOpt.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }
            String username = userOpt.get().getUsername();

            // Extract ratingId from path
            String path = exchange.getRequestURI().getPath();
            int ratingId = Integer.parseInt(path.split("/")[3]);

            // Parse request body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode json = objectMapper.readTree(body);

            String newComment = json.get("comment").asText();

            // Update comment
            boolean updated = ratingService.updateComment(ratingId, username, newComment);

            if (updated) {
                Rating rating = ratingService.getRatingById(ratingId);
                String response = objectMapper.writeValueAsString(rating);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Rating not found\"}");
            }

        } catch (SecurityException e) {
            sendResponse(exchange, 403, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * DELETE /api/ratings/{ratingId}/comment
     * Delete only the comment of a rating (keeps the stars)
     */
    private void handleDeleteComment(HttpExchange exchange) throws IOException {
        try {
            // Extract token and validate user
            String token = extractToken(exchange);
            if (token == null) {
                sendResponse(exchange, 401, "{\"error\": \"Missing Authorization header\"}");
                return;
            }

            var userOpt = userService.validateToken(token);
            if (userOpt.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }
            String username = userOpt.get().getUsername();

            // Extract ratingId from path
            String path = exchange.getRequestURI().getPath();
            int ratingId = Integer.parseInt(path.split("/")[3]);

            // Delete only comment (set to empty string)
            boolean deleted = ratingService.deleteComment(ratingId, username);

            if (deleted) {
                Rating rating = ratingService.getRatingById(ratingId);
                String response = objectMapper.writeValueAsString(rating);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Rating not found\"}");
            }

        } catch (SecurityException e) {
            sendResponse(exchange, 403, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * POST /api/ratings/{ratingId}/like
     * Like a rating (increment like counter)
     */
    private void handleLikeRating(HttpExchange exchange) throws IOException {
        try {
            // Extract ratingId from path
            String path = exchange.getRequestURI().getPath();
            int ratingId = Integer.parseInt(path.split("/")[3]);

            // Increment likes
            boolean success = ratingService.likeRating(ratingId);

            if (success) {
                Rating rating = ratingService.getRatingById(ratingId);
                String response = objectMapper.writeValueAsString(rating);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Rating not found\"}");
            }

        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * POST /api/ratings/{ratingId}/confirm
     * Confirm a rating (moderation)
     */
    private void handleConfirmRating(HttpExchange exchange) throws IOException {
        try {
            // Extract token and validate user (could add admin check here)
            String token = extractToken(exchange);
            if (token == null) {
                sendResponse(exchange, 401, "{\"error\": \"Missing Authorization header\"}");
                return;
            }

            var userOpt = userService.validateToken(token);
            if (userOpt.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }

            // Extract ratingId from path
            String path = exchange.getRequestURI().getPath();
            int ratingId = Integer.parseInt(path.split("/")[3]);

            // Confirm rating
            boolean success = ratingService.confirmRating(ratingId);

            if (success) {
                sendResponse(exchange, 200, "{\"message\": \"Rating confirmed successfully\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Rating not found\"}");
            }

        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /api/users/{username}/rating-history
     * Get rating history for a user
     */
    private void handleGetRatingHistory(HttpExchange exchange) throws IOException {
        try {
            // Extract username from path
            String path = exchange.getRequestURI().getPath();
            String username = path.split("/")[3];

            List<Rating> ratings = ratingService.getRatingHistory(username);

            String response = objectMapper.writeValueAsString(ratings);
            sendResponse(exchange, 200, response);

        } catch (SQLException e) {
            sendResponse(exchange, 500, "{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Extract Bearer token from Authorization header
     */
    private String extractToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Send HTTP response
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
