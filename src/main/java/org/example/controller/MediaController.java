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

// Controller für Media-Verwaltung (CRUD-Operationen für Filme, Serien, Spiele)
public class MediaController {
    private final MediaService mediaService;
    private final UserService userService;
    private final ObjectMapper objectMapper;  // Jackson: JSON <-> Java Objekt Konvertierung

    public MediaController(MediaService mediaService, UserService userService) {
        this.mediaService = mediaService;
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }
    // Haupt-Handler für /api/media Endpunkt
    // exchange = HTTP-Request-Objekt (vom HttpServer automatisch übergeben)
    public void handleMedia(HttpExchange exchange) throws IOException {
        Optional<User> user = authenticateRequest(exchange);  // Prüfe Token (optional für GET)

        String method = exchange.getRequestMethod(); // GET, POST, PUT, DELETE
        String path = exchange.getRequestURI().getPath(); // /api/media oder /api/media/{id}

        try {
            switch (method) {
                case "GET":
                    handleGetMedia(exchange, path);  //kein Token nötig
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

    // get alle oder spezifisches Media
    private void handleGetMedia(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");

        if (parts.length == 3) {
            // GET /api/media - get all media
            List<MediaEntry> media = mediaService.getAllMedia();
            String response = objectMapper.writeValueAsString(media);  // Liste -> JSON
            sendResponse(exchange, 200, response);
        } else if (parts.length == 4) {
            // GET /api/media/{id} - get specific media
            try {
                int id = Integer.parseInt(parts[3]);  // Extrahiere ID aus URL
                Optional<MediaEntry> media = mediaService.getMediaById(id);
                if (media.isPresent()) {
                    String response = objectMapper.writeValueAsString(media.get());
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Media not found\"}");  // 404 = Not Found
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid media ID\"}");  // ID ist keine Zahl
            }
        } else {
            sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
        }
    }

    // POST /api/media - Erstellt neuen Media-Eintrag
    private void handleCreateMedia(HttpExchange exchange, User user) throws IOException {
        try {
            // Lese JSON aus Request-Body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            MediaEntry media = objectMapper.readValue(body, MediaEntry.class);  // JSON -> MediaEntry

            MediaEntry created = mediaService.createMedia(media, user);  // Speichere in DB
            String response = objectMapper.writeValueAsString(created);  // MediaEntry -> JSON
            sendResponse(exchange, 201, response);  // 201 = Created
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PUT /api/media/{id} - Aktualisiert Media-Eintrag (nur Creator darf updaten)
    private void handleUpdateMedia(HttpExchange exchange, String path, User user) throws IOException {
        String[] parts = path.split("/");
        if (parts.length != 4) {  // Muss /api/media/{id} sein
            sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            return;
        }

        try {
            int id = Integer.parseInt(parts[3]);  // Extrahiere ID aus URL
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

    // DELETE /api/media/{id} (nur Creator darf löschen)
    private void handleDeleteMedia(HttpExchange exchange, String path, User user) throws IOException {
        String[] parts = path.split("/");
        if (parts.length != 4) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
            return;
        }

        try {
            int id = Integer.parseInt(parts[3]);
            mediaService.deleteMedia(id, user);  // Service prüft ob User = Creator
            sendResponse(exchange, 204, "");  // 204 = No Content (erfolgreich gelöscht)
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid media ID\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 403, "{\"error\":\"" + e.getMessage() + "\"}");  // Nicht berechtigt
        }
    }

    // Authentifiziert Request über Token im Authorization-Header
    private Optional<User> authenticateRequest(HttpExchange exchange) {
        try {
            // Lese Authorization-Header: "Bearer {token}"
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Optional.empty();  // Kein Token vorhanden
            }

            String token = authHeader.substring(7);  // Entferne "Bearer ", hole nur Token
            return userService.validateToken(token);  // Prüfe Token in DB
        } catch (RuntimeException e) {
            return Optional.empty();  //nicht authentifiziert
        }
    }

    // Sendet HTTP-Response mit JSON-Content
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");  // JSON-Header setzen
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);  // Status + Content-Length
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);  // Schreibe Response-Body
        }
    }
}

