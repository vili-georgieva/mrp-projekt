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

// Unit Tests für UserService - Business Logic Layer
// Testet User-Management-Logik (Registrierung, Login, Token-Validierung)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    // Test: Erfolgreiche User-Registrierung
    @Test
    void registerTest() {
        String username = "newuser";
        String password = "securepass";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        doNothing().when(userRepository).save(any(User.class));

        User result = userService.register(username, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertNotEquals(password, result.getPassword());
        verify(userRepository).findByUsername(username);
        verify(userRepository).save(any(User.class));
    }

    // Test: Registrierung mit existierendem Username wirft Exception
    @Test
    void registerWithExistingUsernameTest() {
        String username = "existing";
        User existingUser = new User(username, "hashedpass");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.register(username, "password")
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).save(any(User.class));
    }

    // Test: Registrierung mit leerem Username wirft Exception
    @Test
    void registerWithEmptyUsernameTest() {
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.register("", "password")
        );

        assertEquals("Username cannot be empty", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // Test: Login mit korrekten Credentials gibt Token zurück
    @Test
    void loginTest() {
        String username = "testuser";
        String password = "testpass";
        // SHA-256 Hash von "testpass"
        String hashedPassword = "13d249f2cb4127b40cfa757866850278793f814ded3c587fe5889e889a7a9f6c";

        User mockUser = new User(username, hashedPassword);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        doNothing().when(userRepository).updateToken(eq(username), anyString());

        String token = userService.login(username, password);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertFalse(token.contains("mrpToken"));
        assertTrue(token.contains("-"));
        verify(userRepository).findByUsername(username);
        verify(userRepository).updateToken(eq(username), anyString());
    }

    // Test: Login mit falschem Password wirft Exception
    @Test
    void loginWithWrongPasswordTest() {
        String username = "testuser";
        String wrongPassword = "wrongpass";
        // SHA-256 Hash von "testpass"
        String hashedPassword = "13d249f2cb4127b40cfa757866850278793f814ded3c587fe5889e889a7a9f6c";

        User mockUser = new User(username, hashedPassword);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.login(username, wrongPassword)
        );

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).updateToken(anyString(), anyString());
    }

    // Test: Token-Validierung mit gültigem Token gibt User zurück
    @Test
    void validateTokenTest() {
        String token = "valid-token-123";
        User mockUser = new User("testuser", "hashedpass");
        mockUser.setToken(token);

        when(userRepository.findByToken(token)).thenReturn(Optional.of(mockUser));

        Optional<User> result = userService.validateToken(token);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByToken(token);
    }

    // Test: Token-Validierung mit ungültigem Token gibt leeres Optional zurück
    @Test
    void validateTokenWithInvalidTokenTest() {
        String invalidToken = "invalid-token";

        when(userRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        Optional<User> result = userService.validateToken(invalidToken);

        assertFalse(result.isPresent());
        verify(userRepository).findByToken(invalidToken);
    }
}
