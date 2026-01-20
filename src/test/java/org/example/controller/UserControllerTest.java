package org.example.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.example.model.User;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpExchange exchange;

    private UserController userController;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
        responseBody = new ByteArrayOutputStream();
    }

    //Test: Erfolgreiche User-Registrierung
    @Test
    void handleRegisterTest() throws Exception {
        String requestBody = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        User mockUser = new User("testuser", "hashedpassword");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(userService.register("testuser", "testpass")).thenReturn(mockUser);

        userController.handleRegister(exchange);

        verify(exchange).sendResponseHeaders(eq(201), anyLong());
        verify(userService).register("testuser", "testpass");
        assertTrue(responseBody.toString().contains("User registered successfully"));
    }

    // Test: Registrierung mit existierendem Username schlägt fehl
    @Test
    void handleRegisterWithExistingUsernameTest() throws Exception {
        String requestBody = "{\"username\":\"existing\",\"password\":\"pass\"}";

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(userService.register("existing", "pass"))
            .thenThrow(new IllegalArgumentException("Username already exists"));

        userController.handleRegister(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString().contains("Username already exists"));
    }

    // Test: Erfolgreicher Login gibt Token zurück
    @Test
    void handleLoginTest() throws Exception {
        String requestBody = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        String mockToken = "abc123-xyz789-token";

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(userService.login("testuser", "testpass")).thenReturn(mockToken);

        userController.handleLogin(exchange);

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        verify(userService).login("testuser", "testpass");
        assertTrue(responseBody.toString().contains(mockToken));
    }

    // Test: Login mit falschen Credentials schlägt fehl
    @Test
    void handleLoginWithInvalidCredentialsTest() throws Exception {
        String requestBody = "{\"username\":\"testuser\",\"password\":\"wrongpass\"}";

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(userService.login("testuser", "wrongpass"))
            .thenThrow(new IllegalArgumentException("Invalid username or password"));

        userController.handleLogin(exchange);

        verify(exchange).sendResponseHeaders(eq(401), anyLong());
        assertTrue(responseBody.toString().contains("Invalid username or password"));
    }
}
