#!/bin/bash

# MRP - Complete Integration Test Script
# Tests all API endpoints

BASE_URL="http://localhost:8080"
TIMESTAMP=$(date +%s)
USERNAME="testuser_$TIMESTAMP"

echo "========================================"
echo "MRP - Complete API Integration Tests"
echo "========================================"
echo ""

# Check if server is running
if ! curl -s "$BASE_URL/api/media" > /dev/null 2>&1; then
    echo "ERROR: Server not running on port 8080"
    exit 1
fi
echo "Server is running"
echo ""

# ========================================
# 1. USER MANAGEMENT
# ========================================
echo "========================================"
echo "1. USER MANAGEMENT"
echo "========================================"

echo "1.1 Register user..."
REGISTER=$(curl -s -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"testpass123\"}")
echo "Response: $REGISTER"

echo ""
echo "1.2 Login..."
LOGIN=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"testpass123\"}")
TOKEN=$(echo "$LOGIN" | tr -d '"')
echo "Token: ${TOKEN:0:30}..."

echo ""
echo "1.3 Get user profile..."
curl -s -X GET "$BASE_URL/api/users/$USERNAME" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "1.4 Update user profile (change password)..."
curl -s -X PUT "$BASE_URL/api/users/$USERNAME" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"password":"newpassword123"}'
echo ""

echo ""
echo "1.5 Login with new password..."
NEW_TOKEN=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"newpassword123\"}" | tr -d '"')
echo "New Token: ${NEW_TOKEN:0:30}..."
TOKEN=$NEW_TOKEN

echo ""
echo "1.6 Login with wrong password (should fail)..."
curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"wrongpass\"}"
echo ""

# ========================================
# 2. MEDIA MANAGEMENT
# ========================================
echo ""
echo "========================================"
echo "2. MEDIA MANAGEMENT"
echo "========================================"

echo "2.1 Create Movie..."
MOVIE=$(curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Matrix",
    "description":"Sci-fi action movie",
    "mediaType":"MOVIE",
    "releaseYear":1999,
    "genres":["Action","Sci-Fi"],
    "ageRestriction":16
  }')
echo "Response: $MOVIE"
MOVIE_ID=$(echo "$MOVIE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo "Movie ID: $MOVIE_ID"

echo ""
echo "2.2 Create Series..."
SERIES=$(curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"Breaking Bad",
    "description":"Crime drama series",
    "mediaType":"SERIES",
    "releaseYear":2008,
    "genres":["Drama","Crime"],
    "ageRestriction":18
  }')
echo "Response: $SERIES"
SERIES_ID=$(echo "$SERIES" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

echo ""
echo "2.3 Create Game..."
GAME=$(curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Witcher 3",
    "description":"RPG game",
    "mediaType":"GAME",
    "releaseYear":2015,
    "genres":["RPG","Action"],
    "ageRestriction":18
  }')
echo "Response: $GAME"
GAME_ID=$(echo "$GAME" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

echo ""
echo "2.4 Get all media..."
curl -s -X GET "$BASE_URL/api/media"
echo ""

echo ""
echo "2.5 Get media by ID..."
curl -s -X GET "$BASE_URL/api/media/$MOVIE_ID"
echo ""

echo ""
echo "2.6 Update media..."
curl -s -X PUT "$BASE_URL/api/media/$MOVIE_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Matrix (Remastered)",
    "description":"Updated description",
    "mediaType":"MOVIE",
    "releaseYear":1999,
    "genres":["Action","Sci-Fi","Thriller"],
    "ageRestriction":16
  }'
echo ""

# ========================================
# 3. SEARCH & FILTER
# ========================================
echo ""
echo "========================================"
echo "3. SEARCH & FILTER"
echo "========================================"

echo "3.1 Search by title..."
curl -s -X GET "$BASE_URL/api/media?title=Matrix"
echo ""

echo ""
echo "3.2 Filter by genre..."
curl -s -X GET "$BASE_URL/api/media?genre=Action"
echo ""

echo ""
echo "3.3 Filter by media type..."
curl -s -X GET "$BASE_URL/api/media?mediaType=MOVIE"
echo ""

echo ""
echo "3.4 Filter by age restriction..."
curl -s -X GET "$BASE_URL/api/media?ageRestriction=16"
echo ""

echo ""
echo "3.5 Combined filters..."
curl -s -X GET "$BASE_URL/api/media?genre=Action&mediaType=MOVIE"
echo ""

# ========================================
# 4. RATING SYSTEM
# ========================================
echo ""
echo "========================================"
echo "4. RATING SYSTEM"
echo "========================================"

echo "4.1 Create rating with comment..."
RATING=$(curl -s -X POST "$BASE_URL/api/media/$MOVIE_ID/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"stars":5,"comment":"Amazing movie!"}')
echo "Response: $RATING"
RATING_ID=$(echo "$RATING" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo "Rating ID: $RATING_ID"

echo ""
echo "4.2 Create rating without comment..."
curl -s -X POST "$BASE_URL/api/media/$SERIES_ID/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"stars":4}'
echo ""

echo ""
echo "4.3 Update rating via PUT..."
curl -s -X PUT "$BASE_URL/api/ratings/$RATING_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"stars":4,"comment":"Updated via PUT endpoint"}'
echo ""

echo ""
echo "4.4 Get ratings for media..."
curl -s -X GET "$BASE_URL/api/media/$MOVIE_ID/ratings"
echo ""

echo ""
echo "4.5 Get user rating history..."
curl -s -X GET "$BASE_URL/api/users/$USERNAME/rating-history" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "4.6 Update rating comment..."
curl -s -X PATCH "$BASE_URL/api/ratings/$RATING_ID/comment" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"comment":"Updated: Best movie ever!"}'
echo ""

echo ""
echo "4.7 Like rating..."
curl -s -X POST "$BASE_URL/api/ratings/$RATING_ID/like" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "4.8 Confirm rating..."
curl -s -X POST "$BASE_URL/api/ratings/$RATING_ID/confirm" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "4.9 Invalid rating (stars > 5)..."
curl -s -X POST "$BASE_URL/api/media/$GAME_ID/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"stars":10}'
echo ""

# ========================================
# 5. FAVORITES SYSTEM
# ========================================
echo ""
echo "========================================"
echo "5. FAVORITES SYSTEM"
echo "========================================"

echo "5.1 Add to favorites..."
curl -s -X POST "$BASE_URL/api/users/$USERNAME/favorites/$MOVIE_ID" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "5.2 Get all favorites..."
curl -s -X GET "$BASE_URL/api/users/$USERNAME/favorites" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "5.3 Check if media is favorite..."
curl -s -X GET "$BASE_URL/api/users/$USERNAME/favorites/check/$MOVIE_ID" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "5.4 Toggle favorite (remove)..."
curl -s -X POST "$BASE_URL/api/users/$USERNAME/favorites/$MOVIE_ID/toggle" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "5.5 Toggle favorite (add back)..."
curl -s -X POST "$BASE_URL/api/users/$USERNAME/favorites/$MOVIE_ID/toggle" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "5.6 Remove from favorites..."
curl -s -X DELETE "$BASE_URL/api/users/$USERNAME/favorites/$MOVIE_ID" \
  -H "Authorization: Bearer $TOKEN"
echo ""

# ========================================
# 6. LEADERBOARD
# ========================================
echo ""
echo "========================================"
echo "6. LEADERBOARD"
echo "========================================"

echo "6.1 Get leaderboard (with auth)..."
curl -s -X GET "$BASE_URL/api/leaderboard" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "6.2 Get leaderboard with limit..."
curl -s -X GET "$BASE_URL/api/leaderboard?limit=5" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "6.3 Get leaderboard without token (should fail)..."
curl -s -X GET "$BASE_URL/api/leaderboard"
echo ""

# ========================================
# 7. RECOMMENDATIONS
# ========================================
echo ""
echo "========================================"
echo "7. RECOMMENDATIONS"
echo "========================================"

echo "7.1 Get recommendations..."
curl -s -X GET "$BASE_URL/api/recommendations?username=$USERNAME" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "7.2 Get recommendations with limit..."
curl -s -X GET "$BASE_URL/api/recommendations?username=$USERNAME&limit=3" \
  -H "Authorization: Bearer $TOKEN"
echo ""

# ========================================
# 8. ERROR HANDLING
# ========================================
echo ""
echo "========================================"
echo "8. ERROR HANDLING"
echo "========================================"

echo "8.1 Access without token..."
curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","mediaType":"MOVIE"}'
echo ""

echo ""
echo "8.2 Invalid token..."
curl -s -X GET "$BASE_URL/api/users/$USERNAME" \
  -H "Authorization: Bearer invalid-token"
echo ""

echo ""
echo "8.3 Get non-existent media..."
curl -s -X GET "$BASE_URL/api/media/99999"
echo ""

echo ""
echo "8.4 Duplicate user registration..."
curl -s -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"test\"}"
echo ""

# ========================================
# 9. CLEANUP
# ========================================
echo ""
echo "========================================"
echo "9. CLEANUP"
echo "========================================"

echo "9.1 Delete rating comment..."
curl -s -X DELETE "$BASE_URL/api/ratings/$RATING_ID/comment" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "9.2 Delete rating..."
curl -s -X DELETE "$BASE_URL/api/ratings/$RATING_ID" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "9.3 Delete media..."
curl -s -X DELETE "$BASE_URL/api/media/$GAME_ID" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo ""
echo "========================================"
echo "TESTS COMPLETE"
echo "========================================"
