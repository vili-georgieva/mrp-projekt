# Präsentations-Antworten - Media Ratings Platform (MRP)

## Ablauf der Präsentation

**Zu Beginn:**
- Routen vorzeigen und demonstrieren dass sie funktionieren
- Dann Fragen zu Implementation und Testing-Strategie

---

## Frage 1: Hast du das Projekt selbst gemacht?

**Antwort:** Ja, das Projekt wurde eigenständig entwickelt. Die Git-Historie zeigt die kontinuierliche Entwicklung über mehrere Wochen.

**Nachweis:**
- Git Repository mit vollständiger Commit-Historie
- Alle 6 Controller selbst implementiert
- 52 Unit Tests geschrieben
- Dokumentation eigenständig erstellt

---

## Frage 2: Wo confirmst du ein rating comment?

**Antwort:** Rating Comments werden über einen dedizierten Endpoint bestätigt.

### Implementation:

**Endpoint:**
```
POST /api/ratings/{ratingId}/confirm
```

**Code Location:**
- `RatingController.java` Zeile 95-101
- `RatingService.java` Zeile 113-119
- `RatingRepository.java` Zeile 215-228

**Code-Beispiel aus RatingController:**
```java
case "confirm":
    if (method.equals("POST")) {
        handleConfirmRating(exchange, ratingId);
    } else {
        sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
    }
    return;
```

**Code-Beispiel aus RatingService:**
```java
public boolean confirmRating(int ratingId) {
    return ratingRepository.confirmRating(ratingId);
}
```

**Code-Beispiel aus RatingRepository:**
```java
public boolean confirmRating(int ratingId) {
    return DatabaseConnection.executeInTransaction(conn -> {
        String sql = "UPDATE ratings SET confirmed = true WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ratingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    });
}
```

**Business Logic:**
- Rating hat ein `confirmed` Flag (Boolean)
- Default: `false` beim Erstellen
- Nur bestätigte Comments sind öffentlich sichtbar
- Moderation-Feature für Kommentare

**Datenbank:**
```sql
CREATE TABLE ratings (
    ...
    confirmed BOOLEAN DEFAULT FALSE,
    ...
);
```

---

## Frage 3: Wie bekommst du das Leaderboard?

**Antwort:** Das Leaderboard wird über SQL-Aggregation mit JOIN und GROUP BY geholt.

### Implementation:

**Endpoint:**
```
GET /api/leaderboard?limit=10
```

**Code Location:**
- `LeaderboardController.java` Zeile 56
- `LeaderboardService.java` Zeile 17-21
- `UserRepository.java` Zeile 202-228

**SQL Query aus UserRepository.getLeaderboard():**
```java
String sql = "SELECT u.username, COUNT(r.id) as rating_count " +
            "FROM users u " +
            "LEFT JOIN ratings r ON u.username = r.username " +
            "GROUP BY u.username " +
            "ORDER BY rating_count DESC " +
            "LIMIT ?";
```

**Erklärung:**
1. **LEFT JOIN** zwischen `users` und `ratings` Tabellen
2. **COUNT(r.id)** zählt Anzahl der Ratings pro User
3. **GROUP BY u.username** gruppiert nach User
4. **ORDER BY rating_count DESC** sortiert absteigend nach Anzahl
5. **LIMIT ?** begrenzt Ergebnisse (z.B. Top 10)

**Rückgabewert:**
```json
[
  {
    "rank": 1,
    "username": "poweruser",
    "ratingCount": 42
  },
  {
    "rank": 2,
    "username": "moviefan",
    "ratingCount": 38
  }
]
```

**PreparedStatement Schutz:**
```java
stmt.setInt(1, limit);  // SQL Injection-Schutz
```

---

## Frage 4: Wo likest du einen media entry?

**Antwort:** Man liked kein Media Entry direkt, sondern ein **Rating** (Bewertung).

### Implementation:

**Endpoint:**
```
POST /api/ratings/{ratingId}/like
```

**Code Location:**
- `RatingController.java` Zeile 87-93
- `RatingService.java` Zeile 97-100
- `RatingRepository.java` Zeile 200-213

**Code-Beispiel aus RatingController:**
```java
case "like":
    if (method.equals("POST")) {
        handleLikeRating(exchange, ratingId);
    } else {
        sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
    }
    return;
```

**Code-Beispiel aus RatingRepository:**
```java
public boolean likeRating(int ratingId) {
    return DatabaseConnection.executeInTransaction(conn -> {
        String sql = "UPDATE ratings SET likes = likes + 1 WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ratingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    });
}
```

**Erklärung:**
- Rating hat ein `likes` Feld (INTEGER)
- **SQL: `likes = likes + 1`** erhöht Counter atomar
- PreparedStatement verhindert SQL Injection

**Datenbank:**
```sql
CREATE TABLE ratings (
    ...
    likes INTEGER DEFAULT 0,
    ...
);
```

---

## Frage 5: Zeig Tests her. Wo mockst du? Was ist mocking?

**Antwort:** 52 Unit Tests vorhanden, hauptsächlich mit Mockito für Mocking.

### Was ist Mocking?

**Mocking** ist eine Test-Technik, bei der externe Abhängigkeiten durch Fake-Objekte (Mocks) ersetzt werden.

**Warum Mocking?**
- Isoliert die zu testende Komponente
- Tests ohne Datenbank-Abhängigkeit
- Schnellere Tests
- Kontrolle über Test-Szenarien

### Beispiel aus UserServiceTest:

**Code Location:** `UserServiceTest.java` Zeile 1-154

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;  // ← Mock erstellt
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);  // ← Mock injiziert
    }
    
    @Test
    void registerTest() {
        String username = "newuser";
        String password = "securepass";
        
        // ARRANGE: Mock-Verhalten definieren
        when(userRepository.findByUsername(username))
            .thenReturn(Optional.empty());
        doNothing().when(userRepository).save(any(User.class));
        
        // ACT: Methode ausführen
        User result = userService.register(username, password);
        
        // ASSERT: Ergebnis prüfen
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertNotEquals(password, result.getPassword());  // Password gehashed
        
        // VERIFY: Mock-Interaktionen verifizieren
        verify(userRepository).findByUsername(username);
        verify(userRepository).save(any(User.class));
    }
}
```

### Mockito Keywords erklärt:

1. **@Mock** - Erstellt Mock-Objekt
2. **when().thenReturn()** - Definiert Rückgabewert
3. **doNothing()** - Für void-Methoden
4. **verify()** - Prüft ob Methode aufgerufen wurde
5. **any()** - Matcher für beliebige Parameter
6. **never()** - Prüft dass Methode NICHT aufgerufen wurde

### Test-Verteilung:

- **Service Layer**: 27 Tests (52%)
- **Controller Layer**: 19 Tests (37%)
- **Integration Tests**: 6 Tests (11%)

**Wo wird gemockt:**
- In Service Tests: Repositories werden gemockt
- In Controller Tests: Services werden gemockt
- In Integration Tests: Keine Mocks (echter HTTP)

---

## Frage 6: Wo kann man run with test coverage machen?

**Antwort:** Test Coverage kann in IntelliJ IDEA über das integrierte Coverage-Tool ausgeführt werden.

### In IntelliJ IDEA:

**Methode 1: Rechtsklick auf Test-Klasse**
1. Rechtsklick auf Test-Datei (z.B. `UserServiceTest.java`)
2. Wähle: **"Run 'UserServiceTest' with Coverage"**
3. Coverage-Report wird angezeigt

**Methode 2: Via Run Configuration**
1. Rechtsklick auf Projekt oder Test-Ordner
2. Wähle: **"More Run/Debug" → "Run Tests with Coverage"**
3. IntelliJ zeigt Coverage in %

**Methode 3: Via Terminal mit Maven Plugin**
```bash
mvn test jacoco:report
```
Report wird generiert in: `target/site/jacoco/index.html`

### Was zeigt Test Coverage?

- **Line Coverage**: Welche Code-Zeilen wurden ausgeführt
- **Branch Coverage**: Welche if/else-Zweige getestet
- **Method Coverage**: Welche Methoden aufgerufen

**IntelliJ Visualisierung:**
- Grün: Code wurde getestet
- Rot: Code wurde NICHT getestet
- Gelb: Code nur teilweise getestet (z.B. nur if, nicht else)

### Unser Projekt:

**52 Tests** mit Fokus auf:
- Business Logic (Service Layer)
- HTTP Handling (Controller Layer)
- Integration Tests (End-to-End)

---

## Frage 7: Zeige 2 SOLID-Prinzipien in deinem Code her

**Antwort:** Das Projekt demonstriert alle SOLID-Prinzipien, hier zwei Beispiele:

---

### 1. Single Responsibility Principle (SRP)

**Definition:** Jede Klasse hat genau eine Verantwortung und einen Grund sich zu ändern.

#### Beispiel: Layered Architecture

**UserController** - Nur HTTP-Kommunikation:
```java
public class UserController {
    private final UserService userService;
    
    public void handleRegister(HttpExchange exchange) throws IOException {
        // NUR: HTTP Request parsen, Service aufrufen, HTTP Response senden
        String body = new String(exchange.getRequestBody().readAllBytes());
        RegisterRequest request = objectMapper.readValue(body, RegisterRequest.class);
        User user = userService.register(request.getUsername(), request.getPassword());
        sendResponse(exchange, 201, "User registered successfully");
    }
}
```
**Verantwortung:** HTTP-Layer, keine Business Logic!

**UserService** - Nur Business Logic:
```java
public class UserService {
    private final UserRepository userRepository;
    
    public User register(String username, String password) {
        // NUR: Validierung, Password-Hashing, Business Rules
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
**Verantwortung:** Business Logic, keine HTTP, keine SQL!

**UserRepository** - Nur Datenbank-Zugriff:
```java
public class UserRepository {
    public void save(User user) {
        // NUR: SQL-Queries, Datenbank-Operationen
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
**Verantwortung:** Database Access, keine Validierung!

**Vorteil:**
- Änderung an HTTP (z.B. WebSockets) → Nur Controller ändern
- Änderung an DB (z.B. MySQL statt PostgreSQL) → Nur Repository ändern
- Business Rules ändern → Nur Service ändern

---

### 2. Dependency Inversion Principle (DIP)

**Definition:** High-level Module abhängig von Abstraktionen, nicht von konkreten Implementierungen.

#### Beispiel: Constructor Injection

**UserService ohne DIP (schlecht):**
```java
public class UserService {
    private UserRepository userRepository;
    
    public UserService() {
        this.userRepository = new UserRepository();  // ❌ Hard-coded Dependency
    }
}
```

**UserService mit DIP (gut):**
```java
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {  // ✓ Dependency Injection
        this.userRepository = userRepository;
    }
}
```

**Verwendung:**
```java
// Production Code
UserRepository repo = new UserRepository();
UserService service = new UserService(repo);

// Test Code
@Mock
private UserRepository mockRepo;
UserService service = new UserService(mockRepo);  // Mock injiziert!
```

**Vorteil:**
- **Testbarkeit**: Mock kann injiziert werden
- **Flexibilität**: Verschiedene Implementierungen möglich
- **Entkopplung**: UserService kennt nur Interface, nicht Implementierung

#### Beispiel im Test:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;  // Mock-Implementierung
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);  // DIP ermöglicht Mocking!
    }
    
    @Test
    void registerTest() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        userService.register("user", "pass");
        verify(userRepository).save(any(User.class));
    }
}
```

**Ohne DIP:** Tests müssten echte Datenbank verwenden (langsam, komplex)  
**Mit DIP:** Tests verwenden Mocks (schnell, isoliert)

---

### Weitere SOLID-Prinzipien im Code:

**Open/Closed Principle (OCP):**
- MediaType Enum kann erweitert werden (BOOK, PODCAST) ohne bestehenden Code zu ändern

**Liskov Substitution Principle (LSP):**
- Repository-Implementierungen können ausgetauscht werden

**Interface Segregation Principle (ISP):**
- Services haben fokussierte, spezifische Methoden
- Keine aufgeblähten Interfaces mit ungenutzten Methoden

---

## Frage 8: Wie rennst du dein Testskript (Postman oder curl)?

**Antwort:** Beide Methoden verfügbar - Postman Collection und curl Scripts.

### Methode 1: Postman

**Datei:** `MRP_Postman_Collection.json`

**Verwendung:**
1. Postman öffnen
2. **Import** → File auswählen
3. Collection läuft alle Endpoints durch
4. Variablen werden automatisch gesetzt (z.B. Token)

**Vorteile:**
- GUI-basiert
- Automatische Variable-Extraktion
- Request History
- Response-Formatierung

---

### Methode 2: curl Scripts

**Master Script:** `test_all_endpoints.sh`

**Ausführung:**
```bash
# Server muss laufen auf localhost:8080
chmod +x test_all_endpoints.sh
./test_all_endpoints.sh
```

**Verfügbare Scripts:**
- `test_all_endpoints.sh` - Alle Features
- `test_api.sh` - Basic CRUD
- `test_search.sh` - Search & Filter
- `test_leaderboard.sh` - Leaderboard
- `test_recommendations.sh` - Empfehlungen
- `test_favorites.sh` - Favorites
- `test_rating_system.sh` - Rating System

**Beispiel aus test_api.sh:**
```bash
#!/bin/bash

echo "=== Testing Media Ratings Platform API ==="

# 1. Register User
echo "1. Registering user..."
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'

# 2. Login and get Token
echo "2. Logging in..."
TOKEN=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' | tr -d '"')

echo "Token: $TOKEN"

# 3. Create Media
echo "3. Creating media..."
curl -X POST http://localhost:8080/api/media \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"The Matrix","mediaType":"MOVIE","genre":"Sci-Fi","releaseYear":1999}'
```

**Vorteile:**
- Automatisiert
- Reproduzierbar
- CI/CD-fähig
- Kein zusätzliches Tool nötig

---

## Zusatzfragen aus Checkliste

### SQL Injection - Erklärung und Prävention

**Wie SQL Injection funktioniert:**

**Unsicherer Code (GEFÄHRLICH):**
```java
// ❌ NIEMALS SO MACHEN!
String username = request.getParameter("username");
String sql = "SELECT * FROM users WHERE username = '" + username + "'";
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);
```

**Angriff:**
```
username = "admin' OR '1'='1"
```

**Resultierende Query:**
```sql
SELECT * FROM users WHERE username = 'admin' OR '1'='1'
```
→ **Gibt ALLE User zurück!**

**Weiterer Angriff:**
```
username = "admin'; DROP TABLE users; --"
```

**Resultierende Query:**
```sql
SELECT * FROM users WHERE username = 'admin'; DROP TABLE users; --'
```
→ **Löscht die gesamte Users-Tabelle!**

---

**Sicherer Code (KORREKT):**

**Mit PreparedStatement:**
```java
// ✓ SICHER mit PreparedStatement
String sql = "SELECT * FROM users WHERE username = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, username);  // Parameter wird escaped
ResultSet rs = stmt.executeQuery();
```

**Was passiert:**
1. SQL wird vorkompiliert mit Platzhalter `?`
2. Parameter wird separat gesetzt
3. JDBC escaped automatisch gefährliche Zeichen
4. `'` wird zu `\'`, SQL Injection unmöglich

**Angriff-Versuch:**
```
username = "admin' OR '1'='1"
```

**Resultierende Query intern:**
```sql
SELECT * FROM users WHERE username = 'admin\' OR \'1\'=\'1'
```
→ **Sucht nach User mit genau diesem Namen (findet nichts)**

---

**Unser Code - Alle Queries sind sicher:**

**Beispiel aus UserRepository:**
```java
public Optional<User> findByUsername(String username) {
    return DatabaseConnection.executeInTransaction(conn -> {
        String sql = "SELECT * FROM users WHERE token = ?";  // ← Platzhalter
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);  // ← Parameter wird escaped
            ResultSet rs = stmt.executeQuery();
            // ...
        }
    });
}
```

**Alle Repositories verwenden PreparedStatements:**
- UserRepository ✓
- MediaRepository ✓
- RatingRepository ✓
- FavoriteRepository ✓

---

### Phasen von Unit Testing (AAA-Pattern)

**AAA = Arrange, Act, Assert**

**1. ARRANGE - Test Setup**
- Vorbereitung der Test-Daten
- Mock-Verhalten definieren
- Objekte initialisieren

**2. ACT - Ausführung**
- Die zu testende Methode aufrufen
- Nur EINE Aktion pro Test

**3. ASSERT - Überprüfung**
- Ergebnis validieren
- Exceptions prüfen
- Mock-Interaktionen verifizieren

---

**Beispiel aus UserServiceTest:**

```java
@Test
void registerWithExistingUsernameTest() {
    // ===== ARRANGE =====
    String username = "existing";
    User existingUser = new User(username, "hashedpass");
    
    // Mock-Verhalten definieren
    when(userRepository.findByUsername(username))
        .thenReturn(Optional.of(existingUser));
    
    // ===== ACT =====
    // Exception sollte geworfen werden
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.register(username, "password")
    );
    
    // ===== ASSERT =====
    // Prüfe Exception Message
    assertEquals("Username already exists", exception.getMessage());
    
    // Prüfe Mock-Interaktionen
    verify(userRepository).findByUsername(username);
    verify(userRepository, never()).save(any(User.class));
}
```

---

**Weitere Test-Konzepte:**

**Given-When-Then (BDD-Style):**
```java
// GIVEN: existierender User
when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

// WHEN: Register mit gleichem Username
Exception ex = assertThrows(() -> service.register("user", "pass"));

// THEN: Exception mit korrekter Message
assertEquals("Username already exists", ex.getMessage());
```

**Mockito Verify:**
```java
// Prüfe dass Methode aufgerufen wurde
verify(userRepository).save(any(User.class));

// Prüfe dass Methode NICHT aufgerufen wurde
verify(userRepository, never()).delete(any());

// Prüfe Anzahl der Aufrufe
verify(userRepository, times(2)).findByUsername(any());
```

---

### Token-Speicherung in DB - Warum?

**Frage:** Warum speichern wir Tokens in der Datenbank?

**Unsere Implementierung:** Token wird in DB gespeichert

**Vorteile:**

1. **Persistenz über Server-Neustarts**
   - Token bleibt gültig nach Neustart
   - User muss sich nicht neu einloggen

2. **Token-Invalidierung möglich**
   - Logout kann implementiert werden
   - Admin kann User-Sessions beenden
   - Security: Kompromittierte Tokens können gelöscht werden

3. **Session-Management**
   - Aktive Sessions sichtbar
   - "Wer ist gerade eingeloggt?"
   - Audit-Trail möglich

4. **Multi-Device Support**
   - Mehrere Tokens pro User möglich
   - User kann auf verschiedenen Geräten eingeloggt sein

**Code:**
```java
// Token wird generiert
String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

// Token wird in DB gespeichert
userRepository.updateToken(username, token);

// Token wird bei jedem Request validiert
public Optional<User> findByToken(String token) {
    String sql = "SELECT * FROM users WHERE token = ?";
    // ...
}
```

---

**Alternative: In-Memory (nicht implementiert)**

**Nachteile:**
- Token verloren bei Server-Restart
- Skalierung schwierig (mehrere Server-Instanzen)
- Kein Logout möglich

**Vorteile:**
- Schneller (keine DB-Abfrage)
- Weniger DB-Load

**Wann In-Memory:**
- Microservices mit JWT (Token enthält alle Infos)
- Redis/Memcached für Session-Store
- Stateless Applications

**Unsere Entscheidung:**
- **PostgreSQL-Speicherung** für Persistenz und Kontrolle
- Einfache Implementierung
- Ausreichende Performance für die Anforderungen

---

## Demo-Reihenfolge für Präsentation

**1. Server starten**
```bash
docker-compose up -d
mvn clean compile exec:java
```

**2. Routen zeigen**
- Postman Collection durchlaufen ODER
- `./test_all_endpoints.sh` ausführen

**3. Code zeigen - Wichtige Stellen:**
- `RatingController.java` - Comment Confirmation
- `UserRepository.java` - Leaderboard SQL
- `RatingRepository.java` - Like Rating
- `UserServiceTest.java` - Mocking Beispiel
- `UserService.java` - Dependency Injection (DIP)
- `UserController/Service/Repository` - SRP Beispiel
- `UserRepository.java` - PreparedStatement (SQL Injection Schutz)

**4. Tests zeigen**
- IntelliJ: Run with Coverage
- Oder: `mvn test`

**5. Dokumentation zeigen**
- `README.md`
- `protocol.md`
- Git Historie

---

## Wichtige Code-Locations Zusammenfassung

| Feature | Controller | Service | Repository | Zeile |
|---------|-----------|---------|------------|-------|
| Confirm Comment | RatingController | RatingService | RatingRepository | 95-101 |
| Leaderboard | LeaderboardController | LeaderboardService | UserRepository | 202-228 |
| Like Rating | RatingController | RatingService | RatingRepository | 200-213 |
| Mocking | - | - | UserServiceTest | 1-154 |
| SRP Beispiel | UserController | UserService | UserRepository | Alle |
| DIP Beispiel | - | UserService | - | 16-20 |
| SQL Injection Schutz | - | - | Alle Repositories | Überall |

---

## Checkliste für Präsentation

- [ ] Server läuft und ist erreichbar
- [ ] Postman Collection funktioniert
- [ ] curl Scripts sind ausführbar
- [ ] IntelliJ geöffnet mit wichtigen Dateien
- [ ] Git History verfügbar
- [ ] protocol.md und README.md offen
- [ ] Datenbank läuft (Docker)

**Viel Erfolg bei der Präsentation!** 🚀
