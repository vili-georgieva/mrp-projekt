package org.example.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIf;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.ConnectException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

// Integration Tests die auf echte HTTP Routes zugreifen
// WICHTIG: Server muss vorher manuell gestartet werden (Main.java)
// WICHTIG: Docker PostgreSQL muss laufen (docker-compose up -d)
// Tests werden automatisch übersprungen wenn Server nicht läuft
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RouteIntegrationTest {

    private static HttpClient client;
    private static final String BASE_URL = "http://localhost:8080";
    private static String authToken = "";
    private static String testUsername;
    private static boolean serverRunning = false;

    @BeforeAll
    static void setUp() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        testUsername = "integrationtest_" + System.currentTimeMillis();

        // Prüft ob Server läuft
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/media"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
            serverRunning = true;
        } catch (Exception e) {
            serverRunning = false;
            System.out.println("WARNING: Server not running, integration tests will be skipped");
        }
    }

    static boolean isServerRunning() {
        return serverRunning;
    }

    // Test: POST /api/users/register - User-Registrierung via HTTP Route
    @Test
    @Order(1)
    @EnabledIf("isServerRunning")
    void registerUserViaRouteTest() throws Exception {
        String requestBody = "{\"username\":\"" + testUsername + "\",\"password\":\"testpass123\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Status 201 = Created
        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("registered"));
    }

    // Test: POST /api/users/login - User-Login via HTTP Route
    @Test
    @Order(2)
    @EnabledIf("isServerRunning")
    void loginUserViaRouteTest() throws Exception {
        String requestBody = "{\"username\":\"" + testUsername + "\",\"password\":\"testpass123\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Status 200 = OK
        assertEquals(200, response.statusCode());

        // Token Format: einfacher Token String
        authToken = response.body().replace("\"", "");
        assertFalse(authToken.isEmpty());
    }

    // Test: GET /api/media - Media abrufen via HTTP Route (kein Token erforderlich)
    @Test
    @Order(3)
    @EnabledIf("isServerRunning")
    void getMediaViaRouteTest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/media"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Status 200 = OK, Response ist JSON Array
        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("["));
    }

    // Test: POST /api/media ohne Token liefert 401 Unauthorized
    @Test
    @Order(4)
    @EnabledIf("isServerRunning")
    void createMediaWithoutTokenViaRouteTest() throws Exception {
        String requestBody = "{\"title\":\"Test Movie\",\"mediaType\":\"MOVIE\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/media"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Status 401 = Unauthorized
        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Unauthorized"));
    }

    // Test: GET /api/users/{username} - User-Profil via HTTP Route
    @Test
    @Order(5)
    @EnabledIf("isServerRunning")
    void getUserProfileViaRouteTest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/" + testUsername))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Status 200 = OK
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains(testUsername));
    }

    // Test: POST /api/users/login mit falschen Credentials liefert 401
    @Test
    @Order(6)
    @EnabledIf("isServerRunning")
    void loginWithWrongCredentialsViaRouteTest() throws Exception {
        String requestBody = "{\"username\":\"" + testUsername + "\",\"password\":\"wrongpassword\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Status 401 = Unauthorized
        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Invalid"));
    }
}
