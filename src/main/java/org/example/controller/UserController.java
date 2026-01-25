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

// Controller für User HTTP Endpoints (Registrierung, Login, User Info)
public class UserController {
    private final UserService userService;
    private final ObjectMapper objectMapper;  // Jackson: JSON <-> Java Object Konvertierung

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
            // Liest JSON vom Request Body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            RegisterRequest request = objectMapper.readValue(body, RegisterRequest.class);  // JSON -> Java Object

            User user = userService.register(request.getUsername(), request.getPassword()); // Registriert User

            String response = objectMapper.writeValueAsString(new ResponseMessage("User registered successfully"));
            sendResponse(exchange, 201, response);  // 201 = Created
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");  // 400 = Bad Request
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");  // 500 = Server Error
        }
    }

    // POST /api/users/login - Authentifiziert User und gibt Token zurück
    public void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            // Liest Login-Daten vom Request
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            LoginRequest request = objectMapper.readValue(body, LoginRequest.class);

            String token = userService.login(request.getUsername(), request.getPassword()); // Führt Login durch -> gibt Token zurück

            sendResponse(exchange, 200, "\"" + token + "\"");  // 200 = OK
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 401, "{\"error\":\"" + e.getMessage() + "\"}");  // 401 = Unauthorized
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    // PUT /api/users/{username} - Aktualisiert User-Profil (Passwort)
    public void handleUpdateProfile(HttpExchange exchange) throws IOException {
        if (!"PUT".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            // Prüft Authentifizierung
            Optional<User> user = authenticateRequest(exchange);
            if (user.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            // Extrahiert Username aus Pfad
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
                return;
            }

            String username = parts[3];

            // User kann nur eigenes Profil aktualisieren
            if (!username.equals(user.get().getUsername())) {
                sendResponse(exchange, 403, "{\"error\":\"Forbidden\"}");
                return;
            }

            // Liest neues Passwort vom Request Body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var updateData = objectMapper.readValue(body, java.util.Map.class);

            String newPassword = (String) updateData.get("password");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Password cannot be empty\"}");
                return;
            }

            // Aktualisiert Passwort
            userService.updatePassword(username, newPassword);

            sendResponse(exchange, 200, "{\"message\":\"Profile updated successfully\"}");
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
            // Prüft Token im Authorization Header
            Optional<User> user = authenticateRequest(exchange);
            if (user.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/"); // Extrahiert Username aus URL: /api/users/{username}

            if (parts.length < 4) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
                return;
            }

            String username = parts[3];
            // Identifiziert User
            if (!username.equals(user.get().getUsername())) {
                sendResponse(exchange, 403, "{\"error\":\"Forbidden\"}");
                return;
            }

            // Baut Response mit User-Daten und Statistiken
            var responseData = new java.util.HashMap<String, Object>();
            responseData.put("username", user.get().getUsername());
            responseData.put("statistics", userService.getUserStatistics(username));

            String response = objectMapper.writeValueAsString(responseData);
            sendResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    // GET /api/leaderboard?limit=10 - benötigt Authentifizierung


    // Parst Limit-Parameter aus Query String, gibt defaultValue zurück wenn nicht gefunden
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

    // Authentifiziert Request via Token im Authorization Header
    private Optional<User> authenticateRequest(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();  // Kein Token vorhanden
        }

        // Extrahiert Token (entfernt "Bearer " Prefix), "Bearer " = 7 Zeichen
        String token = authHeader.substring(7);
        return userService.validateToken(token);  // Prüft Token in DB
    }

    // Sendet HTTP Response mit JSON Content
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");  // Setzt JSON Header
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);  // Status + Content-Length
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);  // Schreibt Response Body
        }
    }

    // Einfache JSON Response mit "message" Feld
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

