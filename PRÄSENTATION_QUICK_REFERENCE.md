# Quick Reference für Präsentation

## Schnellzugriff - Wichtige Code-Stellen

### 1. Rating Comment Confirmation
**Pfad:** `src/main/java/org/example/controller/RatingController.java`
**Zeile:** 95-101
**Zeigen:** `case "confirm"` Block

### 2. Leaderboard SQL Query
**Pfad:** `src/main/java/org/example/repository/UserRepository.java`
**Zeile:** 202-228
**Zeigen:** SQL mit JOIN und GROUP BY

### 3. Like Rating
**Pfad:** `src/main/java/org/example/repository/RatingRepository.java`
**Zeile:** 200-213
**Zeigen:** `UPDATE ratings SET likes = likes + 1`

### 4. Mocking Beispiel
**Pfad:** `src/test/java/org/example/service/UserServiceTest.java`
**Zeile:** 1-80
**Zeigen:** `@Mock`, `when()`, `verify()`

### 5. SOLID - Single Responsibility Principle
**Pfade:**
- Controller: `src/main/java/org/example/controller/UserController.java`
- Service: `src/main/java/org/example/service/UserService.java`
- Repository: `src/main/java/org/example/repository/UserRepository.java`
**Zeigen:** Jede Klasse hat nur eine Verantwortung

### 6. SOLID - Dependency Inversion Principle
**Pfad:** `src/main/java/org/example/service/UserService.java`
**Zeile:** 16-20
**Zeigen:** Constructor Injection

### 7. SQL Injection Schutz
**Pfad:** `src/main/java/org/example/repository/UserRepository.java`
**Zeile:** 46-65
**Zeigen:** PreparedStatement mit `?` und `stmt.setString()`

### 8. Test Coverage
**IntelliJ:** Rechtsklick auf `src/test/java` → "Run Tests with Coverage"
**Terminal:** `mvn test`

---

## Demo-Ablauf (5 Minuten)

### 1. Server Starten (30 Sekunden)
```bash
# Terminal 1: Datenbank
docker-compose up -d

# Terminal 2: Server
mvn clean compile exec:java
```

### 2. Routen Demonstrieren (1 Minute)
**Option A: curl Script**
```bash
./test_all_endpoints.sh
```

**Option B: Postman**
- Collection öffnen
- User registrieren
- Login
- Media erstellen
- Rating erstellen

### 3. Code Durchgehen (2 Minuten)

**Öffne nacheinander:**
1. `RatingController.java` - Zeige confirm endpoint
2. `UserRepository.java` - Zeige Leaderboard SQL
3. `UserServiceTest.java` - Zeige Mocking
4. `UserService.java` - Zeige Constructor Injection
5. `UserRepository.java` - Zeige PreparedStatement

### 4. Tests Zeigen (1 Minute)
- IntelliJ: Run with Coverage
- Zeige grüne/rote Markierungen

### 5. Dokumentation Zeigen (30 Sekunden)
- `protocol.md` öffnen
- Git Historie zeigen

---

## Häufige Folgefragen - Quick Answers

### "Warum PreparedStatement?"
→ "Verhindert SQL Injection durch automatisches Escaping"

### "Was macht verify()?"
→ "Prüft ob Mock-Methode aufgerufen wurde"

### "Warum Token in DB?"
→ "Persistenz über Neustarts, Logout möglich, Session-Management"

### "Was ist der Vorteil von Layered Architecture?"
→ "Separation of Concerns, jede Schicht hat eigene Verantwortung"

### "Wie viele Tests?"
→ "52 Tests: 27 Service, 19 Controller, 6 Integration"

### "Welche SOLID-Prinzipien?"
→ "Alle 5, hauptsächlich SRP und DIP demonstriert"

---

## Fallback - Wenn etwas nicht funktioniert

### Server startet nicht
1. Port belegt? → `lsof -i :8080` und Kill Process
2. DB nicht erreichbar? → `docker-compose down && docker-compose up -d`

### Tests schlagen fehl
→ "Integration Tests brauchen laufenden Server, Unit Tests nicht"

### curl Script funktioniert nicht
→ Postman Collection verwenden

---

## Selbstbewusste Antworten

### "Hast du das selbst gemacht?"
**Ja, vollständig. Git-Historie zeigt kontinuierliche Entwicklung über mehrere Wochen. Alle 6 Controller, 52 Tests und komplette Dokumentation eigenständig erstellt.**

### "Warum diese Architektur?"
**Layered Architecture mit klarer Trennung: Controller für HTTP, Service für Business Logic, Repository für Datenbank. Macht Code testbar, wartbar und erweiterbar.**

### "Was würdest du anders machen?"
**Connection Pooling für bessere Performance, DTO-Pattern für API-Contracts, Logging-Framework statt System.out**

---

## Wichtige Zahlen auswendig

- **52 Unit Tests** (46 aktiv + 6 Integration skipped)
- **6 Controller** (User, Media, Rating, Favorite, Leaderboard, Recommendation)
- **4 Repositories** mit PreparedStatements
- **40+ API Endpoints**
- **Java 21** mit HttpServer
- **PostgreSQL 16** in Docker
- **49 Stunden** Entwicklungszeit

---

## IntelliJ Shortcuts für Präsentation

- `Ctrl + N` - Klasse finden
- `Ctrl + Shift + N` - Datei finden
- `Ctrl + B` - Zu Definition springen
- `Ctrl + Alt + B` - Zu Implementierung springen
- `Ctrl + F` - In Datei suchen
- `Ctrl + Shift + F` - Im Projekt suchen

---

## Vor Präsentation checken

- [ ] Docker läuft
- [ ] PostgreSQL Container läuft (`docker ps`)
- [ ] Server startet ohne Fehler
- [ ] Ein curl Test funktioniert
- [ ] IntelliJ geöffnet
- [ ] protocol.md und README.md bereit
- [ ] Test-Klasse für Coverage bereit
- [ ] Git Historie zeigbar

**Los geht's!** 💪
