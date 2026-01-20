# Media Ratings Platform (MRP)

## Project Description
A RESTful HTTP server for managing media content (movies, series, games) with user registration, authentication, ratings, and favorites system.

## GitHub Repository
https://github.com/vili-georgieva/mrp-projekt

## Technologies
- Java 21
- Pure HTTP (com.sun.net.httpserver.HttpServer)
- PostgreSQL Database
- Jackson (JSON serialization)
- Docker & Docker Compose

## Prerequisites
- Java 21 or higher
- Maven 3.6+
- Docker & Docker Compose (empfohlen)

## Projekt starten (mit Docker) - EMPFOHLEN

### 1. PostgreSQL Container starten
```bash
# Im Projekt-Verzeichnis
docker-compose up -d
```

Was passiert:
- PostgreSQL 16 Container wird gestartet
- Datenbank `mrp_db` wird automatisch erstellt
- Schema mit allen Tabellen wird automatisch ausgeführt
- Container läuft im Hintergrund

### 2. Container-Status prüfen
```bash
# Status anzeigen
docker-compose ps

# Logs ansehen
docker-compose logs -f postgres
```

### 3. Anwendung starten
```bash
# Build und Start
mvn clean compile exec:java -Dexec.mainClass="org.example.Main"
```

Server läuft auf: `http://localhost:8080`

### 4. Container stoppen
```bash
# Container stoppen (Daten bleiben erhalten)
docker-compose stop

# Container stoppen und entfernen (Daten bleiben im Volume)
docker-compose down

# Container UND Daten komplett löschen
docker-compose down -v
```

## Alternative: Projekt ohne Docker starten

### PostgreSQL manuell installieren
```bash
# Ubuntu/Debian
sudo apt-get install postgresql postgresql-contrib

# macOS
brew install postgresql
```

### Datenbank erstellen
```sql
CREATE DATABASE mrp_db;
```

### Configuration
Database credentials in `src/main/resources/application.properties`:
```properties
db.url=jdbc:postgresql://localhost:5432/mrp_db
db.username=postgres
db.password=postgres
```

### Build und Run
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.Main"
```


## Testing

### Automatisches Test-Script
```bash
chmod +x test_api_curl.sh
./test_api_curl.sh
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


