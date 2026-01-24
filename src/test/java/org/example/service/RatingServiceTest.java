package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

// Unit Tests für RatingService - Business Logic Layer
// Testet Rating-Logik (Create, Update, Delete, Like, Confirm)
@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService();
    }

    // Test: Rating mit ungültigen Sternen (< 1) wirft Exception
    @Test
    void createRatingWithInvalidStarsTest() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ratingService.createOrUpdateRating(1, "testuser", 0, "Bad rating")
        );

        assertEquals("Stars must be between 1 and 5", exception.getMessage());
    }

    // Test: Rating with invalid stars (> 5) throws exception
    @Test
    void createRatingWithTooManyStarsTest() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ratingService.createOrUpdateRating(1, "testuser", 6, "Too good")
        );

        assertEquals("Stars must be between 1 and 5", exception.getMessage());
    }

    // Test: Rating with valid stars (1) is accepted
    @Test
    void createRatingWithMinStarsTest() {
        assertDoesNotThrow(
            () -> ratingService.createOrUpdateRating(1, "testuser", 1, "Minimum rating")
        );
    }

    // Test: Rating with valid stars (5) is accepted
    @Test
    void createRatingWithMaxStarsTest() {
        assertDoesNotThrow(
            () -> ratingService.createOrUpdateRating(1, "testuser", 5, "Maximum rating")
        );
    }

    // Test: Rating with null comment is accepted
    @Test
    void createRatingWithNullCommentTest() {
        assertDoesNotThrow(
            () -> ratingService.createOrUpdateRating(1, "testuser", 4, null)
        );
    }
}
