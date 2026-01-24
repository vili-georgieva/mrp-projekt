package org.example.service;

import org.example.repository.FavoriteRepository;
import org.example.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Unit Tests for FavoriteService - Business Logic Layer
// Tests favorite management logic (add, remove, toggle, check)
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private MediaRepository mediaRepository;

    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() {
        favoriteService = new FavoriteService(favoriteRepository, mediaRepository);
    }

    // Test: Successfully add media to favorites
    @Test
    void addFavoriteTest() {
        String username = "testuser";
        int mediaId = 1;

        when(mediaRepository.getMediaById(mediaId)).thenReturn(new org.example.model.MediaEntry());
        when(favoriteRepository.isFavorite(username, mediaId)).thenReturn(false);
        when(favoriteRepository.addFavorite(username, mediaId)).thenReturn(true);

        assertDoesNotThrow(() -> favoriteService.addFavorite(username, mediaId));

        verify(mediaRepository).getMediaById(mediaId);
        verify(favoriteRepository).isFavorite(username, mediaId);
        verify(favoriteRepository).addFavorite(username, mediaId);
    }

    // Test: Adding already favorited media throws exception
    @Test
    void addFavoriteAlreadyExistsTest() {
        String username = "testuser";
        int mediaId = 1;

        when(mediaRepository.getMediaById(mediaId)).thenReturn(new org.example.model.MediaEntry());
        when(favoriteRepository.isFavorite(username, mediaId)).thenReturn(true);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> favoriteService.addFavorite(username, mediaId)
        );

        assertEquals("Media is already in favorites", exception.getMessage());
        verify(favoriteRepository, never()).addFavorite(anyString(), anyInt());
    }

    // Test: Toggle favorite adds when not favorited
    @Test
    void toggleFavoriteAddTest() {
        String username = "testuser";
        int mediaId = 1;

        when(mediaRepository.getMediaById(mediaId)).thenReturn(new org.example.model.MediaEntry());
        when(favoriteRepository.isFavorite(username, mediaId)).thenReturn(false);
        when(favoriteRepository.addFavorite(username, mediaId)).thenReturn(true);

        boolean result = favoriteService.toggleFavorite(username, mediaId);

        assertTrue(result);
        verify(favoriteRepository).addFavorite(username, mediaId);
        verify(favoriteRepository, never()).removeFavorite(anyString(), anyInt());
    }

    // Test: Toggle favorite removes when already favorited
    @Test
    void toggleFavoriteRemoveTest() {
        String username = "testuser";
        int mediaId = 1;

        when(mediaRepository.getMediaById(mediaId)).thenReturn(new org.example.model.MediaEntry());
        when(favoriteRepository.isFavorite(username, mediaId)).thenReturn(true);
        when(favoriteRepository.removeFavorite(username, mediaId)).thenReturn(true);

        boolean result = favoriteService.toggleFavorite(username, mediaId);

        assertFalse(result);
        verify(favoriteRepository).removeFavorite(username, mediaId);
        verify(favoriteRepository, never()).addFavorite(anyString(), anyInt());
    }
}
