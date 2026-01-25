package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.example.model.MediaEntry;
import org.example.model.MediaType;
import org.example.model.User;
import org.example.service.MediaService;
import org.example.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

// Controller für Media-Management (CRUD Operationen für Filme, Serien, Spiele)
public class MediaController {
    private final MediaService mediaService;
    private final UserService userService;
    private final ObjectMapper objectMapper;  // Jackson: JSON <-> Java Object Konvertierung

    public MediaController(MediaService mediaService, UserService userService) {
        this.mediaService = mediaService;
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }
    // Haupt-Handler für /api/media Endpoint
    // exchange = HTTP Request Object (automatisch vom HttpServer übergeben)
    public void handleMedia(HttpExchange exchange) throws IOException {
        Optional<User> user = authenticateRequest(exchange);  // Prüft Token (optional für GET)

        String method = exchange.getRequestMethod(); // GET, POST, PUT, DELETE
        String path = exchange.getRequestURI().getPath(); // /api/media oder /api/media/{id}

        try {
            switch (method) {
                case "GET":
                    handleGetMedia(exchange, path);  // Kein Token erforderlich
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

    // Lädt alle oder spezifisches Media
    private void handleGetMedia(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");

        if (parts.length == 3) {
            // GET /api/media - lädt alle Media ODER Suche mit Query-Parametern
            String query = exchange.getRequestURI().getQuery();

            if (query != null && !query.isEmpty()) {
                // Parst Query-Parameter für Suche
                String title = getQueryParam(query, "title");
                String genre = getQueryParam(query, "genre");
                String typeStr = getQueryParam(query, "mediaType");
                String ratingStr = getQueryParam(query, "minRating");
                String ageStr = getQueryParam(query, "ageRestriction");

                MediaType mediaType = null;
                if (typeStr != null && !typeStr.isEmpty()) {
                    try {
                        mediaType = MediaType.valueOf(typeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Ungültiger Media-Typ, ignorieren
                    }
                }

                Integer minRating = parseIntegerParam(ratingStr);
                Integer ageRestriction = parseIntegerParam(ageStr);

                List<MediaEntry> results = mediaService.searchMedia(title, genre, mediaType, minRating, ageRestriction);
                String response = objectMapper.writeValueAsString(results);
                sendResponse(exchange, 200, response);
            } else {
                // gibt alle Media zurück
                List<MediaEntry> media = mediaService.getAllMedia();
                String response = objectMapper.writeValueAsString(media);  // List -> JSON
                sendResponse(exchange, 200, response);
            }
        } else if (parts.length == 4) {
            // GET /api/media/{id} - lädt spezifisches Media
            try {
                int id = Integer.parseInt(parts[3]);  // Extrahiert ID aus URL
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
            // Liest JSON vom Request Body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            MediaEntry media = objectMapper.readValue(body, MediaEntry.class);  // JSON -> MediaEntry

            MediaEntry created = mediaService.createMedia(media, user);  // Speichert in DB
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
            int id = Integer.parseInt(parts[3]);  // Extrahiert ID aus URL
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
            mediaService.deleteMedia(id, user);  // Service prüft ob user = creator
            sendResponse(exchange, 204, "");  // 204 = No Content (erfolgreich gelöscht)
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid media ID\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 403, "{\"error\":\"" + e.getMessage() + "\"}");  // Nicht autorisiert
        }
    }

    // Authentifiziert Request via Token im Authorization Header
    private Optional<User> authenticateRequest(HttpExchange exchange) {
        try {
            // Liest Authorization Header: "Bearer {token}"
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Optional.empty();  // Kein Token vorhanden
            }

            String token = authHeader.substring(7);  // Entfernt "Bearer ", nimmt nur Token
            return userService.validateToken(token);  // Prüft Token in DB
        } catch (RuntimeException e) {
            return Optional.empty();  // Nicht authentifiziert
        }
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

    // Extrahiert Query-Parameter aus Query-String (z.B. "?title=Matrix&genre=Action")
    private String getQueryParam(String query, String paramName) {
        if (query == null) return null;
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
    }

    // Parst Integer aus String, gibt null zurück wenn ungültig
    private Integer parseIntegerParam(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

