#!/bin/bash

BASE_URL="http://localhost:8080"

echo "Testing Search & Filter Media"
echo "=============================="
echo ""

# Register user
echo "1. Register user..."
curl -s -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"searchuser","password":"testpass"}' > /dev/null
echo ""

# Login
echo "2. Login..."
TOKEN=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"searchuser","password":"testpass"}' | tr -d '"')
echo "Token: $TOKEN"
echo ""

# Create test media
echo "3. Creating test media..."
curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"The Matrix","mediaType":"MOVIE","genres":["Action","Sci-Fi"],"releaseYear":1999,"ageRestriction":16,"description":"Sci-fi action"}' > /dev/null

curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Breaking Bad","mediaType":"SERIES","genres":["Drama","Crime"],"releaseYear":2008,"ageRestriction":18,"description":"Crime drama"}' > /dev/null

curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"The Witcher 3","mediaType":"GAME","genres":["RPG","Action"],"releaseYear":2015,"ageRestriction":18,"description":"RPG game"}' > /dev/null

echo "Test media created"
echo ""

# Test 1: Search by title
echo "4. Search by title (Matrix)..."
curl -s -X GET "$BASE_URL/api/media?title=Matrix"
echo ""
echo ""

# Test 2: Filter by genre
echo "5. Filter by genre (Action)..."
curl -s -X GET "$BASE_URL/api/media?genre=Action"
echo ""
echo ""

# Test 3: Filter by media type
echo "6. Filter by media type (MOVIE)..."
curl -s -X GET "$BASE_URL/api/media?mediaType=MOVIE"
echo ""
echo ""

# Test 4: Filter by age restriction
echo "7. Filter by age restriction (max 16)..."
curl -s -X GET "$BASE_URL/api/media?ageRestriction=16"
echo ""
echo ""

# Test 5: Combined filters
echo "8. Combined filters (Action + MOVIE)..."
curl -s -X GET "$BASE_URL/api/media?genre=Action&mediaType=MOVIE"
echo ""
echo ""

echo "Search & Filter tests complete!"
