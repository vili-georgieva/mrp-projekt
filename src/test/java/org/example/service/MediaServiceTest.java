package org.example.service;

import org.example.model.MediaEntry;
import org.example.model.MediaType;
import org.example.model.User;
import org.example.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Unit Tests für MediaService - Business Logic Layer
// Testet Media-Management-Logik (Create, Update, Delete)
@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    private MediaService mediaService;
    private User testUser;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(mediaRepository);
        testUser = new User("testuser", "hashedpass");
    }

    // Test: Erfolgreiche Media-Erstellung
    @Test
    void createMediaTest() {
        MediaEntry media = new MediaEntry();
        media.setTitle("Test Movie");
        media.setMediaType(MediaType.MOVIE);
        media.setDescription("Test description");

        when(mediaRepository.save(any(MediaEntry.class))).thenReturn(1);

        MediaEntry result = mediaService.createMedia(media, testUser);

        assertNotNull(result);
        assertEquals("Test Movie", result.getTitle());
        assertEquals("testuser", result.getCreator());
        assertEquals(1, result.getId());
        verify(mediaRepository).save(any(MediaEntry.class));
    }

    // Test: Media-Erstellung mit leerem Titel wirft Exception
    @Test
    void createMediaWithEmptyTitleTest() {
        MediaEntry media = new MediaEntry();
        media.setTitle("");
        media.setMediaType(MediaType.MOVIE);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.createMedia(media, testUser)
        );

        assertEquals("Title cannot be empty", exception.getMessage());
        verify(mediaRepository, never()).save(any(MediaEntry.class));
    }

    // Test: Erfolgreiche Media-Update (nur eigene Media)
    @Test
    void updateMediaTest() {
        MediaEntry existingMedia = new MediaEntry();
        existingMedia.setId(1);
        existingMedia.setTitle("Old Title");
        existingMedia.setCreator("testuser");
        existingMedia.setMediaType(MediaType.MOVIE);

        MediaEntry updatedMedia = new MediaEntry();
        updatedMedia.setTitle("New Title");
        updatedMedia.setMediaType(MediaType.MOVIE);

        when(mediaRepository.findById(1)).thenReturn(Optional.of(existingMedia));
        doNothing().when(mediaRepository).update(any(MediaEntry.class));

        MediaEntry result = mediaService.updateMedia(1, updatedMedia, testUser);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("testuser", result.getCreator());
        verify(mediaRepository).findById(1);
        verify(mediaRepository).update(any(MediaEntry.class));
    }

    // Test: Update von fremder Media wirft Exception
    @Test
    void updateMediaByDifferentUserTest() {
        MediaEntry existingMedia = new MediaEntry();
        existingMedia.setId(1);
        existingMedia.setCreator("otheruser");

        MediaEntry updatedMedia = new MediaEntry();
        updatedMedia.setTitle("New Title");

        when(mediaRepository.findById(1)).thenReturn(Optional.of(existingMedia));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.updateMedia(1, updatedMedia, testUser)
        );

        assertEquals("You can only update your own media entries", exception.getMessage());
        verify(mediaRepository, never()).update(any(MediaEntry.class));
    }

    // Test: Erfolgreiche Media-Löschung (nur eigene Media)
    @Test
    void deleteMediaTest() {
        MediaEntry existingMedia = new MediaEntry();
        existingMedia.setId(1);
        existingMedia.setCreator("testuser");

        when(mediaRepository.findById(1)).thenReturn(Optional.of(existingMedia));
        doNothing().when(mediaRepository).delete(1);

        assertDoesNotThrow(() -> mediaService.deleteMedia(1, testUser));

        verify(mediaRepository).findById(1);
        verify(mediaRepository).delete(1);
    }

    // Test: Löschung von fremder Media wirft Exception
    @Test
    void deleteMediaByDifferentUserTest() {
        MediaEntry existingMedia = new MediaEntry();
        existingMedia.setId(1);
        existingMedia.setCreator("otheruser");

        when(mediaRepository.findById(1)).thenReturn(Optional.of(existingMedia));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.deleteMedia(1, testUser)
        );

        assertEquals("You can only delete your own media entries", exception.getMessage());
        verify(mediaRepository, never()).delete(anyInt());
    }

    // Test: getAllMedia gibt Liste von Media zurück
    @Test
    void getAllMediaTest() {
        MediaEntry media1 = new MediaEntry();
        media1.setId(1);
        media1.setTitle("Movie 1");

        MediaEntry media2 = new MediaEntry();
        media2.setId(2);
        media2.setTitle("Series 1");

        List<MediaEntry> mockList = Arrays.asList(media1, media2);
        when(mediaRepository.findAll()).thenReturn(mockList);

        List<MediaEntry> result = mediaService.getAllMedia();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Movie 1", result.get(0).getTitle());
        assertEquals("Series 1", result.get(1).getTitle());
        verify(mediaRepository).findAll();
    }

    // Test: Update von nicht existierender Media wirft Exception
    @Test
    void updateNonexistentMediaTest() {
        MediaEntry updatedMedia = new MediaEntry();
        updatedMedia.setTitle("Updated Title");
        updatedMedia.setMediaType(MediaType.MOVIE);

        when(mediaRepository.findById(999)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.updateMedia(999, updatedMedia, testUser)
        );

        assertEquals("Media entry not found", exception.getMessage());
        verify(mediaRepository).findById(999);
        verify(mediaRepository, never()).update(any(MediaEntry.class));
    }

    // Test: Creating media without MediaType throws exception
    @Test
    void createMediaWithoutMediaTypeTest() {
        MediaEntry media = new MediaEntry();
        media.setTitle("Test Movie");
        media.setMediaType(null);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mediaService.createMedia(media, testUser)
        );

        assertEquals("Media type cannot be empty", exception.getMessage());
        verify(mediaRepository, never()).save(any(MediaEntry.class));
    }

    // Test: Get media by ID returns correct media
    @Test
    void getMediaByIdTest() {
        MediaEntry existingMedia = new MediaEntry();
        existingMedia.setId(1);
        existingMedia.setTitle("Test Movie");
        existingMedia.setMediaType(MediaType.MOVIE);

        when(mediaRepository.findById(1)).thenReturn(Optional.of(existingMedia));

        Optional<MediaEntry> result = mediaService.getMediaById(1);

        assertTrue(result.isPresent());
        assertEquals("Test Movie", result.get().getTitle());
        assertEquals(1, result.get().getId());
        verify(mediaRepository).findById(1);
    }

    // Test: Get non-existent media by ID returns empty
    @Test
    void getMediaByIdNotFoundTest() {
        when(mediaRepository.findById(999)).thenReturn(Optional.empty());

        Optional<MediaEntry> result = mediaService.getMediaById(999);

        assertFalse(result.isPresent());
        verify(mediaRepository).findById(999);
    }
}
