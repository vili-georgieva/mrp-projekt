#!/bin/bash

BASE_URL="http://localhost:8080"

echo "Testing Recommendation System"
echo "=============================="
echo ""

# Register user
echo "1. Register user..."
curl -s -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"recouser","password":"testpass"}' > /dev/null
echo ""

# Login
echo "2. Login..."
TOKEN=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"recouser","password":"testpass"}' | tr -d '"')
echo "Token: $TOKEN"
echo ""

# Create test media with different genres
echo "3. Creating test media..."
curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Matrix","mediaType":"MOVIE","genres":["Action","Sci-Fi"],"releaseYear":1999,"ageRestriction":16}' > /dev/null

curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Inception","mediaType":"MOVIE","genres":["Action","Thriller"],"releaseYear":2010,"ageRestriction":16}' > /dev/null

curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Interstellar","mediaType":"MOVIE","genres":["Sci-Fi","Drama"],"releaseYear":2014,"ageRestriction":12}' > /dev/null

curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"The Notebook","mediaType":"MOVIE","genres":["Romance","Drama"],"releaseYear":2004,"ageRestriction":12}' > /dev/null

echo "Test media created"
echo ""

# Rate Matrix highly (to establish preference for Action/Sci-Fi)
echo "4. Rating Matrix with 5 stars..."
curl -s -X POST "$BASE_URL/api/media/1/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"stars":5,"comment":"Amazing movie!"}' > /dev/null
echo ""

# Get recommendations (should recommend Inception and Interstellar - similar genres)
echo "5. Get recommendations..."
curl -s -X GET "$BASE_URL/api/recommendations?username=recouser&limit=5" \
  -H "Authorization: Bearer $TOKEN"
echo ""
echo ""

echo "6. Get recommendations with limit 3..."
curl -s -X GET "$BASE_URL/api/recommendations?username=recouser&limit=3" \
  -H "Authorization: Bearer $TOKEN"
echo ""
echo ""

echo "Recommendation System tests complete!"
