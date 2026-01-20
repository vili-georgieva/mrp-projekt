#!/bin/bash

# API Test Script mit curl
# Starte den Server bevor du dieses Script ausführst

BASE_URL="http://localhost:8080"

echo "=== Media Ratings Platform API Tests ==="
echo ""

# ============================================
# 1. USER REGISTRATION & LOGIN
# ============================================

echo "1. User registrieren"
curl -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' \
  -w "\nStatus: %{http_code}\n\n"

echo "2. User login und Token erhalten"
TOKEN=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' | tr -d '"')
echo "Token: $TOKEN"
echo ""

echo "3. User-Info abrufen"
curl -X GET "$BASE_URL/api/users/testuser" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n\n"

# ============================================
# 2. MEDIA MANAGEMENT
# ============================================

echo "4. Neues Medium erstellen (Film)"
MEDIA_RESPONSE=$(curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Matrix",
    "mediaType":"MOVIE",
    "genre":"Sci-Fi",
    "releaseYear":1999,
    "director":"Wachowski Sisters",
    "description":"A computer hacker learns about the true nature of reality"
  }')
echo "$MEDIA_RESPONSE"
MEDIA_ID=$(echo "$MEDIA_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "Media ID: $MEDIA_ID"
echo ""

echo "5. Alle Medien abrufen"
curl -X GET "$BASE_URL/api/media" \
  -w "\nStatus: %{http_code}\n\n"

echo "6. Spezifisches Medium abrufen"
curl -X GET "$BASE_URL/api/media/$MEDIA_ID" \
  -w "\nStatus: %{http_code}\n\n"

echo "7. Medium aktualisieren"
curl -X PUT "$BASE_URL/api/media/$MEDIA_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Matrix Reloaded",
    "mediaType":"MOVIE",
    "genre":"Sci-Fi",
    "releaseYear":2003,
    "director":"Wachowski Sisters",
    "description":"Updated description"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# ============================================
# 3. RATING SYSTEM
# ============================================

echo "8. Rating für Medium erstellen"
curl -X POST "$BASE_URL/api/media/$MEDIA_ID/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "score":9,
    "comment":"Excellent movie!"
  }' \
  -w "\nStatus: %{http_code}\n\n"

echo "9. Alle Ratings für Medium abrufen"
curl -X GET "$BASE_URL/api/media/$MEDIA_ID/ratings" \
  -w "\nStatus: %{http_code}\n\n"

echo "10. Rating-History eines Users abrufen"
curl -X GET "$BASE_URL/api/users/testuser/rating-history" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n\n"

# Erstelle zweiten User für Like-Test
echo "11. Zweiten User registrieren"
curl -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser2","password":"test123"}' \
  -w "\nStatus: %{http_code}\n\n"

echo "12. Zweiten User einloggen"
TOKEN2=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser2","password":"test123"}' | tr -d '"')
echo "Token2: $TOKEN2"
echo ""

# Hole Rating ID
RATING_ID=$(curl -s -X GET "$BASE_URL/api/media/$MEDIA_ID/ratings" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo "Rating ID: $RATING_ID"
echo ""

echo "13. Rating liken (mit zweitem User)"
curl -X POST "$BASE_URL/api/ratings/$RATING_ID/like" \
  -H "Authorization: Bearer $TOKEN2" \
  -w "\nStatus: %{http_code}\n\n"

echo "14. Rating-Kommentar aktualisieren"
curl -X PATCH "$BASE_URL/api/ratings/$RATING_ID/comment" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"comment":"Updated comment: Still excellent!"}' \
  -w "\nStatus: %{http_code}\n\n"

echo "15. Rating-Kommentar löschen"
curl -X DELETE "$BASE_URL/api/ratings/$RATING_ID/comment" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n\n"

# ============================================
# 4. FAVORITES SYSTEM
# ============================================

echo "16. Medium zu Favoriten hinzufügen"
curl -X POST "$BASE_URL/api/users/testuser/favorites/$MEDIA_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n\n"

echo "17. Alle Favoriten abrufen"
curl -X GET "$BASE_URL/api/users/testuser/favorites" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n\n"

echo "18. Prüfen ob Medium Favorit ist"
curl -X GET "$BASE_URL/api/users/testuser/favorites/check/$MEDIA_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n\n"

echo "19. Favoriten-Status togglen"
curl -X POST "$BASE_URL/api/users/testuser/favorites/$MEDIA_ID/toggle" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n\n"

echo "20. Medium aus Favoriten entfernen"
curl -X DELETE "$BASE_URL/api/users/testuser/favorites/$MEDIA_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n\n"

# ============================================
# 5. DELETE OPERATIONS
# ============================================

echo "21. Rating löschen"
curl -X DELETE "$BASE_URL/api/ratings/$RATING_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n\n"

echo "22. Medium löschen"
curl -X DELETE "$BASE_URL/api/media/$MEDIA_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== Tests abgeschlossen ==="
