# Protocol - Media Ratings Platform (MRP)

## Student Information
- **Name:** Velichka Georgieva
- **Student ID:** if24b265
- **Date:** January 25, 2026
- **Submission:** Final

## Project Overview
Implementation of a RESTful HTTP server for a Media Ratings Platform using pure Java HTTP libraries (HttpServer) without web frameworks like Spring or JSP.

## Implemented Features

### Core Features
- **User Management**: Registrierung, Login, Token-basierte Authentifizierung
- **Media Management**: CRUD-Operationen (Create, Read, Update, Delete) mit Ownership-Validierung
- **Model Classes**: User, MediaEntry, Rating, MediaType (enum)
- **HTTP Server**: Pure Java HttpServer auf Port 8080
- **Database**: PostgreSQL mit Schema und Foreign Keys
- **SQL Injection Protection**: Alle Queries verwenden PreparedStatements

### Rating System
- Erstellen/Aktualisieren von Ratings (1-5 Sterne) mit Kommentar
- Ein Rating pro User pro Media (UNIQUE Constraint, editierbar)
- Like-Funktion für Ratings
- Kommentar-Moderation (confirmed Flag vor öffentlicher Sichtbarkeit)
- Löschen eigener Ratings
- Separates Update/Löschen von Kommentaren
- Rating-Historie pro User
- Automatische Durchschnitts-Berechnung pro Media

### Favorites System
- Media zu Favoriten hinzufügen
- Media aus Favoriten entfernen
- Toggle Favorite Status (hinzufügen/entfernen in einem Call)
- Alle User-Favoriten mit vollständigen Media-Details auflisten
- Prüfen ob Media ein Favorit ist

### Profile und Statistics
- User-Profil mit Statistiken anzeigen
- Statistiken beinhalten: Media-Anzahl, Rating-Anzahl, Favorite-Anzahl, durchschnittliche vergebene Sterne
- Profil bearbeiten (Passwort ändern)

### Search und Filter
- Media nach Teil-Titel suchen
- Filtern nach Genre, Media-Typ, Altersfreigabe
- Mehrere Filter kombinierbar
- Dynamischer SQL-Query-Aufbau

### Business Logic
- **Ownership Validation**: Nur Creator kann Media ändern/löschen
- **One Rating per User**: Durch UNIQUE Constraint erzwungen, erlaubt Updates
- **Comment Moderation**: Kommentare erst nach Bestätigung öffentlich
- **Average Rating**: Automatisch berechnet und pro Media gespeichert

### Advanced Features
- **Leaderboard**: Top User nach Rating-Anzahl mit Ranking
- **Recommendation System**: Genre-basierte Empfehlungen basierend auf hoch bewerteten Media (4-5 Sterne), schließt bereits bewertete Media aus

### API Endpoints

#### User Management (4 Endpoints)
- POST /api/users/register
- POST /api/users/login
- GET /api/users/{username}
- PUT /api/users/{username}

#### Media Management (5 Endpoints)
- GET /api/media (mit optionalen Search/Filter Parametern)
- GET /api/media/{id}
- POST /api/media
- PUT /api/media/{id}
- DELETE /api/media/{id}

#### Rating System (9 Endpoints)
- POST /api/media/{id}/ratings
- GET /api/media/{id}/ratings
- DELETE /api/ratings/{id}
- PUT /api/ratings/{id}
- PATCH /api/ratings/{id}/comment
- DELETE /api/ratings/{id}/comment
- POST /api/ratings/{id}/like
- POST /api/ratings/{id}/confirm
- GET /api/users/{username}/rating-history

#### Favorites (5 Endpoints)
- POST /api/users/{username}/favorites/{mediaId}
- DELETE /api/users/{username}/favorites/{mediaId}
- POST /api/users/{username}/favorites/{mediaId}/toggle
- GET /api/users/{username}/favorites
- GET /api/users/{username}/favorites/check/{mediaId}

#### Leaderboard und Recommendations (2 Endpoints)
- GET /api/leaderboard?limit={n}
- GET /api/recommendations?username={user}&limit={n}

### Non-Functional Requirements
- **Security**: Token-basierte Authentifizierung (UUID Tokens)
- **Password Security**: SHA-256 Hashing
- **Data Persistence**: PostgreSQL in Docker
- **Testing**: 52 Unit Tests (JUnit 5 + Mockito)
- **Integration Testing**: Curl Scripts für alle Endpoints
- **Documentation**: Vollständige README.md und protocol.md
- **SOLID Principles**: Klare Implementierung mit Beispielen
- **Clean Architecture**: Controller-Service-Repository Pattern

## Technical Architecture

### Architecture Decisions

#### 1. Layered Architecture
Das Projekt folgt einer klassischen 3-Schichten-Architektur:
- **Controller Layer**: Verarbeitet HTTP Requests/Responses
- **Service Layer**: Implementiert Business Logic
- **Repository Layer**: Verwaltet Datenbank-Operationen

Diese Trennung gewährleistet:
- Klare Separation of Concerns
- Einfache Testbarkeit
- Wartbarkeit und Skalierbarkeit

#### 2. Technology Stack
- **Java 21**: Moderne LTS-Version mit aktuellen Features
- **HttpServer (com.sun.net.httpserver)**: Pure HTTP-Implementierung ohne Web-Frameworks
- **PostgreSQL**: Robuste relationale Datenbank für Datenpersistenz
- **Jackson**: Industry-Standard JSON Serialisierung/Deserialisierung

### Class Diagram

```
┌─────────────────┐
│   Main.java     │
└────────┬────────┘
         │ creates
         ▼
┌─────────────────┐
│  RestServer     │
└────────┬────────┘
         │ initializes
         ▼
┌──────────────────────────────────────────┐
│  Controllers                              │
│  ├─ UserController                        │
│  ├─ MediaController                       │
│  ├─ RatingController                      │
│  ├─ FavoriteController                    │
│  ├─ LeaderboardController                 │
│  └─ RecommendationController              │
└────────┬─────────────────────────────────┘
         │ uses
         ▼
┌──────────────────────────────────────────┐
│  Services (Business Logic)                │
│  ├─ UserService                           │
│  ├─ MediaService                          │
│  ├─ RatingService                         │
│  ├─ FavoriteService                       │
│  ├─ LeaderboardService                    │
│  └─ RecommendationService                 │
└────────┬─────────────────────────────────┘
         │ uses
         ▼
┌──────────────────────────────────────────┐
│  Repositories (Data Access)               │
│  ├─ UserRepository                        │
│  ├─ MediaRepository                       │
│  ├─ RatingRepository                      │
│  └─ FavoriteRepository                    │
└────────┬─────────────────────────────────┘
         │ uses
         ▼
┌──────────────────────────────────────────┐
│  PostgreSQL Database                      │
└──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│  Models (Domain Objects)                  │
│  ├─ User                                  │
│  ├─ MediaEntry                            │
│  ├─ Rating                                │
│  └─ MediaType (enum)                      │
└──────────────────────────────────────────┘
```

## Implementation Details

### 1. HTTP Server Implementation
**File:** `RestServer.java`

Der Server verwendet Javas eingebaute `HttpServer` Klasse um einen reinen HTTP-Server ohne Web-Framework zu erstellen:
- Hört auf Port 8080
- Routet Requests zu entsprechenden Controllern
- Verwaltet Datenbank-Connection Lifecycle
- Verwendet Default Thread Executor für parallele Requests

### 2. Routing Strategy
**Implementation:** Context-basiertes Routing mit HttpServer Contexts

Jeder Endpoint wird als separater Context registriert:
```java
server.createContext("/api/users/register", userController::handleRegister);
server.createContext("/api/users/login", userController::handleLogin);
server.createContext("/api/media", mediaController::handleMedia);
```

Controller parsen HTTP-Methode und Pfad um die spezifische Operation zu bestimmen.

### 3. Authentication System
**Implementation:** Token-basierte Authentifizierung

**Flow:**
1. User registriert sich mit Username/Passwort
2. User loggt ein und erhält Token (doppelte UUID)
3. Token wird in Datenbank gespeichert
4. Folgende Requests enthalten Token im `Authorization: Bearer {token}` Header
5. Server validiert Token vor Verarbeitung geschützter Endpoints

### 4. Database Schema

**Users Table:**
```sql
CREATE TABLE users (
    username VARCHAR(255) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    token VARCHAR(255)
);
```

**Media Entries Table:**
```sql
CREATE TABLE media_entries (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    media_type VARCHAR(50) NOT NULL,
    release_year INTEGER,
    genres TEXT,
    age_restriction INTEGER,
    creator VARCHAR(255) NOT NULL,
    FOREIGN KEY (creator) REFERENCES users(username) ON DELETE CASCADE
);
```

**Ratings Table:**
```sql
CREATE TABLE ratings (
    id SERIAL PRIMARY KEY,
    media_id INTEGER NOT NULL,
    username VARCHAR(255) NOT NULL,
    stars INTEGER NOT NULL CHECK (stars >= 1 AND stars <= 5),
    comment TEXT,
    confirmed BOOLEAN DEFAULT FALSE,
    likes INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (media_id) REFERENCES media_entries(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    UNIQUE(media_id, username)
);
```

**Favorites Table:**
```sql
CREATE TABLE favorites (
    username VARCHAR(255) NOT NULL,
    media_id INTEGER NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (username, media_id),
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (media_id) REFERENCES media_entries(id) ON DELETE CASCADE
);
```

**Key Design Decisions:**
- UNIQUE Constraint auf (media_id, username) in ratings stellt ein Rating pro User pro Media sicher
- CASCADE Deletes erhalten referentielle Integrität
- CHECK Constraint validiert Star-Rating Bereich (1-5)
- confirmed Flag ermöglicht Kommentar-Moderation

### 5. HTTP Response Codes
Korrekte HTTP Status Codes sind implementiert:
- **200 OK**: Erfolgreiche GET/PUT Requests
- **201 Created**: Erfolgreiche POST Requests (User-Registrierung, Media-Erstellung)
- **204 No Content**: Erfolgreiche DELETE Requests
- **400 Bad Request**: Ungültige Eingabedaten
- **401 Unauthorized**: Fehlende oder ungültige Authentifizierung
- **403 Forbidden**: Gültige Auth aber unzureichende Berechtigungen
- **404 Not Found**: Ressource existiert nicht
- **405 Method Not Allowed**: Falsche HTTP-Methode
- **500 Internal Server Error**: Datenbank- oder Server-Fehler

### 6. JSON Serialization
Jackson Library verarbeitet alle JSON-Operationen:
- Automatische Serialisierung von Java-Objekten zu JSON
- Deserialisierung von JSON Request Bodies zu Java-Objekten
- Support für Java 8 Date/Time Types (LocalDateTime)

## SOLID Principles Implementation

### Single Responsibility Principle (SRP)
Jede Klasse hat eine klare Verantwortung:
- **Controllers**: Verarbeiten nur HTTP-Kommunikation
- **Services**: Implementieren Business Logic und Validierung
- **Repositories**: Verwalten nur Datenbank-Operationen
- **Models**: Repräsentieren Domain-Daten

### Open/Closed Principle (OCP)
- Services verwenden Dependency Injection, erlauben Erweiterung ohne Modifikation
- Neue Media-Typen können zum Enum hinzugefügt werden ohne bestehenden Code zu ändern
- Zusätzliche Repositories können nach demselben Pattern erstellt werden

### Liskov Substitution Principle (LSP)
- Repository-Implementierungen können ausgetauscht werden (z.B. für verschiedene Datenbanken)
- Services arbeiten mit Abstraktionen, nicht konkreten Implementierungen

### Interface Segregation Principle (ISP)
- Service-Methoden sind fokussiert und spezifisch
- Controller exponieren nur notwendige Methoden
- Keine aufgeblähten Interfaces mit ungenutzten Methoden

### Dependency Inversion Principle (DIP)
- High-Level Module (Controllers) hängen von Abstraktionen (Services) ab
- Dependencies werden via Konstruktoren injiziert

## Integration Tests

### Postman Collection
Eine vollständige Postman Collection ist bereitgestellt (`MRP_Postman_Collection.json`) mit folgenden Requests:
- User Registration
- User Login
- Get User Profile
- Create Media Entry
- Get All Media
- Get Media by ID
- Update Media Entry
- Delete Media Entry
- Rating Operations
- Favorite Operations
- Leaderboard
- Recommendations

### curl Scripts
Automatisierte curl Scripts testen alle Endpoints:
- `test_all_endpoints.sh`: Master Test Script
- `test_api.sh`: Basis API Tests
- `test_search.sh`: Such- und Filter-Tests
- `test_leaderboard.sh`: Leaderboard Tests
- `test_recommendations.sh`: Empfehlungs-Tests

## Problems Encountered and Solutions

### Problem 1: Routing mit HttpServer
**Issue:** Javas HttpServer hat kein eingebautes Routing wie Spring.

**Solution:** 
- Context Paths für Basis-Routing verwendet
- Custom Path Parsing in Controllern implementiert
- Logik nach HTTP-Methode aufgeteilt (GET, POST, PUT, DELETE)

### Problem 2: JSON Handling
**Issue:** Manuelles JSON Parsing wäre fehleranfällig.

**Solution:** 
- Jackson Library für automatische Serialisierung integriert
- Datatype Module für LocalDateTime Support hinzugefügt
- DTOs für Request/Response Handling erstellt

### Problem 3: Database Connection Management
**Issue:** Single Connection über Repositories teilen.

**Solution:**
- DatabaseConnection Utility-Klasse mit Transaction-Management erstellt
- executeInTransaction() Methode für konsistente Fehlerbehandlung
- Automatisches Rollback bei Exceptions

### Problem 4: Token Storage
**Issue:** Entscheiden wo Authentication Tokens gespeichert werden.

**Solution:**
- In Datenbank gespeichert (persistent über Neustarts)
- Doppelte UUID als Format (sicher und unvorhersagbar)
- Einfache Validierung mit Datenbank-Query
- Token-Invalidierung möglich (Logout-Funktionalität)

## Git Repository
GitHub Link: https://github.com/vili-georgieva/mrp-projekt

## Unit Testing Strategy and Coverage

### Test Distribution
Das Projekt enthält 52 Unit Tests verteilt über die Layer:
- **Service Layer (BLL)**: 27 Tests (52%)
- **Controller Layer (PL)**: 19 Tests (37%)
- **Integration Tests (HTTP Routes)**: 6 Tests (11%, übersprungen wenn Server nicht läuft)

Diese Verteilung folgt der Empfehlung des Lecturers:
- Starker Fokus auf Business Logic Layer (Service Tests)
- Gute Coverage auf Presentation Layer (Controller Tests)
- Integration Tests validieren HTTP Routing und End-to-End Flows

**Test Results:**
- 46 Tests laufen erfolgreich (alle bestanden)
- 6 Integration Tests übersprungen wenn Server nicht läuft
- 0 Failures, 0 Errors
- Vollständige Coverage aller kritischen Business Logic

### Test Categories

#### UserServiceTest (7 Tests)
- `registerTest`: Validiert erfolgreiche User-Registrierung mit Password Hashing
- `registerWithExistingUsernameTest`: Stellt Duplikat-Username-Verhinderung sicher
- `registerWithEmptyUsernameTest`: Validiert Input-Validierung
- `loginTest`: Verifiziert Login-Flow und UUID Token-Generierung
- `loginWithWrongPasswordTest`: Testet falsches Passwort Ablehnung
- `validateTokenTest`: Testet Token-Validierungs-Logik
- `validateTokenWithInvalidTokenTest`: Stellt sicher dass ungültige Tokens abgelehnt werden

#### MediaServiceTest (11 Tests)
- `createMediaTest`: Validiert Media-Erstellung mit Creator-Zuweisung
- `createMediaWithEmptyTitleTest`: Testet Titel-Validierung
- `createMediaWithoutMediaTypeTest`: Testet Media-Type-Validierung
- `updateMediaTest`: Verifiziert Ownership-Check bei Updates
- `updateMediaByDifferentUserTest`: Stellt sicher nur Owner kann updaten
- `updateNonexistentMediaTest`: Testet Fehlerbehandlung für fehlende Media
- `deleteMediaTest`: Validiert Löschung mit Ownership-Check
- `deleteMediaByDifferentUserTest`: Stellt sicher nur Owner kann löschen
- `getAllMediaTest`: Testet Abruf aller Media-Einträge
- `getMediaByIdTest`: Testet Abruf nach ID
- `getMediaByIdNotFoundTest`: Testet Behandlung nicht existierender Media

#### RatingServiceTest (5 Tests)
- `createRatingWithInvalidStarsTest`: Validiert Star-Value Constraints (< 1)
- `createRatingWithTooManyStarsTest`: Testet Upper Bound Validierung (> 5)
- `createRatingWithMinStarsTest`: Testet minimale gültige Stars (1)
- `createRatingWithMaxStarsTest`: Testet maximale gültige Stars (5)
- `createRatingWithNullCommentTest`: Testet Null-Kommentar Behandlung

#### FavoriteServiceTest (4 Tests)
- `addFavoriteTest`: Testet Hinzufügen von Media zu Favoriten
- `addFavoriteAlreadyExistsTest`: Testet Duplikat-Favorit-Verhinderung
- `toggleFavoriteAddTest`: Testet Toggle zum Hinzufügen
- `toggleFavoriteRemoveTest`: Testet Toggle zum Entfernen

#### UserControllerTest (4 Tests)
- `handleRegisterTest`: Validiert HTTP POST für Registrierung (201)
- `handleRegisterWithExistingUsernameTest`: Testet Duplikat-User (400)
- `handleLoginTest`: Testet HTTP POST für Login (200)
- `handleLoginWithInvalidCredentialsTest`: Testet ungültigen Login (401)

#### MediaControllerTest (6 Tests)
- `handleGetAllMediaTest`: Testet GET /api/media (200)
- `handleCreateMediaWithoutTokenTest`: Testet unauthorized Create (401)
- `handleCreateMediaWithValidTokenTest`: Testet authorized Create (201)
- `handleDeleteMediaWithoutTokenTest`: Testet unauthorized Delete (401)
- `handleUpdateMediaWithoutTokenTest`: Testet unauthorized Update (401)
- `handleUnsupportedMethodTest`: Testet ungültige Methode (405)

#### RatingControllerTest (5 Tests)
- `handleGetRatingsForMediaTest`: Testet GET Ratings (200)
- `handleCreateRatingWithoutTokenTest`: Testet unauthorized Rating (401)
- `handleCreateRatingWithValidTokenTest`: Testet authorized Rating (201)
- `handleInvalidMediaIdTest`: Testet ungültige ID (400)
- `handleUnsupportedMethodTest`: Testet ungültige Methode (405)

#### FavoriteControllerTest (4 Tests)
- `handleGetFavoritesWithoutTokenTest`: Testet unauthorized Access (401)
- `handleAddFavoriteWithoutTokenTest`: Testet unauthorized Add (401)
- `handleToggleFavoriteWithValidTokenTest`: Testet Toggle mit Token (200)
- `handleCheckFavoriteTest`: Testet Check Favorite (200)

#### RouteIntegrationTest (6 Tests)
- `registerUserViaRouteTest`: Testet echten HTTP POST /api/users/register
- `loginUserViaRouteTest`: Testet echten HTTP POST /api/users/login
- `getMediaViaRouteTest`: Testet echten HTTP GET /api/media
- `createMediaWithoutTokenViaRouteTest`: Testet unauthorized Access (401)
- `getUserProfileViaRouteTest`: Testet echten HTTP GET /api/users/{username}
- `loginWithWrongCredentialsViaRouteTest`: Testet falsche Credentials (401)

**Hinweis:** Integration Tests werden übersprungen wenn der Server nicht läuft, sodass Unit Tests unabhängig laufen können.

### Mockito Strategy
Mockito wird extensiv verwendet um Unit Tests zu isolieren:
- **Mock Repositories** in Service Tests um Datenbank-Abhängigkeit zu vermeiden
- **Mock Services** in Controller Tests um auf HTTP Handling zu fokussieren
- `when().thenReturn()` für Method Stubbing
- `verify()` um korrekte Methodenaufrufe sicherzustellen
- `never()` um zu verifizieren dass Methoden in Fehlerfällen nicht aufgerufen werden

Beispiel aus MediaServiceTest:
```java
@Mock
private MediaRepository mediaRepository;

@Test
void createMediaTest() {
    when(mediaRepository.save(any(MediaEntry.class))).thenReturn(1);
    MediaEntry result = mediaService.createMedia(media, testUser);
    verify(mediaRepository).save(any(MediaEntry.class));
}
```

### Naming Convention
Tests folgen konsistenter Benennung:
- Test-Klassen: `{ClassName}Test.java`
- Test-Methoden: `{functionality}Test()`
- Klare, beschreibende Namen die anzeigen was getestet wird

### Test Pattern
Alle Tests folgen Arrange-Act-Assert Pattern:
```java
@Test
void registerWithEmptyUsernameTest() {
    // Arrange
    when(userRepository.findByUsername("")).thenReturn(Optional.empty());
    
    // Act und Assert
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.register("", "password")
    );
    
    assertEquals("Username cannot be empty", exception.getMessage());
}
```

### Test Coverage Focus
Tests priorisieren:
- **Business Logic Validierung**: Input Validierung, Constraints
- **Error Handling**: Exception Cases, Edge Cases
- **Security Checks**: Ownership Validierung, Authentifizierung
- **State Changes**: Datenpersistenz, Updates

Tests decken nicht ab:
- Datenbank-Operationen (wären Integration Tests)
- JSON Serialisierung (Jackson Verantwortung)
- HTTP Protocol Details (HttpServer Verantwortung)

---

## SOLID Principles in Detail

Die Codebase demonstriert klare Einhaltung der SOLID Prinzipien. Hier sind konkrete Beispiele aus dem Projekt:

### 1. Single Responsibility Principle (SRP)

Jede Klasse hat genau einen Grund sich zu ändern.

#### Beispiel: UserController
**File:** `UserController.java`

**Responsibility:** Nur HTTP Requests und Responses verarbeiten

```java
public class UserController {
    private final UserService userService;
    private final ObjectMapper objectMapper;
    
    public void handleRegister(HttpExchange exchange) throws IOException {
        // Verarbeitet nur: HTTP Request parsen, Service aufrufen, HTTP Response senden
        String body = new String(exchange.getRequestBody().readAllBytes());
        RegisterRequest request = objectMapper.readValue(body, RegisterRequest.class);
        User user = userService.register(request.getUsername(), request.getPassword());
        sendResponse(exchange, 201, "User registered successfully");
    }
}
```

**Keine Business Logic hier** - nur HTTP Handling.

#### Beispiel: UserService
**File:** `UserService.java`

**Responsibility:** Business Logic und Validierung implementieren

```java
public class UserService {
    private final UserRepository userRepository;
    
    public User register(String username, String password) {
        // Business Logic: Validierung, Password Hashing
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword);
        userRepository.save(user);
        return user;
    }
}
```

**Kein HTTP Handling, kein SQL** - nur Business Logic.

#### Beispiel: UserRepository
**File:** `UserRepository.java`

**Responsibility:** Nur Datenbank-Operationen verwalten

```java
public class UserRepository {
    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash, token) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getToken());
            stmt.executeUpdate();
        }
    }
}
```

**Keine Validierung, kein HTTP** - nur Datenbank-Zugriff.

**Ergebnis:** Jede Schicht kann unabhängig geändert werden. Wechsel von PostgreSQL zu MySQL betrifft nur Repositories. Wechsel von HTTP zu WebSockets betrifft nur Controller.

### 2. Dependency Inversion Principle (DIP)

High-Level Module hängen von Abstraktionen (injizierten Dependencies) ab, nicht von konkreten Implementierungen.

#### Beispiel: Service mit Constructor Injection
**File:** `UserService.java`

**Before (Bad):**
```java
public class UserService {
    private UserRepository userRepository;
    
    public UserService() {
        this.userRepository = new UserRepository(); // Hard-coded Dependency
    }
}
```

**After (Good):**
```java
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository; // Injizierte Dependency
    }
}
```

**Benefits:**
- Einfach zu testen: kann Mock Repository injizieren
- Kann Implementierungen austauschen ohne UserService zu ändern
- Folgt Dependency Inversion Principle

#### Beispiel: Testing mit Mock
**File:** `UserServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository; // Mock injiziert
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository); // Mock injizieren
    }
    
    @Test
    void registerTest() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        userService.register("user", "pass");
        verify(userRepository).save(any(User.class)); // Interaktion verifizieren
    }
}
```

**Ergebnis:** Tests laufen ohne Datenbank. Services sind von Repositories entkoppelt.

### 3. Open/Closed Principle (OCP)

Software-Entitäten sollten offen für Erweiterung, geschlossen für Modifikation sein.

#### Beispiel: MediaType Enum
**File:** `MediaType.java`

```java
public enum MediaType {
    MOVIE,
    SERIES,
    GAME
}
```

**Neue Media-Typen hinzufügen:**
```java
public enum MediaType {
    MOVIE,
    SERIES,
    GAME,
    BOOK,      // Neuer Typ hinzugefügt
    PODCAST    // Ein weiterer neuer Typ
}
```

**Keine Änderungen nötig in:**
- MediaService (funktioniert mit jedem MediaType)
- MediaRepository (speichert Typ als String)
- MediaController (akzeptiert jeden gültigen Enum-Wert)

**Ergebnis:** System kann erweitert werden ohne bestehenden Code zu modifizieren.

### 4. Weitere SOLID Prinzipien

#### Liskov Substitution Principle (LSP)
Nicht stark demonstriert aufgrund fehlender Vererbung, aber:
- Repository-Implementierungen könnten ausgetauscht werden
- Verschiedene Datenbank-Implementierungen würden identisch funktionieren

#### Interface Segregation Principle (ISP)
- Service-Methoden sind fokussiert und spezifisch
- Controller exponieren nur notwendige HTTP-Methoden
- Keine aufgeblähten Interfaces die Implementierung ungenutzter Methoden erzwingen

**Beispiel:** RatingService erzwingt nicht Implementierung von Media-Management-Methoden.

### SOLID Benefits Realized

1. **Testability**: Einfach Unit Tests mit Mocks zu schreiben
2. **Maintainability**: Änderungen sind auf spezifische Layer lokalisiert
3. **Scalability**: Neue Features können hinzugefügt werden ohne bestehenden Code zu brechen
4. **Clarity**: Jede Klasse hat einen klaren, einzelnen Zweck

---

## Time Tracking

### Estimated vs. Actual Time

| Phase | Estimated | Actual | Notes |
|-------|-----------|--------|-------|
| Project Setup und Planning | 2h | 3h | Setup dauerte länger (Docker, PostgreSQL, Maven) |
| Database Schema Design | 3h | 4h | Mehrere Iterationen bei Foreign Keys und Constraints |
| Repository Layer | 4h | 5h | Transaction Handling komplexer als erwartet |
| Service Layer (Basic) | 3h | 4h | Password Hashing und Validierung brauchten Zeit |
| Controller Layer (Basic) | 3h | 4h | HTTP Routing manueller als erwartet |
| Authentication System | 2h | 3h | Token Generation und Validation Logic |
| Unit Tests (Basic) | 4h | 5h | Mockito Setup hatte Lernkurve |
| **Intermediate Submission** | **21h** | **28h** | |
| Rating System | 3h | 4h | Average Rating Calculation Logic |
| Favorites System | 2h | 2h | Straightforward Implementation |
| Profile und Statistics | 1h | 1.5h | SQL Aggregation Queries |
| Search und Filter | 2h | 2.5h | Query Parameter Parsing |
| Leaderboard | 1h | 1h | Einfaches SQL mit GROUP BY |
| Recommendation System | 4h | 3h | Vereinfachter Genre-basierter Ansatz |
| Additional Unit Tests | 3h | 3h | Brachte Total auf 52 Tests |
| Integration Testing | 2h | 2h | Curl Scripts für alle Endpoints |
| Documentation (Protocol) | 2h | 2h | Dieses Dokument |
| **Final Submission** | **20h** | **21h** | |
| **TOTAL** | **41h** | **49h** | |

### Key Takeaways

**Overestimated:**
- Recommendation System (vereinfachter Ansatz war schneller)

**Underestimated:**
- Project Setup (Docker Konfiguration, Environment Setup)
- Repository Layer (Transaction Handling Komplexität)
- Unit Tests (Mockito Lernkurve)

**Most Time-Consuming:**
- Repository Layer: 5 Stunden
- Unit Tests: 8 Stunden total (5h + 3h)
- Service Layer: 4 Stunden

**Quickest Tasks:**
- Favorites System: 2 Stunden
- Leaderboard: 1 Stunde

### Development Phases

1. **Setup Phase** (3h): Environment, Tools, Dependencies
2. **Data Layer Phase** (9h): Database + Repositories
3. **Business Logic Phase** (8h): Services + Validation
4. **API Layer Phase** (7h): Controllers + Routing
5. **Feature Phase** (10h): Rating, Favorites, Search, Recommendations
6. **Testing Phase** (8h): Unit Tests + Integration Tests
7. **Documentation Phase** (4h): README, Protocol, Comments

**Total Development Time: 49 Stunden über 4 Wochen**

---

## Submission Completeness

### Delivered Files
- **Source Code**: Vollständiges Java-Projekt mit Maven-Konfiguration
- **README.md**: User-Dokumentation mit API-Referenz und Quick Start Guide
- **protocol.md**: Dieser Entwicklungsbericht mit Architektur, Testing und Reflections
- **docker-compose.yml**: PostgreSQL Datenbank-Konfiguration
- **MRP_Postman_Collection.json**: Vollständige Postman Collection für alle Endpoints
- **Integration Test Scripts**: test_all_endpoints.sh, test_api.sh, test_search.sh, test_leaderboard.sh, test_recommendations.sh

### GitHub Repository
- **Link**: https://github.com/vili-georgieva/mrp-projekt
- **Commit History**: Vollständige Entwicklungshistorie mit aussagekräftigen Commit Messages
- **Branches**: Main Branch mit stabilem Code

### Requirements Fulfilled
- Java Implementation
- Pure HTTP Server (keine Web-Frameworks)
- PostgreSQL Datenbank in Docker
- SQL Injection Prevention (PreparedStatements)
- Kein OR-Mapping Library
- 52 Unit Tests (Anforderung: Minimum 20)
- Token-basierte Authentifizierung
- Alle funktionalen Anforderungen implementiert
- Alle Business Logic implementiert
- Vollständige Dokumentation
- Integration Tests (Curl Scripts)
- SOLID Prinzipien demonstriert
- Time Tracking enthalten
- Lessons Learned dokumentiert

### Presentation Readiness
- Funktionierende Lösung getestet und bereit
- Docker Environment konfiguriert
- Postman Collection vorbereitet
- Architektur-Diagramme verfügbar
- Demo-Daten können via Test Scripts erstellt werden

---

## Final Notes

Dieses Projekt demonstriert erfolgreich eine vollständige RESTful API Implementation mit pure Java HTTP Libraries ohne Web-Frameworks. Alle geforderten Features sind implementiert, getestet und dokumentiert.

Die finale Applikation beinhaltet:
- **52 Unit Tests** die Business Logic abdecken (46 aktive + 6 Integration Tests)
- **6 Controllers**: User, Media, Rating, Favorite, Leaderboard, Recommendation
- **6 Services**: Vollständige Business Logic Layer
- **4 Repositories**: Datenbank-Zugriff mit PreparedStatements (SQL Injection Protection)
- Integration Tests via Curl Scripts
- Docker-basierte PostgreSQL Datenbank
- Token-basierte Authentifizierung (doppelte UUID)
- Vollständige CRUD-Operationen für Users und Media
- **Rating System** mit Confirmation, Likes und Comment Moderation
- **Favorites System** mit Toggle-Funktionalität
- **Search und Filter** Funktionalität (Titel, Genre, Typ, Altersfreigabe)
- **Leaderboard** (Top User nach Rating-Anzahl)
- **Genre-basiertes Recommendation System**
- **User Statistics** (Media Count, Rating Count, Average Stars, Favorites)

Der gesamte Code hält sich an SOLID Prinzipien und folgt Clean Architecture Patterns mit klarer Trennung zwischen Controller, Service und Repository Layers.
