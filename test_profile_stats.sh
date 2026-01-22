#!/bin/bash

BASE_URL="http://localhost:8080"

echo "Testing User Profile with Statistics"
echo "====================================="
echo ""

# Register user
echo "1. Register user..."
curl -s -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"statsuser","password":"testpass"}'
echo ""
echo ""

# Login
echo "2. Login..."
TOKEN=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"statsuser","password":"testpass"}' | tr -d '"')
echo "Token: $TOKEN"
echo ""

# Get user profile (should now include statistics)
echo "3. Get user profile with statistics..."
curl -s -X GET "$BASE_URL/api/users/statsuser" \
  -H "Authorization: Bearer $TOKEN" | json_pp
echo ""

# Create some media
echo "4. Create media entry..."
curl -s -X POST "$BASE_URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title":"Test Movie",
    "mediaType":"MOVIE",
    "genres":["Action"],
    "releaseYear":2024,
    "ageRestriction":12,
    "description":"Test"
  }' > /dev/null
echo "Media created"
echo ""

# Get profile again (mediaCount should be 1)
echo "5. Get profile after creating media..."
curl -s -X GET "$BASE_URL/api/users/statsuser" \
  -H "Authorization: Bearer $TOKEN" | json_pp
echo ""

echo "Test complete!"
