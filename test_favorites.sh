#!/bin/bash

# Test script for Favorites System
BASE_URL="http://localhost:8080"
CONTENT_TYPE="Content-Type: application/json"

echo "=========================================="
echo "Testing Favorites System"
echo "=========================================="
echo ""

# Step 1: Register a test user
echo "1. Registering test user..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/register" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "favoriteUser",
    "password": "password123",
    "email": "favorite@test.com"
  }')
echo "Response: $REGISTER_RESPONSE"
echo ""

# Step 2: Login to get token
echo "2. Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "favoriteUser",
    "password": "password123"
  }')
echo "Response: $LOGIN_RESPONSE"

# Extract token (simple extraction - works if token is the only field or first field)
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
    echo "ERROR: Could not extract token. Trying alternative method..."
    TOKEN=$(echo $LOGIN_RESPONSE | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
fi

if [ -z "$TOKEN" ]; then
    echo "ERROR: Could not extract token from response!"
    echo "Response was: $LOGIN_RESPONSE"
    exit 1
fi

echo "Token extracted: ${TOKEN:0:20}..."
echo ""

# Step 3: Create a media entry
echo "3. Creating a media entry..."
CREATE_MEDIA=$(curl -s -X POST "$BASE_URL/api/media" \
  -H "$CONTENT_TYPE" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Favorite Movie",
    "description": "A great movie to favorite",
    "mediaType": "MOVIE",
    "releaseYear": 2024,
    "genres": ["Action", "Adventure"],
    "ageRestriction": 12
  }')
echo "Response: $CREATE_MEDIA"

# Extract media ID
MEDIA_ID=$(echo $CREATE_MEDIA | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
if [ -z "$MEDIA_ID" ]; then
    echo "ERROR: Could not extract media ID!"
    exit 1
fi
echo "Media ID: $MEDIA_ID"
echo ""

# Step 4: Add to favorites
echo "4. Adding media to favorites..."
ADD_FAV=$(curl -s -X POST "$BASE_URL/api/users/favoriteUser/favorites/$MEDIA_ID" \
  -H "$CONTENT_TYPE" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $ADD_FAV"
echo ""

# Step 5: Get favorites list
echo "5. Getting favorites list..."
GET_FAV=$(curl -s -X GET "$BASE_URL/api/users/favoriteUser/favorites" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $GET_FAV"
echo ""

# Step 6: Check if media is in favorites
echo "6. Checking if media is favorited..."
CHECK_FAV=$(curl -s -X GET "$BASE_URL/api/users/favoriteUser/favorites/check/$MEDIA_ID" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $CHECK_FAV"
echo ""

# Step 7: Toggle favorite (should remove)
echo "7. Toggling favorite (should remove)..."
TOGGLE_FAV=$(curl -s -X POST "$BASE_URL/api/users/favoriteUser/favorites/$MEDIA_ID/toggle" \
  -H "$CONTENT_TYPE" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $TOGGLE_FAV"
echo ""

# Step 8: Check again (should not be favorited)
echo "8. Checking if media is favorited (should be false)..."
CHECK_FAV2=$(curl -s -X GET "$BASE_URL/api/users/favoriteUser/favorites/check/$MEDIA_ID" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $CHECK_FAV2"
echo ""

# Step 9: Toggle again (should add back)
echo "9. Toggling favorite again (should add)..."
TOGGLE_FAV2=$(curl -s -X POST "$BASE_URL/api/users/favoriteUser/favorites/$MEDIA_ID/toggle" \
  -H "$CONTENT_TYPE" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $TOGGLE_FAV2"
echo ""

# Step 10: Remove favorite explicitly
echo "10. Removing favorite explicitly..."
REMOVE_FAV=$(curl -s -X DELETE "$BASE_URL/api/users/favoriteUser/favorites/$MEDIA_ID" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $REMOVE_FAV"
echo ""

# Step 11: Get empty favorites list
echo "11. Getting favorites list (should be empty)..."
GET_FAV2=$(curl -s -X GET "$BASE_URL/api/users/favoriteUser/favorites" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $GET_FAV2"
echo ""

echo "=========================================="
echo "Favorites System Test Complete!"
echo "=========================================="
