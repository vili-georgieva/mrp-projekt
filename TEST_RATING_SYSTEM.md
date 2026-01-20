# Rating System - Implementierung und Tests

## ✅ Implementierte Features

### 1. **Datenbank-Schema** (schema.sql)
- ✅ `ratings` Tabelle mit allen Feldern:
  - `id`, `media_id`, `username`, `stars` (1-5)
  - `comment`, `confirmed`, `likes`
  - `created_at`, `updated_at`
  - **UNIQUE Constraint**: Ein User kann nur 1 Rating pro Media erstellen
  - **Foreign Keys**: Verbindung zu `media_entries` und `users`

### 2. **Model Layer** (Rating.java)
- ✅ Rating-Klasse mit allen Properties
- ✅ Getter/Setter für: id, mediaId, username, stars, comment, likes, confirmed, timestamp

### 3. **Repository Layer** (RatingRepository.java)
Implementierte Methoden:
- ✅ `createRating()` - Erstellt Rating mit UPSERT (ON CONFLICT)
- ✅ `updateRating()` - Aktualisiert Sterne und Kommentar
- ✅ `deleteRating()` - Löscht Rating
- ✅ `getRatingById()` - Holt Rating nach ID
- ✅ `getRatingsByMediaId()` - Alle Ratings für ein Media
- ✅ `getConfirmedRatingsByMediaId()` - Nur bestätigte Ratings
- ✅ `getRatingByMediaAndUser()` - Prüft ob Rating existiert
- ✅ `getRatingsByUser()` - Rating-Historie eines Users
- ✅ `incrementLikes()` - Like-Counter erhöhen
- ✅ `confirmRating()` - Rating bestätigen (Moderation)
- ✅ `getAverageRating()` - Durchschnittsbewertung berechnen

### 4. **Service Layer** (RatingService.java)
Business Logic:
- ✅ `createOrUpdateRating()` 
  - Validiert Stars (1-5)
  - Prüft ob Rating existiert → Update oder Create
  - Setzt `confirmed=false` für neue Kommentare (Moderation)
  - Berechnet automatisch Average Rating nach jedem Rating
- ✅ `deleteRating()` - Nur Owner kann löschen (Security Check)
- ✅ `likeRating()` - Like-Funktion
- ✅ `confirmRating()` - Moderation (setzt confirmed=true)
- ✅ `getRatingsByMediaId()` - Alle Ratings
- ✅ `getConfirmedRatingsByMediaId()` - Nur bestätigte
- ✅ `getRatingHistory()` - User Rating History
- ✅ `getUserRatingForMedia()` - Prüft ob User bereits bewertet hat

### 5. **Controller Layer** (RatingController.java)
REST API Endpoints:
- ✅ `POST /api/media/{mediaId}/ratings` - Rating erstellen/aktualisieren
- ✅ `GET /api/media/{mediaId}/ratings` - Alle Ratings für Media
- ✅ `GET /api/media/{mediaId}/ratings?confirmed=true` - Nur bestätigte Ratings
- ✅ `DELETE /api/ratings/{ratingId}` - Rating löschen (mit Auth)
- ✅ `POST /api/ratings/{ratingId}/like` - Rating liken
- ✅ `POST /api/ratings/{ratingId}/confirm` - Rating bestätigen (Moderation)
- ✅ `GET /api/users/{username}/rating-history` - Rating-Historie

### 6. **MediaRepository Erweiterung**
- ✅ `updateAverageRating()` - Aktualisiert average_rating Feld in media_entries

---

## 🔧 API Verwendung

### Beispiel 1: Rating erstellen (1-5 Sterne + Kommentar)
```bash
curl -X POST http://localhost:8080/api/media/1/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "stars": 5,
    "comment": "Absolutely amazing! A masterpiece."
  }'
```

**Response:**
```json
{
  "id": 1,
  "mediaId": 1,
  "username": "john_doe",
  "stars": 5,
  "comment": "Absolutely amazing! A masterpiece.",
  "confirmed": false,
  "likes": 0,
  "timestamp": "2026-01-15T14:30:00"
}
```

### Beispiel 2: Rating aktualisieren (selber Endpoint!)
```bash
curl -X POST http://localhost:8080/api/media/1/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "stars": 4,
    "comment": "Still great, but second viewing was less impressive."
  }'
```

### Beispiel 3: Alle Ratings für ein Media abrufen
```bash
curl -X GET http://localhost:8080/api/media/1/ratings
```

**Response:**
```json
[
  {
    "id": 1,
    "mediaId": 1,
    "username": "john_doe",
    "stars": 5,
    "comment": "Amazing!",
    "confirmed": true,
    "likes": 12,
    "timestamp": "2026-01-15T14:30:00"
  },
  {
    "id": 2,
    "mediaId": 1,
    "username": "jane_smith",
    "stars": 4,
    "comment": "Pretty good",
    "confirmed": true,
    "likes": 5,
    "timestamp": "2026-01-15T15:00:00"
  }
]
```

### Beispiel 4: Rating löschen
```bash
curl -X DELETE http://localhost:8080/api/ratings/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### Beispiel 5: Rating liken
```bash
curl -X POST http://localhost:8080/api/ratings/1/like
```

**Response:** Gibt das aktualisierte Rating zurück mit erhöhtem `likes` Counter.

### Beispiel 6: Kommentar bestätigen (Moderation)
```bash
curl -X POST http://localhost:8080/api/ratings/1/confirm \
  -H "Authorization: Bearer <TOKEN>"
```

### Beispiel 7: Rating-Historie eines Users
```bash
curl -X GET http://localhost:8080/api/users/john_doe/rating-history
```

---

## 🎯 Business Logic Features

### 1. **One Rating per User per Media**
- Datenbank: `UNIQUE(media_id, username)` Constraint
- Service: `createOrUpdateRating()` prüft ob Rating existiert
- Wenn existiert: UPDATE, sonst: INSERT
- **Ergebnis**: Ein User kann nicht mehrere Ratings für dasselbe Media erstellen

### 2. **Comment Moderation**
- Neue Ratings haben `confirmed = false`
- Nur bestätigte Ratings erscheinen in der Average-Berechnung
- Admin kann mit `/api/ratings/{id}/confirm` Kommentare freischalten

### 3. **Average Rating Calculation**
- Automatisch nach jedem CREATE/UPDATE/DELETE
- Nur bestätigte Ratings (`confirmed = true`) fließen in Durchschnitt ein
- Wird in `media_entries.average_rating` gespeichert

### 4. **Like-Funktion**
- Jeder kann Ratings liken (keine Auth nötig)
- Increment-Counter (kein Decrement)
- Zeigt Beliebtheit von Kommentaren

### 5. **Ownership Logic**
- Nur der Owner eines Ratings kann es löschen
- Security Check in `RatingService.deleteRating()`
- Wirft `SecurityException` bei unberechtigtem Zugriff

---

## 📊 Datenfluss

```
User Request (POST /api/media/1/ratings)
    ↓
RatingController.handleCreateOrUpdateRating()
    ↓ (validates token)
    ↓
RatingService.createOrUpdateRating()
    ↓ (validates stars 1-5)
    ↓ (checks if rating exists)
    ↓
RatingRepository.createRating() [UPSERT]
    ↓
PostgreSQL (ratings table)
    ↓
RatingService.updateMediaAverageRating()
    ↓
RatingRepository.getAverageRating()
    ↓
MediaRepository.updateAverageRating()
    ↓
PostgreSQL (media_entries.average_rating updated)
```

---

## ✅ Erfüllte Requirements

| Requirement | Status | Details |
|------------|--------|---------|
| Rate media (1-5 Sterne) | ✅ | Stars validiert, CHECK constraint in DB |
| Kommentare hinzufügen | ✅ | POST /api/media/{id}/ratings |
| Kommentare bearbeiten | ✅ | Selber Endpoint (UPSERT) |
| Kommentare löschen | ✅ | DELETE /api/ratings/{id} |
| Like-Funktion | ✅ | POST /api/ratings/{id}/like |
| One Rating per User per Media | ✅ | UNIQUE constraint + UPSERT logic |
| Comment Confirmation | ✅ | confirmed Boolean + Moderation endpoint |
| Average Rating | ✅ | Auto-berechnet nach jedem Rating |
| Rating History | ✅ | GET /api/users/{username}/rating-history |

---

## 🧪 Manuelle Tests (wenn DB läuft)

```bash
#!/bin/bash

# 1. Register User
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ratingtest","password":"test123"}'

# 2. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ratingtest","password":"test123"}' | jq -r '.token')

# 3. Create Media
MEDIA_ID=$(curl -s -X POST http://localhost:8080/api/media \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"Test Movie",
    "mediaType":"MOVIE",
    "description":"Test",
    "releaseYear":2024,
    "genres":["Action"],
    "ageRestriction":12
  }' | jq -r '.id')

# 4. Create Rating
curl -X POST "http://localhost:8080/api/media/$MEDIA_ID/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"stars":5,"comment":"Excellent movie!"}'

# 5. Get Ratings
curl -X GET "http://localhost:8080/api/media/$MEDIA_ID/ratings"

# 6. Like Rating
curl -X POST "http://localhost:8080/api/ratings/1/like"

# 7. Delete Rating
curl -X DELETE "http://localhost:8080/api/ratings/1" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📝 Zusammenfassung

**Das Rating System ist vollständig implementiert!**

✅ **Alle 4 Layers**: Model, Repository, Service, Controller  
✅ **Alle Features**: Rate (1-5), Comment, Edit, Delete, Like  
✅ **Business Logic**: One Rating per User, Moderation, Average Calculation  
✅ **Security**: Token-basierte Auth, Ownership Checks  
✅ **REST API**: 7 Endpoints vollständig dokumentiert  

**Nächster Schritt**: Favorites System implementieren (separates Feature)
