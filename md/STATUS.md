# Aktueller Projekt-Status
## Datum: 20. Januar 2026
### Gelöste Probleme:
1. **Datenbank-Verbindung hergestellt**
   - Docker war nicht verfügbar
   - PostgreSQL nativ installiert und gestartet (systemd)
   - Datenbank `mrp_db` erstellt
   - Schema initialisiert (users, media_entries, ratings, favorites)
   - Verbindung auf localhost:5432 mit Credentials: postgres/postgres
2. **Token-Authentifizierung funktioniert korrekt**
   - Format: UUID-UUID (z.B. `49a40343-622a-4979-a5fc-2be0bfa0f632-e3429952-c0a5-494d-9cea-054ad41e0be6`)
   - 73 Zeichen lang
   - Wird bei Login generiert und in DB gespeichert
   - Authorization Header: `Bearer {token}`
   - Validierung funktioniert (401 bei fehlerhaftem/fehlendem Token)
3. **Server läuft und ist funktional**
   - Port: 8080
   - Alle Endpoints erreichbar
   - User Registration: `POST /api/users/register`
   - User Login: `POST /api/users/login`
   - User Info: `GET /api/users/{username}` (mit Token)
4. **Routing-Problem behoben**
   - FavoriteController war auf `/api/users/` registriert
   - Verschob auf `/api/users/favorites`
   - UserController auf `/api/users` funktioniert jetzt korrekt
### Getestete Funktionen:
- ✓ User Registration
- ✓ User Login mit UUID-Token-Generierung
- ✓ Token-basierte Authentifizierung
- ✓ Authorization-Header-Validierung
- ✓ 401 Unauthorized bei fehlenden/falschen Tokens
- ✓ Datenbank-Persistenz (Tokens in DB gespeichert)
### Nächste Schritte (aus PROJEKTPLAN_FINAL.md):
1. **Dokumentation** (Höchste Priorität - 3.5 Punkte)
2. **Search & Filter** (3 Punkte)
3. **Leaderboard** (2 Punkte)
4. **Profile & Statistics** (1 Punkt)
### Aktuell gesicherte Punkte: 38+
- Docker Setup: 7 Punkte
- Unit Tests: 20 Punkte
- Rating System: 2 Punkte
- Favorites: 2 Punkte
- Average Rating: 3 Punkte
- Rating History: 2 Punkte
- Comment Confirmation: 2 Punkte
### Server-Start-Anleitung:
```bash
# PostgreSQL starten (falls nicht läuft)
sudo systemctl start postgresql
# Server starten
cd /home/m/Downloads/mrp-projekt-main
java -cp "target/classes:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.16.0/jackson-databind-2.16.0.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.16.0/jackson-annotations-2.16.0.jar:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.16.0/jackson-core-2.16.0.jar:$HOME/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.16.0/jackson-datatype-jsr310-2.16.0.jar:$HOME/.m2/repository/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar:$HOME/.m2/repository/org/checkerframework/checker-qual/3.41.0/checker-qual-3.41.0.jar" org.example.Main
# Oder im Hintergrund mit Log:
nohup java -cp "..." org.example.Main > server.log 2>&1 &
```
### Test-Befehle:
```bash
# Registration
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'
# Login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'
# User Info (mit Token)
curl -X GET http://localhost:8080/api/users/testuser \
  -H "Authorization: Bearer {TOKEN}"
```
