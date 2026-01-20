# Media Ratings Platform - Aktueller Status
Stand: 20. Januar 2026

## Implementiert und Funktionsfähig

### 1. Core System
- REST Server mit Java HttpServer
- PostgreSQL Datenbank
- Repository-Service-Controller Architektur
- Token-basierte Authentifizierung (UUID-Format)
- SHA-256 Password Hashing

### 2. Rating System (KOMPLETT)
- Create/Update Rating (1-5 Sterne + Kommentar)
- Delete Rating
- Update/Delete Comment
- Like-Funktion
- Comment Moderation (confirmed Flag)
- Average Rating Berechnung (automatisch)
- Rating History per User
- One Rating per User per Media (UNIQUE Constraint)

**Endpoints**: 7 Endpoints implementiert

### 3. Favorites System (KOMPLETT)
- Add Favorite
- Remove Favorite
- Toggle Favorite
- Get Favorites (mit Media-Details)
- Check if Favorite

**Endpoints**: 5 Endpoints implementiert

### 4. Media Management (KOMPLETT)
- Create Media
- Update Media (nur Owner)
- Delete Media (nur Owner)
- Get Media by ID
- Get all Media
- Average Rating automatisch aktualisiert

**Endpoints**: 5 Endpoints implementiert

### 5. User Management (KOMPLETT)
- Registrierung mit Validierung
- Login mit Token-Generierung
- Token-basierte Authentifizierung
- Password Hashing

**Endpoints**: 2 Endpoints implementiert

### 6. Unit Tests
- UserServiceTest: 6 Tests
- UserControllerTest: 4 Tests
- MediaServiceTest: 7 Tests
- RatingServiceTest: 2 Tests

**Gesamt**: 19 Tests (Minimum 20 gefordert - 1 Test fehlt noch)

### 7. Docker Setup
- docker-compose.yml vorhanden
- PostgreSQL Container konfiguriert
- Volume für persistente Daten
- Auto-Initialisierung mit schema.sql
- Healthcheck konfiguriert

**Status**: Fertig, aber Docker Daemon muss gestartet werden

### 8. Datenbank Schema
- users (mit Token-Feld)
- media_entries (mit average_rating, genres, age_restriction)
- ratings (mit UNIQUE Constraint, confirmed, likes)
- favorites (Composite Primary Key)
- Alle Foreign Keys mit CASCADE DELETE

## Noch zu implementieren

### Priorität HOCH (für mehr Punkte)
1. **Search & Filter** (3 Punkte, ~2h)
   - GET /api/media/search?title=...&genre=...&type=...&minRating=...
   
2. **Leaderboard** (2 Punkte, ~1h)
   - GET /api/media/leaderboard?limit=10
   
3. **User Profile** (1 Punkt, ~1h)
   - GET /api/users/{username}/profile (mit Statistiken)

### Priorität MITTEL
4. **1 weiterer Unit Test** (für 20+ Tests)
   - z.B. testValidateToken() in UserServiceTest

5. **Dokumentation vervollständigen** (3.5 Punkte)
   - Lessons Learned
   - SOLID Principles detailliert
   - Time Tracking

### Optional (kann weggelassen werden)
6. **Recommendation System** (4 Punkte, aber sehr komplex)

## Geschätzte Punktzahl

| Feature | Punkte | Status |
|---------|--------|--------|
| Rating System | 2 | FERTIG |
| Favorites System | 2 | FERTIG |
| Average Rating | 3 | FERTIG |
| Comment Confirmation | 2 | FERTIG |
| Rating History | 2 | FERTIG |
| Docker Setup | 7 | FERTIG |
| Unit Tests | ca. 5 | Fast fertig (19/20) |
| Media CRUD | 3 | FERTIG |
| User Auth | 2 | FERTIG |
| **Gesamt aktuell** | **~28** | |
| Search & Filter | 3 | TODO |
| Leaderboard | 2 | TODO |
| User Profile | 1 | TODO |
| Dokumentation | 3.5 | Teilweise |
| **Potenzial gesamt** | **~37.5** | |

## Nächste Schritte (Empfehlung)

1. **Search & Filter implementieren** (~2h) - +3 Punkte
2. **Leaderboard implementieren** (~1h) - +2 Punkte  
3. **User Profile implementieren** (~1h) - +1 Punkt
4. **1 weiteren Test schreiben** (~15min)
5. **Dokumentation vervollständigen** (~2h) - +3.5 Punkte

**Zeitaufwand gesamt**: ~6-7h für ca. 10 zusätzliche Punkte

## Token-System

Das Token-System ist bereits korrekt implementiert:
- Format: `{UUID}-{UUID}` (z.B. `3550cead-cfe7-497d-8b89-c39a9dad4d6a-47b590ce-0e4e-4735-9d2a-c0aabe3df1a1`)
- Wird in DB gespeichert
- Wird bei jedem Request validiert
- Authorization Header: `Bearer {token}`

Keine Änderung notwendig - das alte `username.mrp` Format wurde bereits ersetzt.

## Docker Status

WICHTIG: PostgreSQL muss laufen, damit die Application startet.

### Option 1: Docker verwenden (empfohlen)

```bash
# Docker Daemon starten (falls nicht aktiv)
sudo snap start docker.dockerd
sudo chmod 666 /var/run/docker.sock

# PostgreSQL Container starten
cd /home/m/Downloads/mrp-projekt-main
docker compose up -d

# Prüfen ob Container läuft
docker ps

# Logs prüfen falls Probleme
docker compose logs postgres
```

### Option 2: PostgreSQL nativ installieren

```bash
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql

# DB und User erstellen
sudo -u postgres psql -c "CREATE DATABASE mrp_db;"
sudo -u postgres psql -c "CREATE USER postgres WITH PASSWORD 'postgres';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE mrp_db TO postgres;"

# Schema initialisieren
psql -U postgres -d mrp_db -f src/main/resources/schema.sql
```

### Application starten

```bash
cd /home/m/Downloads/mrp-projekt-main
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.Main"
```

Server läuft dann auf: http://localhost:8080
