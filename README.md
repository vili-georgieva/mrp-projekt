# Media Ratings Platform (MRP)

## Projektbeschreibung
RESTful HTTP Server zur Verwaltung von Medieninhalten (Filme, Serien, Spiele) mit User-Registration, Authentifizierung, Bewertungssystem, Favoriten und Empfehlungen.

## Features
- **User Management**: Registrierung, Login, Profil mit Statistiken
- **Media Management**: CRUD-Operationen (nur Owner kann ändern/löschen)
- **Rating System**: 1-5 Sterne, Kommentare mit Moderation, Likes
- **Favorites**: Media zu Favoriten hinzufügen/entfernen
- **Search & Filter**: Nach Titel, Genre, Typ, Altersbeschränkung
- **Leaderboard**: Top User nach Anzahl der Ratings
- **Recommendations**: Genre-basierte Empfehlungen basierend auf hoch bewerteten Media

## Student
- **Name:** Velichka Georgieva
- **Matrikelnummer:** if24b265

## GitHub Repository
https://github.com/vili-georgieva/mrp-projekt

## Technologien
- **Java 21**: LTS-Version mit modernen Features
- **Pure HTTP** (com.sun.net.httpserver.HttpServer): Kein Web-Framework (Spring/JSP)
- **PostgreSQL 16** (Docker): Relational Database mit vollem ACID-Support
- **Jackson 2.16.0**: JSON Serialisierung/Deserialisierung
- **JUnit 5.10.0**: Unit Testing Framework
- **Mockito 5.5.0**: Mocking für isolierte Tests
- **Maven**: Build & Dependency Management

## Voraussetzungen
- Java 21+
- Docker & Docker Compose

## Schnellstart

### 1. PostgreSQL starten
```bash
docker-compose up -d
```
Wartet bis PostgreSQL bereit ist (Healthcheck). Das Schema wird automatisch initialisiert.

### 2. Anwendung starten
```bash
# In IntelliJ: Main.java ausführen
# Oder via Maven:
mvn clean compile exec:java -Dexec.mainClass="org.example.Main"

# Oder mit Fat JAR:
mvn clean package
java -jar target/sem_projekt-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Server läuft auf: `http://localhost:8080`

Database: `localhost:5432/mrp_db` (User: postgres, Password: postgres)

### 3. Tests ausführen
```bash
# Unit Tests
mvn test

# Alle curl-Tests (stelle sicher dass die Scripts ausführbar sind)
chmod +x test_*.sh
./test_all_endpoints.sh

# Oder einzelne Features:
./test_api.sh             # Basic API Tests
./test_search.sh          # Search & Filter
./test_leaderboard.sh     # Leaderboard
./test_recommendations.sh # Recommendations
```

**Hinweis**: Die curl-Tests benötigen einen laufenden Server auf localhost:8080.

### 4. Postman Collection
Import `MRP_Postman_Collection.json` in Postman.

## API Endpoints

### User Management
| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|--------------|------|
| POST | `/api/users/register` | User registrieren | Nein |
| POST | `/api/users/login` | Login, Token erhalten | Nein |
| GET | `/api/users/{username}` | Profil + Statistiken | Ja |
| PUT | `/api/users/{username}` | Profil bearbeiten (Passwort) | Ja |

### Media Management
| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|--------------|------|
| GET | `/api/media` | Alle Media abrufen | Nein |
| GET | `/api/media?title=...&genre=...` | Search & Filter | Nein |
| GET | `/api/media/{id}` | Media by ID | Nein |
| POST | `/api/media` | Media erstellen | Ja |
| PUT | `/api/media/{id}` | Media updaten (Owner) | Ja |
| DELETE | `/api/media/{id}` | Media löschen (Owner) | Ja |

### Rating System
| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|--------------|------|
| POST | `/api/media/{id}/ratings` | Rating erstellen/updaten | Ja |
| GET | `/api/media/{id}/ratings` | Ratings für Media | Nein |
| GET | `/api/users/{username}/rating-history` | Rating-Historie | Nein |
| PUT | `/api/ratings/{id}` | Rating updaten (Owner) | Ja |
| DELETE | `/api/ratings/{id}` | Rating löschen (Owner) | Ja |
| PATCH | `/api/ratings/{id}/comment` | Kommentar updaten | Ja |
| DELETE | `/api/ratings/{id}/comment` | Kommentar löschen | Ja |
| POST | `/api/ratings/{id}/like` | Rating liken | Ja |
| POST | `/api/ratings/{id}/confirm` | Rating bestätigen (Moderation) | Ja |

### Favorites System
| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|--------------|------|
| POST | `/api/users/{username}/favorites/{mediaId}` | Zu Favoriten hinzufügen | Ja |
| DELETE | `/api/users/{username}/favorites/{mediaId}` | Aus Favoriten entfernen | Ja |
| POST | `/api/users/{username}/favorites/{mediaId}/toggle` | Toggle Favorit | Ja |
| GET | `/api/users/{username}/favorites` | Alle Favoriten | Ja |
| GET | `/api/users/{username}/favorites/check/{mediaId}` | Ist Favorit? | Ja |

### Leaderboard & Recommendations
| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|--------------|------|
| GET | `/api/leaderboard?limit=10` | Top User nach Ratings | Ja |
| GET | `/api/recommendations?username={username}&limit=10` | Empfehlungen für User | Ja |

## Authentifizierung
Token-basierte Authentifizierung via Bearer Token:
```
Authorization: Bearer {token}
```

**Token-Format**: Der Token ist eine doppelte UUID (z.B. `a1b2c3d4-e5f6-7890-abcd-ef1234567890-a1b2c3d4-e5f6-7890-abcd-ef1234567890`) und wird beim Login generiert.

**Token-Validierung**: Alle geschützten Endpoints erfordern einen gültigen Bearer Token im Authorization-Header.

**Token-Persistenz**: Tokens werden in der Datenbank gespeichert und sind persistent über Server-Neustarts hinweg.

## HTTP Response Codes
- **200 OK**: Erfolgreiche GET/PUT/PATCH-Anfragen
- **201 Created**: Erfolgreiche POST-Anfragen (User/Media/Rating erstellt)
- **204 No Content**: Erfolgreiche DELETE-Anfragen
- **400 Bad Request**: Ungültige Eingabedaten oder fehlende Parameter
- **401 Unauthorized**: Fehlende oder ungültige Authentifizierung
- **403 Forbidden**: Gültige Authentifizierung aber unzureichende Berechtigungen
- **404 Not Found**: Ressource nicht gefunden
- **405 Method Not Allowed**: Falsche HTTP-Methode
- **500 Internal Server Error**: Datenbankfehler oder Server-Fehler

## Projektstruktur
```
src/main/java/org/example/
├── Main.java                 # Entry Point
├── controller/               # HTTP Request Handler (Presentation Layer)
│   ├── UserController.java
│   ├── MediaController.java
│   ├── RatingController.java
│   ├── FavoriteController.java
│   ├── LeaderboardController.java
│   └── RecommendationController.java
├── service/                  # Business Logic Layer
│   ├── UserService.java
│   ├── MediaService.java
│   ├── RatingService.java
│   ├── FavoriteService.java
│   ├── LeaderboardService.java
│   └── RecommendationService.java
├── repository/               # Data Access Layer
│   ├── UserRepository.java
│   ├── MediaRepository.java
│   ├── RatingRepository.java
│   └── FavoriteRepository.java
├── model/                    # Domain Models
│   ├── User.java
│   ├── MediaEntry.java
│   ├── Rating.java
│   └── MediaType.java (enum)
├── dto/                      # Data Transfer Objects
│   ├── LoginRequest.java
│   └── RegisterRequest.java
├── server/                   # HTTP Server
│   └── RestServer.java
└── util/                     # Utilities
    └── DatabaseConnection.java
```

## Unit Tests
- **52 Unit Tests** (JUnit 5 + Mockito): 46 aktive Tests, 6 Integration Tests (übersprungen wenn Server nicht läuft)
- **Service Layer Tests**: 27 Tests (UserServiceTest, MediaServiceTest, RatingServiceTest, FavoriteServiceTest)
- **Controller Tests**: 19 Tests (UserControllerTest, MediaControllerTest, RatingControllerTest, FavoriteControllerTest)
- **Integration Tests**: 6 Tests (RouteIntegrationTest - benötigt laufenden Server)

## Beispiel-Nutzung

### 1. User registrieren
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'
```
Gibt Token zurück: `"uuid1-uuid2"`

### 3. Media erstellen
```bash
curl -X POST http://localhost:8080/api/media \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Matrix",
    "description":"A sci-fi action movie",
    "mediaType":"MOVIE",
    "releaseYear":1999,
    "genres":["Sci-Fi","Action"],
    "ageRestriction":16
  }'
```

### 4. Rating erstellen
```bash
curl -X POST http://localhost:8080/api/media/1/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "stars":5,
    "comment":"Great movie!"
  }'
```

### 5. Zu Favoriten hinzufügen
```bash
curl -X POST http://localhost:8080/api/users/testuser/favorites/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Empfehlungen abrufen
```bash
curl -X GET "http://localhost:8080/api/recommendations?username=testuser&limit=10" \
  -H "Authorization: Bearer $TOKEN"
```

### 7. Leaderboard abrufen
```bash
curl -X GET "http://localhost:8080/api/leaderboard?limit=10" \
  -H "Authorization: Bearer $TOKEN"
```

## Dokumentation
- **README.md**: Benutzerhandbuch, API-Referenz, Quick Start Guide
- **protocol.md**: Entwicklungsbericht mit Architektur-Entscheidungen, SOLID-Prinzipien, Lessons Learned
- **MRP_Postman_Collection.json**: Postman Collection für alle API Endpoints
- **Test Scripts**: Curl-basierte Integration Tests (`test_all_endpoints.sh`, `test_api.sh`, etc.)

## Author
**Velichka Georgieva**  
Matrikelnummer: if24b265  
FH Technikum Wien  
Januar 2026
