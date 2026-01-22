# TODO: Intermediate → Final

## Aktueller Stand (Intermediate)
- ✅ HTTP Server mit Routing
- ✅ User Registration/Login mit Token
- ✅ Media CRUD (Create, Read, Update, Delete)
- ✅ PostgreSQL Persistenz
- ✅ Basis-Entities: User, Media, Rating
- ✅ 13 Unit Tests vorhanden

---

## ⚡ SETUP: Kritische Fixes (VOR Phase 1!)

Diese 4 Punkte müssen als allererstes implementiert werden, da sie die Grundlage für alle folgenden Phasen sind!

### SETUP 1: Schema.sql - Score Range korrigieren
- [X] `schema.sql` Zeile 26 ändern: `CHECK (score >= 1 AND score <= 10)` → `(score >= 1 AND score <= 5)`
- [X] Test: Score > 5 sollte Error werfen

### SETUP 2: Rating Entity erweitern
- [X] `Rating.java`: Field `isConfirmed` (boolean, default: false) hinzufügen
- [X] `Rating.java`: Field `likeCount` (int, default: 0) hinzufügen
- [X] `schema.sql`: Spalte `is_confirmed` BOOLEAN DEFAULT false
- [X] `schema.sql`: Spalte `like_count` INTEGER DEFAULT 0

### SETUP 3: Media Entity erweitern
- [X] `Media.java`: Field `creatorId` (Long) hinzufügen
- [X] `schema.sql`: Spalte `creator_id` INTEGER NOT NULL (Foreign Key zu users)
- [X] `MediaDAO.save()`: creatorId mit speichern
- [X] `MediaDAO.findById()`: creatorId auslesen

### SETUP 4: Test & Verifizierung
- [X] Projekt kompiliert ohne Fehler
- [X] Tests laufen (sollten bestehen)
- [ ] DB-Schema mit Docker neu initialisiert

---

## Phase 1: Rating-System (Priorität: Hoch)

### 1.1 Rating Entity erweitern
- [X] `creatorId` zu Media Entity hinzufügen (für Ownership)
- [X] `isConfirmed` (boolean) zu Rating Entity hinzufügen (Kommentar-Moderation)
- [X] `likeCount` zu Rating Entity hinzufügen
- [X] Schema.sql aktualisieren: `creator_id`, `is_confirmed` Spalten

### 1.2 RatingDAO erstellen
- [X] `save()` - Rating speichern
- [X] `findById()` - Rating nach ID
- [X] `findByMediaId()` - Alle Ratings eines Media
- [X] `findByUserId()` - Rating History eines Users
- [X] `update()` - Rating bearbeiten
- [X] `delete()` - Rating löschen
- [X] `existsByUserAndMedia()` - Prüfen ob User bereits bewertet hat

### 1.3 RatingService erstellen
- [X] `createRating()` - Ein Rating pro User pro Media (Constraint!)
- [X] `updateRating()` - Nur eigene Ratings bearbeitbar
- [X] `deleteRating()` - Nur eigene Ratings löschbar
- [X] `confirmComment()` - Kommentar bestätigen (Moderation)
- [X] `calculateAverageRating()` - Durchschnitt berechnen
- [X] `getRatingsByMediaIdPublic()` - Ratings mit Kommentar-Filter

### 1.4 RatingController erstellen
- [X] `POST /api/media/{mediaId}/rate` - Rating erstellen
- [X] `PUT /api/ratings/{ratingId}` - Rating bearbeiten
- [X] `DELETE /api/ratings/{ratingId}` - Rating löschen
- [X] `POST /api/ratings/{ratingId}/confirm` - Kommentar bestätigen
- [X] `GET /api/media/{mediaId}/ratings` - Ratings mit Average abrufen

### 1.5 Test: Rating-System
- [X] Postman: Rating erstellen
- [X] Postman: Doppeltes Rating verhindern (409)
- [X] Postman: Rating bearbeiten
- [X] Postman: Average Rating prüfen
- [X] Postman: Kommentar-Filter (nicht-bestätigte unsichtbar)

---

## Phase 2: Like-System (Priorität: Mittel)

### 2.1 RatingLike Entity/Tabelle erstellen
- [X] `rating_likes` Tabelle: (id, user_id, rating_id, created_at)
- [X] UNIQUE constraint auf (user_id, rating_id)

### 2.2 Like-Funktionalität implementieren
- [X] `POST /api/ratings/{ratingId}/like` - Rating liken
- [X] 1 Like pro User pro Rating (Constraint)
- [X] Like Count in Rating Response inkludieren

### 2.3 Test: Like-System
- [X] Postman: Rating liken
- [X] Postman: Doppeltes Like verhindern

---

## Phase 3: Favorites-System (Priorität: Mittel)

### 3.1 Favorites Tabelle erstellen
- [X] `favorites` Tabelle: (id, user_id, media_id, created_at)
- [X] UNIQUE constraint auf (user_id, media_id)

### 3.2 FavoritesDAO erstellen
- [X] `save()` - Favorit hinzufügen
- [X] `delete()` - Favorit entfernen
- [X] `findByUserId()` - Favoriten-Liste eines Users
- [X] `existsByUserAndMedia()` - Prüfen ob bereits Favorit

### 3.3 FavoritesService/Controller
- [X] `POST /api/media/{mediaId}/favorite` - Als Favorit markieren
- [X] `DELETE /api/media/{mediaId}/favorite` - Favorit entfernen
- [X] `GET /api/users/{userId}/favorites` - Favoriten-Liste

### 3.4 Test: Favorites
- [X] Postman: Favorit hinzufügen
- [X] Postman: Favoriten-Liste abrufen
- [X] Postman: Favorit entfernen

---

## Phase 4: Ownership-Logic (Priorität: Hoch)

### 4.1 Media Ownership implementieren
- [X] `creator_id` Spalte in `media` Tabelle
- [X] Media Entity um `creatorId` erweitern
- [X] Beim Erstellen: `creator_id` aus Token setzen
- [X] Update/Delete: Nur wenn User = Creator

### 4.2 Token → User Mapping verbessern
- [X] `getUserFromToken()` in AuthService → User-Objekt zurückgeben
- [X] Controller: User aus Token extrahieren

### 4.3 Test: Ownership
- [X] Postman: Eigenes Media bearbeiten (200)
- [X] Postman: Fremdes Media bearbeiten (403)

---

## Phase 5: Search & Filter (Priorität: Mittel)

### 5.1 MediaDAO erweitern
- [X] `searchByTitle()` - Partial Matching
- [X] `findWithFilters()` - Genre, mediaType, releaseYear, ageRestriction, minRating
- [X] `findAllSorted()` - Nach title, year, score sortieren

### 5.2 MediaController erweitern
- [X] Query-Parameter für `GET /api/media` verarbeiten
- [X] `?title=`, `?genre=`, `?mediaType=`, `?releaseYear=`, `?ageRestriction=`, `?rating=`
- [X] `?sortBy=title|year|score`

### 5.3 Test: Search & Filter
- [X] Postman: Suche nach Titel
- [X] Postman: Filter nach Genre
- [X] Postman: Sortierung

---

## Phase 6: User Profile & Statistics (Priorität: Mittel)

### 6.1 UserStatistics berechnen
- [X] Total Ratings Count (Pre-Final: Basis-Implementierung)
- [X] Average Score (vom User vergeben)
- [X] Favorite Genre (meistbewertetes Genre)

### 6.2 Profile-Endpoint erweitern
- [X] `GET /api/users/{username}/profile` - Statistics inkludieren (Pre-Final)
- [X] `PUT /api/users/{username}/profile` - Profile bearbeiten (optional, für Final)
- [X] `GET /api/users/{username}/ratings` - Rating History (Pre-Final)

### 6.3 Test: Profile
- [X] Postman: Profile mit Statistics abrufen (Pre-Final)
- [X] Postman: Profile aktualisieren (Pre-Final)
- [X] Postman: Rating History abrufen (Pre-Final)

---

## Phase 7: Leaderboard (Priorität: Mittel)

### 7.1 Leaderboard implementieren
- [X] Query: User nach Anzahl Ratings sortieren (Pre-Final: Basis)
- [X] `GET /api/leaderboard` Endpoint (Pre-Final)

### 7.2 Test: Leaderboard
- [X] Postman: Leaderboard abrufen (Pre-Final)

---

## Phase 8: Recommendation System (Priorität: Niedrig)

### 8.1 Genre-basierte Recommendations
- [X] Analysiere User-Ratings → bevorzugte Genres (Pre-Final)
- [X] Finde Media mit ähnlichen Genres (Pre-Final)

### 8.2 Content Similarity
- [X] Matching: Genres, mediaType, ageRestriction (Final)
- [X] Score berechnen (Final)

### 8.3 Endpoint
- [X] `GET /api/users/{userId}/recommendations` (Pre-Final: nur Genre)
- [X] Query-Param: `?type=genre|content` (Pre-Final: nur Genre)

### 8.4 Test: Recommendations
- [X] Postman: Recommendations abrufen (Pre-Final: Genre-basiert)

---

## Phase 9: Unit Tests (min. 4-5 für Pre-Final, 20+ für Final)

### Pre-Final: 4-5 korrekte Tests
- [X] 1-2 Service-Tests mit Business-Logik (Pre-Final)
- [X] 1-2 Controller-Tests mit Mocks (Pre-Final)
- [X] Entity-Tests behalten (Pre-Final)

### Final: Tests vervollständigen (min. 20)
- [ ] Tests (Final)

### Anforderungen
min. 20 unitests für Final. Für Pre-Final: 4-5 aussagekräftige Tests. Keine zu einfachen Unitests. junit-jupiter version 5+ verwenden für unitests.
JUnits sollen heißen wie die Klasse, die ich teste mit "Test" am ende angehängt. Test-Funktionen sollen heißen wie die Funktion die ich teste mit "Test am ende angehängt.
Test: Fokus auf PL, auch für REST Layer, mittel viel bei BLL, 1-2 oder keine bei DAL.
Für Unitests: Mockito oder easy mock (beides geht, voriges Jahr wurde aber easy mock von den meisten benutzt) (Mockito wurde aber in tasks_5 benutzt)
ungeföhr 1/4 der Tests sollen mockito tests sein

---

## Phase 10: Cleanup & Documentation

### 10.1 Postman Collection erweitern
- [ ] Alle neuen Endpoints hinzufügen
- [ ] Test-Scripts für Validierung

### 10.2 Protocol aktualisieren
- [ ] Architektur-Beschreibung
- [ ] SOLID Principles dokumentieren (2x)
- [ ] Unit Test Coverage erklären
- [ ] Time Tracking

### 10.3 Code Cleanup
- [ ] Alte Kommentare entfernen
- [ ] Response-Objekte vereinheitlichen

---

## Auffälligkeiten & Empfehlungen

### 💡 Empfehlungen
1. Response-DTOs erstellen für Rating, Favorites, Leaderboard
2. Request-DTOs erstellen für Rating, Profile-Update
3. Einheitliche Helper-Methode für `getUserFromToken()` in allen Controllern
4. `sendResponse()` und `readRequestBody()` in eine Utility-Klasse auslagern

### 📋 Reihenfolge
1. Ownership-Logic (benötigt für Rating-System)
2. Rating-System (Kernfunktionalität)
3. Like-System (abhängig von Rating)
4. Favorites-System (unabhängig)
5. Search & Filter (unabhängig)
6. Profile & Statistics (abhängig von Rating)
7. Leaderboard (abhängig von Rating)
8. Recommendations (abhängig von allem)
9. Tests & Documentation

---

## Geschätzte Aufwände
| Phase | Aufwand |
|-------|---------|
| Rating-System | ~3h |
| Like-System | ~1h |
| Favorites-System | ~1.5h |
| Ownership-Logic | ~1h |
| Search & Filter | ~2h |
| Profile & Statistics | ~1.5h |
| Leaderboard | ~0.5h |
| Recommendations | ~2h |
| Unit Tests | ~2h |
| Documentation | ~1h |
| **Gesamt** | **~15h** |

