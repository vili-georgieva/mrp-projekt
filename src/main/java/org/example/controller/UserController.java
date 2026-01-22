package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.example.dto.LoginRequest;
import org.example.dto.RegisterRequest;
import org.example.model.User;
import org.example.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

// Controller for User HTTP endpoints (registration, login, user info)
public class UserController {
    private final UserService userService;
    private final ObjectMapper objectMapper;  // Jackson: JSON <-> Java object conversion

    public UserController(UserService userService) {
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
    }

    // POST /api/users/register
    public void handleRegister(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            // Read JSON from request body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            RegisterRequest request = objectMapper.readValue(body, RegisterRequest.class);  // JSON -> Java object

            User user = userService.register(request.getUsername(), request.getPassword()); // Register user

            String response = objectMapper.writeValueAsString(new ResponseMessage("User registered successfully"));
            sendResponse(exchange, 201, response);  // 201 = Created
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");  // 400 = Bad Request
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");  // 500 = Server Error
        }
    }

    // POST /api/users/login - Authenticates user and returns token
    public void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            // Read login data from request
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            LoginRequest request = objectMapper.readValue(body, LoginRequest.class);

            String token = userService.login(request.getUsername(), request.getPassword()); // Perform login -> returns token

            sendResponse(exchange, 200, "\"" + token + "\"");  // 200 = OK
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 401, "{\"error\":\"" + e.getMessage() + "\"}");  // 401 = Unauthorized
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    // GET /api/users/{username}
    public void handleGetUser(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            // Check token in Authorization header
            Optional<User> user = authenticateRequest(exchange);
            if (user.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/"); // Extract username from URL: /api/users/{username}

            if (parts.length < 4) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
                return;
            }

            String username = parts[3];
            // Identify user
            if (!username.equals(user.get().getUsername())) {
                sendResponse(exchange, 403, "{\"error\":\"Forbidden\"}");
                return;
            }

            // Build response with user data and statistics
            var responseData = new java.util.HashMap<String, Object>();
            responseData.put("username", user.get().getUsername());
            responseData.put("statistics", userService.getUserStatistics(username));

            String response = objectMapper.writeValueAsString(responseData);
            sendResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    // GET /api/leaderboard?limit=10
    public void handleLeaderboard(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            // Parse limit from query parameter (default 10)
            int limit = 10;
            String query = exchange.getRequestURI().getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("limit=")) {
                        try {
                            limit = Integer.parseInt(param.substring(6));
                        } catch (NumberFormatException e) {
                            // Keep default
                        }
                    }
                }
            }

            var leaderboard = userService.getLeaderboard(limit);
            String response = objectMapper.writeValueAsString(leaderboard);
            sendResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    // GET /api/users/{username}/recommendations?limit=10
    public void handleRecommendations(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");

            if (parts.length < 4) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
                return;
            }

            String username = parts[3];

            // Parse limit from query parameter (default 10)
            int limit = 10;
            String query = exchange.getRequestURI().getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("limit=")) {
                        try {
                            limit = Integer.parseInt(param.substring(6));
                        } catch (NumberFormatException e) {
                            // Keep default
                        }
                    }
                }
            }

            var recommendations = userService.getRecommendations(username, limit);
            String response = objectMapper.writeValueAsString(recommendations);
            sendResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    // Authenticates request via token in Authorization header
    private Optional<User> authenticateRequest(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();  // No token present
        }

        // Extract token (remove "Bearer " prefix), "Bearer " = 7 characters
        String token = authHeader.substring(7);
        return userService.validateToken(token);  // Check token in DB
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

    // Simple JSON responses with "message" field
    private static class ResponseMessage {
        public String message;

        public ResponseMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}

