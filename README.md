# Media Ratings Platform (MRP)

## Projektbeschreibung
RESTful HTTP Server zur Verwaltung von Medieninhalten (Filme, Serien, Spiele) mit User-Registration, Authentifizierung, Bewertungssystem, Favoriten und Empfehlungen.

## Student
- **Name:** Velichka Georgieva
- **Matrikelnummer:** if24b265

## GitHub Repository
https://github.com/vili-georgieva/mrp-projekt

## Technologien
- Java 21
- Pure HTTP (com.sun.net.httpserver.HttpServer) - kein Web-Framework
- PostgreSQL (Docker)
- Jackson (JSON Serialisierung)
- JUnit 5 + Mockito (Unit Tests)

## Voraussetzungen
- Java 21+
- Docker & Docker Compose

## Schnellstart

### 1. PostgreSQL starten
```bash
docker-compose up -d
```

### 2. Anwendung starten
```bash
# In IntelliJ: Main.java ausführen
# Oder via Maven:
mvn clean compile exec:java -Dexec.mainClass="org.example.Main"
```

Server läuft auf: `http://localhost:8080`

### 3. Tests ausführen
```bash
# Alle curl-Tests
./test_all_endpoints.sh

# Oder einzelne Features:
./test_api.sh           # Basic API Tests
./test_search.sh        # Search & Filter
./test_leaderboard.sh   # Leaderboard
./test_recommendations.sh # Recommendations
```

### 4. Postman Collection
Import `MRP_Postman_Collection.json` in Postman.

## API Endpoints

### User Management
| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|--------------|------|
| POST | `/api/users/register` | User registrieren | Nein |
| POST | `/api/users/login` | Login, Token erhalten | Nein |
| GET | `/api/users/{username}` | Profil + Statistiken | Ja |

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
| GET | `/api/users/{username}/rating-history` | Rating-Historie | Ja |
| DELETE | `/api/ratings/{id}` | Rating löschen (Owner) | Ja |
| PATCH | `/api/ratings/{id}/comment` | Kommentar updaten | Ja |
| DELETE | `/api/ratings/{id}/comment` | Kommentar löschen | Ja |
| POST | `/api/ratings/{id}/like` | Rating liken | Ja |
| POST | `/api/ratings/{id}/confirm` | Rating bestätigen | Ja |

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
| GET | `/api/leaderboard?limit=10` | Top User nach Ratings | Nein |
| GET | `/api/users/{username}/recommendations?limit=10` | Empfehlungen | Nein |

## Authentifizierung
Token-basierte Authentifizierung via Bearer Token:
```
Authorization: Bearer {token}
```

## Projektstruktur
```
src/main/java/org/example/
├── Main.java                 # Entry Point
├── controller/               # HTTP Request Handler (Presentation Layer)
├── service/                  # Business Logic Layer
├── repository/               # Data Access Layer
├── model/                    # Domain Models
├── dto/                      # Data Transfer Objects
├── server/                   # HTTP Server
└── util/                     # Utilities (DatabaseConnection)
```

## Unit Tests
- 53 Unit Tests (JUnit 5 + Mockito)
- Fokus auf Service Layer (Business Logic)
- Controller Tests für HTTP Handling
- Integration Tests für HTTP Routes

## Dokumentation
- `protocol.md` - Entwicklungsbericht, Architektur, SOLID Principles
- `MRP_Postman_Collection.json` - Postman Collection
- `test_all_endpoints.sh` - Curl Integration Tests
```

### Manuelle curl-Tests
Siehe `CURL_COMMANDS.md` für alle verfügbaren curl-Befehle.

Schnelltest:
```bash
# 1. User registrieren
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'

# 2. Login und Token holen
TOKEN=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' | tr -d '"')

# 3. Medium erstellen
curl -X POST http://localhost:8080/api/media \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"The Matrix","mediaType":"MOVIE","genre":"Sci-Fi","releaseYear":1999}'
```

### Using Postman
Import `MRP_Postman_Collection.json` into Postman and run the requests.

## API Endpoints

### User Management
- `POST /api/users/register` - Register a new user
- `POST /api/users/login` - Login and get authentication token
- `GET /api/users/{username}` - Get user profile (requires authentication)

### Media Management
- `POST /api/media` - Create a new media entry (requires authentication)
- `GET /api/media` - Get all media entries
- `GET /api/media/{id}` - Get a specific media entry
- `PUT /api/media/{id}` - Update a media entry (requires authentication, owner only)
- `DELETE /api/media/{id}` - Delete a media entry (requires authentication, owner only)

### Rating System
- `POST /api/media/{mediaId}/ratings` - Create or update rating (requires authentication)
- `GET /api/media/{mediaId}/ratings` - Get all ratings for a media
- `GET /api/users/{username}/rating-history` - Get user's rating history (requires authentication)
- `DELETE /api/ratings/{ratingId}` - Delete rating (requires authentication, owner only)
- `PATCH /api/ratings/{ratingId}/comment` - Update rating comment (requires authentication, owner only)
- `DELETE /api/ratings/{ratingId}/comment` - Delete rating comment (requires authentication, owner only)
- `POST /api/ratings/{ratingId}/like` - Like a rating (requires authentication)
- `POST /api/ratings/{ratingId}/confirm` - Confirm rating (moderation, requires authentication)

### Favorites System
- `POST /api/users/{username}/favorites/{mediaId}` - Add media to favorites (requires authentication)
- `DELETE /api/users/{username}/favorites/{mediaId}` - Remove media from favorites (requires authentication)
- `POST /api/users/{username}/favorites/{mediaId}/toggle` - Toggle favorite status (requires authentication)
- `GET /api/users/{username}/favorites` - Get all favorites (requires authentication)
- `GET /api/users/{username}/favorites/check/{mediaId}` - Check if media is favorite (requires authentication)

## Authentication
The API uses Bearer token authentication. After logging in, include the token in the Authorization header:
```
Authorization: Bearer {token}
```

**Token Format:** Der Token wird beim Login zurückgegeben und hat das Format: `username.mrpToken`

## Project Structure
```
src/main/java/org/example/
├── Main.java                    # Application entry point
├── controller/                  # HTTP request handlers
│   ├── FavoriteController.java
│   ├── MediaController.java
│   ├── RatingController.java
│   └── UserController.java
├── dto/                         # Data Transfer Objects
│   ├── LoginRequest.java
│   └── RegisterRequest.java
├── model/                       # Domain models
│   ├── MediaEntry.java
│   ├── MediaType.java
│   ├── Rating.java
│   └── User.java
├── repository/                  # Database access layer
│   ├── FavoriteRepository.java
│   ├── MediaRepository.java
│   ├── RatingRepository.java
│   └── UserRepository.java
├── server/                      # HTTP server
│   └── RestServer.java
├── service/                     # Business logic
│   ├── FavoriteService.java
│   ├── MediaService.java
│   ├── RatingService.java
│   └── UserService.java
└── util/                        # Utilities
    └── DatabaseConnection.java
```

## Example Usage

### 1. Register a user
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

### 3. Create a media entry
```bash
curl -X POST http://localhost:8080/api/media \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer testuser.mrpToken" \
  -d '{
    "title":"The Matrix",
    "description":"A sci-fi action movie",
    "mediaType":"MOVIE",
    "releaseYear":1999,
    "genre":"Sci-Fi",
    "director":"Wachowski Sisters"
  }'
```

### 4. Create a rating
```bash
curl -X POST http://localhost:8080/api/media/1/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer testuser.mrpToken" \
  -d '{
    "score":9,
    "comment":"Great movie!"
  }'
```

### 5. Add to favorites
```bash
curl -X POST http://localhost:8080/api/users/testuser/favorites/1 \
  -H "Authorization: Bearer testuser.mrpToken"
```

## Author
Velichka Georgieva


