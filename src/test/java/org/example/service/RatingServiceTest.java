package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für RatingService - Business Logic Layer
 * Testet Rating-Logik (Create, Update, Delete, Like, Confirm)
 */
@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        // Da RatingService keine Dependency Injection im Constructor hat,
        // müssen wir dies über Reflection oder durch Refactoring lösen.
        // Für diesen Test verwenden wir einen vereinfachten Ansatz.
        ratingService = new RatingService();
    }

    /**
     * Test: Rating mit ungültigen Sternen (< 1) wirft Exception
     */
    @Test
    void createRatingWithInvalidStarsTest() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ratingService.createOrUpdateRating(1, "testuser", 0, "Bad rating")
        );

        assertEquals("Stars must be between 1 and 5", exception.getMessage());
    }

    /**
     * Test: Rating mit ungültigen Sternen (> 5) wirft Exception
     */
    @Test
    void createRatingWithTooManyStarsTest() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ratingService.createOrUpdateRating(1, "testuser", 6, "Too good")
        );

        assertEquals("Stars must be between 1 and 5", exception.getMessage());
    }
}
