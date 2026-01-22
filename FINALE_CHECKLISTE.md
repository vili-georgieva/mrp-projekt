# FINALE CHECKLISTE VOR ABGABE

## Datum: 22. Januar 2026

---

## PROJEKT STATUS

**ALLE ANFORDERUNGEN ERFÜLLT: 44 / 44 Punkte**

---

## CHECKLISTE VOR ABGABE

### 1. Docker & Datenbank
- [ ] Docker läuft: `sudo docker-compose up -d`
- [ ] PostgreSQL erreichbar: `psql -h localhost -U postgres -d mrp_db`
- [ ] Tabellen existieren: `\dt` in psql
- [ ] Daten können eingefügt werden (Testdaten vorhanden)

### 2. Code & Kompilierung
- [ ] Code kompiliert ohne Errors
- [ ] Keine kritischen Warnings
- [ ] Server startet ohne Fehler: `java -cp target/classes org.example.Main`
- [ ] Server läuft auf Port 8080

### 3. Unit Tests
- [ ] Alle 20 Tests laufen durch
- [ ] Keine failed Tests
- [ ] Test Coverage prüfen

### 4. Integration Tests
- [ ] test_api.sh funktioniert
- [ ] test_profile_stats.sh funktioniert
- [ ] test_search.sh funktioniert
- [ ] test_leaderboard.sh funktioniert
- [ ] test_recommendations.sh funktioniert

### 5. Dokumentation
- [ ] README.md aktuell und vollständig
- [ ] protocol.md vollständig mit:
  - [x] Lessons Learned
  - [x] Unit Testing Strategy
  - [x] SOLID Principles mit Code-Beispielen
  - [x] Time Tracking Tabelle
  - [x] Git Link
- [ ] Alle Kommentare im Code auf Englisch
- [ ] Keine TODO-Kommentare im Code

### 6. Git Repository
- [ ] Alle Änderungen committed
- [ ] `git push` ausgeführt
- [ ] Repository ist public oder Zugriff gewährt
- [ ] README.md enthält Git Link

### 7. ZIP-Datei erstellen
- [ ] Ordnerstruktur korrekt:
  ```
  mrp-projekt-main/
  ├── src/
  ├── target/
  ├── README.md
  ├── protocol.md
  ├── pom.xml
  ├── docker-compose.yml
  ├── test_api.sh
  └── ...
  ```
- [ ] ZIP-Datei erstellt
- [ ] ZIP-Datei entpackt und getestet
- [ ] Dateigröße prüfen (nicht zu groß, target/ evtl. ausschließen)

### 8. Moodle Upload
- [ ] Moodle Deadline geprüft
- [ ] Richtige Abgabe-Seite gefunden
- [ ] ZIP hochgeladen
- [ ] Upload-Bestätigung erhalten

---

## FINALE PRÜFUNG

### Test-Sequenz
```bash
# 1. Docker starten
sudo docker-compose up -d

# 2. Warten bis PostgreSQL bereit
sleep 5

# 3. Server starten (in neuem Terminal)
java -cp target/classes org.example.Main

# 4. Tests ausführen (in anderem Terminal)
./test_api.sh
./test_profile_stats.sh
./test_search.sh
./test_leaderboard.sh
./test_recommendations.sh

# 5. Server stoppen (Ctrl+C)

# 6. Docker stoppen
sudo docker-compose down
```

### Erwartete Ergebnisse
- Server startet ohne Errors
- Alle Tests laufen erfolgreich durch
- Responses sind korrekt (JSON Format)
- HTTP Status Codes korrekt (200, 201, 401, etc.)

---

## PRESENTATION VORBEREITUNG

### Was bereit haben (10-15 Minuten)

1. **Environment gestartet:**
   - Docker läuft
   - PostgreSQL bereit
   - Server gestartet auf Port 8080

2. **Postman/Curl bereit:**
   - test_api.sh geöffnet
   - Bereit zum Ausführen
   - Eventuell bereits ausgeführt mit Beispieldaten

3. **Code geöffnet:**
   - IntelliJ mit Projekt offen
   - RestServer.java zeigen (Routing)
   - UserService.java zeigen (Business Logic)
   - UserRepository.java zeigen (Database Access)

4. **Dokumentation offen:**
   - protocol.md geöffnet
   - Class Diagram sichtbar
   - SOLID Principles Section bereit

5. **Datenbank geöffnet:**
   - psql mit mrp_db verbunden
   - `SELECT * FROM users;` bereit
   - `SELECT * FROM media_entries;` bereit

### Präsentations-Flow

1. **Architektur zeigen** (2 min)
   - Class Diagram
   - 3-tier Architecture erklären

2. **Live Demo** (5 min)
   - User Registration
   - User Login
   - Media erstellen
   - Rating erstellen
   - Recommendations zeigen

3. **Code zeigen** (3 min)
   - SOLID Principles im Code
   - Ein Controller, Service, Repository zeigen

4. **Tests zeigen** (2 min)
   - Unit Test ausführen
   - Integration Test (Curl) ausführen

5. **Q&A** (3 min)

---

## TROUBLESHOOTING

### Docker startet nicht
```bash
sudo systemctl start docker
sudo usermod -aG docker $USER
newgrp docker
```

### PostgreSQL Connection Fehler
```bash
# Prüfen ob Container läuft
docker ps

# Logs prüfen
docker logs mrp_postgres

# Port prüfen
lsof -i :5432
```

### Server startet nicht
```bash
# Neu kompilieren
mvn clean compile

# Classpath prüfen
ls -la target/classes/org/example/Main.class

# Java Version prüfen
java -version  # Sollte 21 sein
```

### Tests schlagen fehl
- Server muss laufen
- Docker muss laufen
- Token in test_api.sh aktualisieren wenn nötig

---

## FINAL CHECK

**ALLE PUNKTE MÜSSEN ERFÜLLT SEIN:**

- [x] 20 Unit Tests vorhanden und lauffähig
- [x] PostgreSQL + Docker läuft
- [x] Token-based Security implementiert
- [x] Pure HTTP (kein Framework)
- [x] SQL Injection Prevention (PreparedStatements)
- [x] Kein OR-Mapping (kein Hibernate)
- [x] SOLID Principles dokumentiert
- [x] Integration Tests vorhanden
- [x] Protocol.md vollständig
- [x] Git Repository Link vorhanden

**PROJEKT IST BEREIT FÜR ABGABE!**

**PUNKTZAHL: 44 / 44 (100%)**
