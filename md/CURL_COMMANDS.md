# curl-Requests für API-Tests
# Kopiere die Befehle einzeln in dein Terminal

## 1. USER REGISTRATION & LOGIN

# User registrieren
curl -X POST "http://localhost:8080/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'

# User login (speichere Token in Variable)
TOKEN=$(curl -s -X POST "http://localhost:8080/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' | tr -d '"')
echo $TOKEN

# User-Info abrufen
curl -X GET "http://localhost:8080/api/users/testuser" \
  -H "Authorization: Bearer $TOKEN"

## 2. MEDIA MANAGEMENT

# Neues Medium erstellen (Film)
curl -X POST "http://localhost:8080/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Matrix",
    "mediaType":"MOVIE",
    "genre":"Sci-Fi",
    "releaseYear":1999,
    "director":"Wachowski Sisters",
    "description":"A computer hacker learns about the true nature of reality"
  }'

# Serie erstellen
curl -X POST "http://localhost:8080/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"Breaking Bad",
    "mediaType":"SERIES",
    "genre":"Crime Drama",
    "releaseYear":2008,
    "director":"Vince Gilligan",
    "description":"A chemistry teacher turns to crime"
  }'

# Game erstellen
curl -X POST "http://localhost:8080/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Witcher 3",
    "mediaType":"GAME",
    "genre":"RPG",
    "releaseYear":2015,
    "director":"CD Projekt Red",
    "description":"Open world RPG"
  }'

# Alle Medien abrufen (kein Token nötig)
curl -X GET "http://localhost:8080/api/media"

# Spezifisches Medium abrufen (ID anpassen)
curl -X GET "http://localhost:8080/api/media/1"

# Medium aktualisieren (ID anpassen)
curl -X PUT "http://localhost:8080/api/media/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Matrix Updated",
    "mediaType":"MOVIE",
    "genre":"Sci-Fi",
    "releaseYear":1999,
    "director":"Wachowski Sisters",
    "description":"Updated description"
  }'

# Medium löschen (ID anpassen)
curl -X DELETE "http://localhost:8080/api/media/1" \
  -H "Authorization: Bearer $TOKEN"

## 3. RATING SYSTEM

# Rating erstellen (mediaId anpassen)
curl -X POST "http://localhost:8080/api/media/1/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "score":9,
    "comment":"Excellent movie!"
  }'

# Rating mit nur Score (ohne Kommentar)
curl -X POST "http://localhost:8080/api/media/1/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"score":8}'

# Alle Ratings für Medium abrufen (kein Token nötig, mediaId anpassen)
curl -X GET "http://localhost:8080/api/media/1/ratings"

# Rating-History eines Users abrufen
curl -X GET "http://localhost:8080/api/users/testuser/rating-history" \
  -H "Authorization: Bearer $TOKEN"

# Rating liken (ratingId anpassen, anderer User nötig)
curl -X POST "http://localhost:8080/api/ratings/1/like" \
  -H "Authorization: Bearer $TOKEN"

# Rating-Kommentar aktualisieren (ratingId anpassen)
curl -X PATCH "http://localhost:8080/api/ratings/1/comment" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"comment":"Updated comment text"}'

# Rating-Kommentar löschen (ratingId anpassen)
curl -X DELETE "http://localhost:8080/api/ratings/1/comment" \
  -H "Authorization: Bearer $TOKEN"

# Rating komplett löschen (ratingId anpassen)
curl -X DELETE "http://localhost:8080/api/ratings/1" \
  -H "Authorization: Bearer $TOKEN"

# Rating bestätigen/moderieren (ratingId anpassen)
curl -X POST "http://localhost:8080/api/ratings/1/confirm" \
  -H "Authorization: Bearer $TOKEN"

## 4. FAVORITES SYSTEM

# Medium zu Favoriten hinzufügen (mediaId anpassen)
curl -X POST "http://localhost:8080/api/users/testuser/favorites/1" \
  -H "Authorization: Bearer $TOKEN"

# Alle Favoriten abrufen
curl -X GET "http://localhost:8080/api/users/testuser/favorites" \
  -H "Authorization: Bearer $TOKEN"

# Prüfen ob Medium Favorit ist (mediaId anpassen)
curl -X GET "http://localhost:8080/api/users/testuser/favorites/check/1" \
  -H "Authorization: Bearer $TOKEN"

# Favoriten-Status togglen (mediaId anpassen)
curl -X POST "http://localhost:8080/api/users/testuser/favorites/1/toggle" \
  -H "Authorization: Bearer $TOKEN"

# Medium aus Favoriten entfernen (mediaId anpassen)
curl -X DELETE "http://localhost:8080/api/users/testuser/favorites/1" \
  -H "Authorization: Bearer $TOKEN"

## 5. ERROR TESTS

# Login mit falschen Credentials
curl -X POST "http://localhost:8080/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"wrong","password":"wrong"}'

# Request ohne Token (sollte 401 zurückgeben)
curl -X POST "http://localhost:8080/api/media" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","mediaType":"MOVIE"}'

# Nicht existierendes Medium abrufen
curl -X GET "http://localhost:8080/api/media/99999"

# Ungültiger Token
curl -X GET "http://localhost:8080/api/users/testuser" \
  -H "Authorization: Bearer invalid-token"

## TIPPS:

# Token in Variable speichern:
TOKEN=$(curl -s -X POST "http://localhost:8080/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' | tr -d '"')

# Schöne JSON-Ausgabe mit jq (falls installiert):
curl -X GET "http://localhost:8080/api/media" | jq

# HTTP-Status anzeigen:
curl -w "\nStatus: %{http_code}\n" -X GET "http://localhost:8080/api/media"

# Verbose Modus (Headers anzeigen):
curl -v -X GET "http://localhost:8080/api/media"
