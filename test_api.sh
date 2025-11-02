#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=== Media Ratings Platform - Integration Tests ==="
echo ""

#new user
echo "1. Testing User Registration..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}')
echo "Response: $REGISTER_RESPONSE"
echo ""

# login
echo "2. Testing User Login..."
TOKEN=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}' | tr -d '"')
echo "Token: $TOKEN"
echo ""

#  get user profile
echo "3. Testing Get User Profile..."
curl -s -X GET "$BASE_URL/api/users/testuser" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json"
echo ""
echo ""

# create a movie
echo "4. Testing Create Media Entry (Movie)..."
MEDIA_RESPONSE=$(curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Matrix",
    "description":"A computer hacker learns about the true nature of reality",
    "mediaType":"MOVIE",
    "releaseYear":1999,
    "genres":["Action","Sci-Fi"],
    "ageRestriction":16
  }')
echo "Response: $MEDIA_RESPONSE"
MEDIA_ID=$(echo $MEDIA_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo ""

#create a series
echo "5. Testing Create Media Entry (Series)..."
curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"Breaking Bad",
    "description":"A high school chemistry teacher turned meth manufacturer",
    "mediaType":"SERIES",
    "releaseYear":2008,
    "genres":["Drama","Crime"],
    "ageRestriction":18
  }'
echo ""
echo ""

# create a game
echo "6. Testing Create Media Entry (Game)..."
curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"The Last of Us",
    "description":"Survive in a post-apocalyptic world",
    "mediaType":"GAME",
    "releaseYear":2013,
    "genres":["Action","Adventure"],
    "ageRestriction":18
  }'
echo ""
echo ""

#get all media
echo "7. Testing Get All Media..."
curl -s -X GET "$BASE_URL/api/media" \
  -H "Accept: application/json"
echo ""
echo ""

#get media by ID
if [ ! -z "$MEDIA_ID" ]; then
  echo "8. Testing Get Media by ID (ID: $MEDIA_ID)..."
  curl -s -X GET "$BASE_URL/api/media/$MEDIA_ID" \
    -H "Accept: application/json"
  echo ""
  echo ""
fi

#update media
if [ ! -z "$MEDIA_ID" ]; then
  echo "9. Testing Update Media Entry..."
  curl -s -X PUT "$BASE_URL/api/media/$MEDIA_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
      "title":"The Matrix (Updated)",
      "description":"Updated description for The Matrix",
      "mediaType":"MOVIE",
      "releaseYear":1999,
      "genres":["Action","Sci-Fi","Thriller"],
      "ageRestriction":16
    }'
  echo ""
  echo ""
fi

#test -(no token)
echo "10. Testing Unauthorized Access (no token)..."
curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","mediaType":"MOVIE","releaseYear":2024}'
echo ""
echo ""

#test invalid credentials
echo "11. Testing Invalid Login Credentials..."
curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"wrongpassword"}'
echo ""
echo ""


echo "=== Integration Tests Complete ==="

