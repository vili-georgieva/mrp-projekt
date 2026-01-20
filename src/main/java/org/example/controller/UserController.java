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

// Controller für User HTTP-Endpoints (Registrierung, Login, User-Info)
public class UserController {
    private final UserService userService;
    private final ObjectMapper objectMapper;  // Jackson: JSON <-> Java Objekt Konvertierung

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
            // Lese JSON aus Request-Body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            RegisterRequest request = objectMapper.readValue(body, RegisterRequest.class);  // JSON -> Java Objekt

            User user = userService.register(request.getUsername(), request.getPassword()); // Registriere User

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
            // Lese Login-Daten aus Request
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            LoginRequest request = objectMapper.readValue(body, LoginRequest.class);

            String token = userService.login(request.getUsername(), request.getPassword()); // Login durchführen -> gibt Token zurück

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
            // Prüfe Token im Authorization-Header
            Optional<User> user = authenticateRequest(exchange);
            if (user.isEmpty()) {
                sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }


            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/"); // Extrahiere Username aus URL: /api/users/{username}

            if (parts.length < 4) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
                return;
            }

            String username = parts[3];
            // User identifizieren
            if (!username.equals(user.get().getUsername())) {
                sendResponse(exchange, 403, "{\"error\":\"Forbidden\"}");
                return;
            }

            String response = objectMapper.writeValueAsString(user.get());
            sendResponse(exchange, 200, response);
        } catch (RuntimeException e) {
            sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
        }
    }

    //Authentifiziert Request über Token im Authorization-Header
    private Optional<User> authenticateRequest(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();  // Kein Token vorhanden
        }

        // Extrahiere Token (entferne "Bearer " Prefix) ,"Bearer " = 7 Zeichen
        String token = authHeader.substring(7);
        return userService.validateToken(token);  // Prüfe Token in DB
    }

    //Sendet HTTP-Response mit JSON-Content
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");  // JSON-Header setzen
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);  // Status + Content-Length
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);  // Schreibe Response-Body
        }
    }

    //einfache JSON-Responses mit "message"-Feld
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

