package org.example.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.example.model.Rating;
import org.example.model.User;
import org.example.service.RatingService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Unit Tests für RatingController - Presentation Layer
// Testet HTTP Request Handling für Rating Endpoints
@ExtendWith(MockitoExtension.class)
class RatingControllerTest {

    @Mock
    private RatingService ratingService;

    @Mock
    private UserService userService;

    @Mock
    private HttpExchange exchange;

    private RatingController ratingController;
    private ByteArrayOutputStream responseBody;
    private Headers responseHeaders;

    @BeforeEach
    void setUp() {
        ratingController = new RatingController(ratingService, userService);
        responseBody = new ByteArrayOutputStream();
        responseHeaders = new Headers();
    }

    // Test: GET /api/media/{id}/ratings gibt Ratings für Media zurück
    @Test
    void handleGetRatingsForMediaTest() throws Exception {
        Rating rating1 = new Rating();
        rating1.setId(1);
        rating1.setMediaId(1);
        rating1.setUsername("user1");
        rating1.setStars(5);

        List<Rating> mockRatings = List.of(rating1);

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media/1/ratings"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(ratingService.getRatingsByMediaId(1)).thenReturn(mockRatings);

        ratingController.handleMediaRatings(exchange);

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        verify(ratingService).getRatingsByMediaId(1);
    }

    // Test: POST /api/media/{id}/ratings ohne Token gibt 401 zurück
    @Test
    void handleCreateRatingWithoutTokenTest() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media/1/ratings"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getRequestHeaders()).thenReturn(new Headers());

        ratingController.handleMediaRatings(exchange);

        verify(exchange).sendResponseHeaders(eq(401), anyLong());
        assertTrue(responseBody.toString().contains("Unauthorized"));
    }

    // Test: POST /api/media/{id}/ratings mit gültigem Token erstellt Rating
    @Test
    void handleCreateRatingWithValidTokenTest() throws Exception {
        String requestBody = "{\"stars\":5,\"comment\":\"Great movie!\"}";
        User mockUser = new User("testuser", "hashedpass");
        mockUser.setToken("valid-token");

        Rating createdRating = new Rating();
        createdRating.setId(1);
        createdRating.setMediaId(1);
        createdRating.setUsername("testuser");
        createdRating.setStars(5);
        createdRating.setComment("Great movie!");

        Headers requestHeaders = new Headers();
        requestHeaders.add("Authorization", "Bearer valid-token");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media/1/ratings"));
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(userService.validateToken("valid-token")).thenReturn(Optional.of(mockUser));
        when(ratingService.createOrUpdateRating(eq(1), eq("testuser"), eq(5), eq("Great movie!")))
            .thenReturn(createdRating);

        ratingController.handleMediaRatings(exchange);

        verify(exchange).sendResponseHeaders(eq(201), anyLong());
        verify(ratingService).createOrUpdateRating(1, "testuser", 5, "Great movie!");
    }

    // Test: Ungültige Media-ID gibt 400 Bad Request zurück
    @Test
    void handleInvalidMediaIdTest() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media/invalid/ratings"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);

        ratingController.handleMediaRatings(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString().contains("Invalid media ID"));
    }

    // Test: Nicht unterstützte Methode gibt 405 zurück
    @Test
    void handleUnsupportedMethodTest() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media/1/ratings"));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);

        ratingController.handleMediaRatings(exchange);

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
    }
}
