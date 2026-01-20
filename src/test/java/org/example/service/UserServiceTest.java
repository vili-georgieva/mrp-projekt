package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für UserService - Business Logic Layer
 * Testet User-Management-Logik (Registrierung, Login, Token-Validierung)
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    /**
     * Test: Erfolgreiche User-Registrierung
     */
    @Test
    void registerTest() {
        // Arrange
        String username = "newuser";
        String password = "securepass";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        doNothing().when(userRepository).save(any(User.class));

        // Act
        User result = userService.register(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertNotEquals(password, result.getPassword()); // Password sollte gehasht sein
        verify(userRepository).findByUsername(username);
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test: Registrierung mit existierendem Username wirft Exception
     */
    @Test
    void registerWithExistingUsernameTest() {
        // Arrange
        String username = "existing";
        User existingUser = new User(username, "hashedpass");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.register(username, "password")
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test: Registrierung mit leerem Username wirft Exception
     */
    @Test
    void registerWithEmptyUsernameTest() {
        // Arrange
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.register("", "password")
        );

        assertEquals("Username cannot be empty", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test: Login mit korrekten Credentials gibt Token zurück
     */
    @Test
    void loginTest() {
        // Arrange
        String username = "testuser";
        String password = "testpass";

        // Simuliere gehashtes Password (in realität würde das durch SHA-256 gehen)
        User mockUser = new User(username, password);
        // Wir müssen das gehashte Password simulieren
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        doNothing().when(userRepository).updateToken(eq(username), anyString());

        // Act
        String token = userService.login(username, password);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertFalse(token.contains("mrpToken")); // Alter Token-Stil sollte nicht verwendet werden
        assertTrue(token.contains("-")); // UUID enthält Bindestriche
        verify(userRepository).findByUsername(username);
        verify(userRepository).updateToken(eq(username), anyString());
    }

    /**
     * Test: Token-Validierung mit gültigem Token gibt User zurück
     */
    @Test
    void validateTokenTest() {
        // Arrange
        String token = "valid-token-123";
        User mockUser = new User("testuser", "hashedpass");
        mockUser.setToken(token);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(mockUser));

        // Act
        Optional<User> result = userService.validateToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByToken(token);
    }

    /**
     * Test: Token-Validierung mit ungültigem Token gibt leeres Optional zurück
     */
    @Test
    void validateTokenWithInvalidTokenTest() {
        // Arrange
        String invalidToken = "invalid-token";

        when(userRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.validateToken(invalidToken);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByToken(invalidToken);
    }
}
