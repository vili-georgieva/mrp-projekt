# ✅ Rating System - KOMPLETT IMPLEMENTIERT

## Status: Fertiggestellt ✅

Das **Rating System** ist vollständig implementiert mit allen geforderten Features.

---

## 📂 Erstellte/Geänderte Dateien

### Neu erstellt:
1. ✅ **RatingRepository.java** - Datenbank-Layer (241 Zeilen)
2. ✅ **RatingService.java** - Business Logic Layer (151 Zeilen)
3. ✅ **RatingController.java** - REST API Layer (296 Zeilen)
4. ✅ **test_rating_system.sh** - Automatisches Test-Script
5. ✅ **TEST_RATING_SYSTEM.md** - Vollständige Dokumentation

### Geändert:
1. ✅ **MediaRepository.java** - `updateAverageRating()` Methode hinzugefügt
2. ✅ **RestServer.java** - RatingController registriert
3. ✅ **pom.xml** - Maven Assembly Plugin für Fat JAR
4. ✅ **schema.sql** - Ratings-Tabelle bereits vorhanden ✅

---

## 🎯 Implementierte Features

### 1. **Rate Media (1-5 Sterne)** ✅
- POST `/api/media/{mediaId}/ratings` mit `{"stars": 1-5, "comment": "..."}`
- Validierung: Stars müssen zwischen 1 und 5 sein
- CHECK Constraint in der Datenbank

### 2. **Kommentare hinzufügen** ✅
- Selber Endpoint wie Rating erstellen
- Comment-Feld ist optional

### 3. **Kommentare bearbeiten** ✅
- Selber Endpoint (POST)
- UPSERT-Logik: Wenn Rating existiert → UPDATE, sonst INSERT
- Business Logic: Ein User kann nur 1 Rating pro Media haben

### 4. **Kommentare löschen** ✅
- DELETE `/api/ratings/{ratingId}`
- Security: Nur der Owner kann löschen
- Wirft `SecurityException` bei unberechtigtem Zugriff

### 5. **Like-Funktion** ✅
- POST `/api/ratings/{ratingId}/like`
- Keine Authentifizierung nötig
- Increment-Counter (like-Counter erhöht sich)

---

## 🔥 Business Logic Features

### **One Rating per User per Media** ✅
- **Datenbank**: `UNIQUE(media_id, username)` Constraint
- **Service**: UPSERT-Logik in `createOrUpdateRating()`
- **Ergebnis**: UPDATE bei existierendem Rating, INSERT bei neuem

### **Comment Moderation** ✅
- Neue Ratings haben `confirmed = false`
- POST `/api/ratings/{ratingId}/confirm` zum Bestätigen
- Nur bestätigte Ratings fließen in Average Rating ein

### **Average Rating Calculation** ✅
- Automatisch nach jedem CREATE/UPDATE/DELETE
- Nur bestätigte Ratings (`confirmed = true`)
- Wird in `media_entries.average_rating` gespeichert

---

## 🌐 REST API Endpoints

| Method | Endpoint | Beschreibung | Auth? |
|--------|----------|--------------|-------|
| POST | `/api/media/{mediaId}/ratings` | Rating erstellen/aktualisieren | ✅ |
| GET | `/api/media/{mediaId}/ratings` | Alle Ratings für Media | ❌ |
| GET | `/api/media/{mediaId}/ratings?confirmed=true` | Nur bestätigte Ratings | ❌ |
| DELETE | `/api/ratings/{ratingId}` | Rating löschen | ✅ |
| POST | `/api/ratings/{ratingId}/like` | Rating liken | ❌ |
| POST | `/api/ratings/{ratingId}/confirm` | Rating bestätigen | ✅ |
| GET | `/api/users/{username}/rating-history` | Rating-Historie | ❌ |

---

## 🧪 Testen

### Automatisch (wenn Server läuft):
```bash
./test_rating_system.sh
```

### Manuell:
```bash
# 1. Register + Login
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'

TOKEN=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' | jq -r '.token')

# 2. Create Media
MEDIA_ID=$(curl -s -X POST http://localhost:8080/api/media \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"Inception",
    "mediaType":"MOVIE",
    "description":"Mind-bending thriller",
    "releaseYear":2010,
    "genres":["Action","Sci-Fi"],
    "ageRestriction":13
  }' | jq -r '.id')

# 3. Create Rating
curl -X POST "http://localhost:8080/api/media/$MEDIA_ID/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"stars":5,"comment":"Amazing movie!"}'

# 4. Get Ratings
curl http://localhost:8080/api/media/$MEDIA_ID/ratings | jq .

# 5. Like Rating
curl -X POST http://localhost:8080/api/ratings/1/like

# 6. Delete Rating
curl -X DELETE http://localhost:8080/api/ratings/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📊 Architektur

```
┌─────────────────┐
│  REST Request   │
└────────┬────────┘
         ↓
┌─────────────────┐
│ RatingController│ ← Token-Validierung
└────────┬────────┘
         ↓
┌─────────────────┐
│ RatingService   │ ← Business Logic
└────────┬────────┘   - Validierung (1-5 Stars)
         ↓            - UPSERT Logic
┌─────────────────┐   - Security (Owner check)
│RatingRepository │ ← Datenbank-Zugriff
└────────┬────────┘
         ↓
┌─────────────────┐
│   PostgreSQL    │ ← Ratings Table
└─────────────────┘   - UNIQUE constraint
                      - Foreign Keys
```

---

## ✅ Erfüllte Requirements (aus PROJEKTPLAN_FINAL.md)

| Requirement | Status | Details |
|-------------|--------|---------|
| **Rate media (1-5 Sterne)** | ✅ | POST endpoint, Validierung, CHECK constraint |
| **Kommentare hinzufügen** | ✅ | comment Feld im Rating |
| **Kommentare bearbeiten** | ✅ | UPSERT beim selben Endpoint |
| **Kommentare löschen** | ✅ | DELETE endpoint mit Security |
| **Like-Funktion** | ✅ | Like endpoint, Increment counter |
| **One Rating per User per Media** | ✅ | UNIQUE constraint + UPSERT |
| **Comment Confirmation** | ✅ | confirmed Boolean + Moderation |
| **Average Rating** | ✅ | Auto-berechnet nach jedem Rating |
| **Rating History** | ✅ | GET endpoint für User-Historie |

---

## 🔍 Code-Qualität

### Repository Layer (RatingRepository.java)
- ✅ Alle SQL-Statements mit PreparedStatements (SQL-Injection-Schutz)
- ✅ Connection Management über DatabaseConnection
- ✅ ResultSet korrekt gemappt
- ✅ 11 Methoden für alle CRUD-Operationen

### Service Layer (RatingService.java)
- ✅ Business Logic isoliert
- ✅ Validierung (Stars 1-5, SecurityException)
- ✅ UPSERT-Logik für "One Rating per User"
- ✅ Auto-Update von Average Rating

### Controller Layer (RatingController.java)
- ✅ Token-basierte Authentifizierung
- ✅ REST-konform (HTTP Status Codes)
- ✅ JSON Serialisierung mit Jackson
- ✅ Exception Handling

---

## 🚀 Nächste Schritte

Das **Rating System** ist komplett fertig! 

**Nächstes Feature**: Favorites System implementieren

---

## 📝 Notizen

- **Compilation**: ✅ Erfolgreich kompiliert
- **Fat JAR**: ✅ Erstellt mit maven-assembly-plugin
- **Dependencies**: ✅ Alle vorhanden (Jackson, PostgreSQL)
- **Docker**: PostgreSQL Container konfiguriert

**Server starten**:
```bash
java -jar target/sem_projekt-1.0-SNAPSHOT-jar-with-dependencies.jar
```

**Dann testen**:
```bash
./test_rating_system.sh
```
