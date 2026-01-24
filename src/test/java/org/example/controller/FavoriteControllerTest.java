package org.example.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.example.model.User;
import org.example.service.FavoriteService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Unit Tests for FavoriteController - Presentation Layer
// Tests HTTP request handling for favorite endpoints
@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private UserService userService;

    @Mock
    private HttpExchange exchange;

    private FavoriteController favoriteController;
    private ByteArrayOutputStream responseBody;
    private Headers responseHeaders;

    @BeforeEach
    void setUp() {
        favoriteController = new FavoriteController(favoriteService, userService);
        responseBody = new ByteArrayOutputStream();
        responseHeaders = new Headers();
    }

    // Test: GET favorites without token returns 401 Unauthorized
    @Test
    void handleGetFavoritesWithoutTokenTest() throws Exception {
        when(exchange.getRequestHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);

        favoriteController.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(401), anyLong());
        assertTrue(responseBody.toString().contains("error"));
    }

    // Test: POST add favorite without token returns 401
    @Test
    void handleAddFavoriteWithoutTokenTest() throws Exception {
        when(exchange.getRequestHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);

        favoriteController.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(401), anyLong());
    }

    // Test: Toggle favorite with valid token works
    @Test
    void handleToggleFavoriteWithValidTokenTest() throws Exception {
        User mockUser = new User("testuser", "hashedpass");
        mockUser.setToken("valid-token");

        Headers requestHeaders = new Headers();
        requestHeaders.add("Authorization", "Bearer valid-token");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/users/testuser/favorites/1/toggle"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(userService.validateToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(favoriteService.toggleFavorite("testuser", 1)).thenReturn(true);

        favoriteController.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        verify(favoriteService).toggleFavorite("testuser", 1);
    }

    // Test: Check favorite with valid token works
    @Test
    void handleCheckFavoriteTest() throws Exception {
        User mockUser = new User("testuser", "hashedpass");
        mockUser.setToken("valid-token");

        Headers requestHeaders = new Headers();
        requestHeaders.add("Authorization", "Bearer valid-token");

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/users/testuser/favorites/check/1"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(userService.validateToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(favoriteService.isFavorite("testuser", 1)).thenReturn(true);

        favoriteController.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        assertTrue(responseBody.toString().contains("true"));
    }
}
