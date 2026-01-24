package org.example.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.example.model.MediaEntry;
import org.example.model.MediaType;
import org.example.model.User;
import org.example.service.MediaService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Unit Tests for MediaController - Presentation Layer
// Tests HTTP request handling for media endpoints
@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @Mock
    private UserService userService;

    @Mock
    private HttpExchange exchange;

    private MediaController mediaController;
    private ByteArrayOutputStream responseBody;
    private Headers responseHeaders;

    @BeforeEach
    void setUp() {
        mediaController = new MediaController(mediaService, userService);
        responseBody = new ByteArrayOutputStream();
        responseHeaders = new Headers();
    }

    // Test: GET /api/media returns all media (200 OK)
    @Test
    void handleGetAllMediaTest() throws Exception {
        MediaEntry media1 = new MediaEntry();
        media1.setId(1);
        media1.setTitle("Test Movie");
        media1.setMediaType(MediaType.MOVIE);

        MediaEntry media2 = new MediaEntry();
        media2.setId(2);
        media2.setTitle("Test Series");
        media2.setMediaType(MediaType.SERIES);

        List<MediaEntry> mockList = Arrays.asList(media1, media2);

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getRequestHeaders()).thenReturn(new Headers());
        when(mediaService.getAllMedia()).thenReturn(mockList);

        mediaController.handleMedia(exchange);

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        assertTrue(responseBody.toString().contains("Test Movie"));
        assertTrue(responseBody.toString().contains("Test Series"));
    }

    // Test: POST /api/media without token returns 401 Unauthorized
    @Test
    void handleCreateMediaWithoutTokenTest() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getRequestHeaders()).thenReturn(new Headers());

        mediaController.handleMedia(exchange);

        verify(exchange).sendResponseHeaders(eq(401), anyLong());
        assertTrue(responseBody.toString().contains("Unauthorized"));
    }

    // Test: POST /api/media with valid token creates media (201 Created)
    @Test
    void handleCreateMediaWithValidTokenTest() throws Exception {
        String requestBody = "{\"title\":\"New Movie\",\"mediaType\":\"MOVIE\",\"description\":\"Test description\"}";
        User mockUser = new User("testuser", "hashedpass");
        mockUser.setToken("valid-token");

        MediaEntry createdMedia = new MediaEntry();
        createdMedia.setId(1);
        createdMedia.setTitle("New Movie");
        createdMedia.setMediaType(MediaType.MOVIE);
        createdMedia.setCreator("testuser");

        Headers requestHeaders = new Headers();
        requestHeaders.add("Authorization", "Bearer valid-token");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media"));
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(userService.validateToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(mediaService.createMedia(any(MediaEntry.class), eq(mockUser))).thenReturn(createdMedia);

        mediaController.handleMedia(exchange);

        verify(exchange).sendResponseHeaders(eq(201), anyLong());
        verify(mediaService).createMedia(any(MediaEntry.class), eq(mockUser));
    }

    // Test: DELETE /api/media/{id} without token returns 401 Unauthorized
    @Test
    void handleDeleteMediaWithoutTokenTest() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media/1"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getRequestHeaders()).thenReturn(new Headers());

        mediaController.handleMedia(exchange);

        verify(exchange).sendResponseHeaders(eq(401), anyLong());
        assertTrue(responseBody.toString().contains("Unauthorized"));
    }

    // Test: PUT /api/media/{id} without token returns 401 Unauthorized
    @Test
    void handleUpdateMediaWithoutTokenTest() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("PUT");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media/1"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getRequestHeaders()).thenReturn(new Headers());

        mediaController.handleMedia(exchange);

        verify(exchange).sendResponseHeaders(eq(401), anyLong());
        assertTrue(responseBody.toString().contains("Unauthorized"));
    }

    // Test: Unsupported HTTP method returns 405 Method Not Allowed
    @Test
    void handleUnsupportedMethodTest() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("PATCH");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getRequestHeaders()).thenReturn(new Headers());

        mediaController.handleMedia(exchange);

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
        assertTrue(responseBody.toString().contains("Method not allowed"));
    }
}
