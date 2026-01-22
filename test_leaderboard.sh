#!/bin/bash

BASE_URL="http://localhost:8080"

echo "Testing Leaderboard"
echo "==================="
echo ""

# Create test users and ratings
echo "1. Setting up test data..."

# User 1
curl -s -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pass"}' > /dev/null

TOKEN1=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pass"}' | tr -d '"')

# User 2
curl -s -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","password":"pass"}' > /dev/null

TOKEN2=$(curl -s -X POST "$BASE_URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","password":"pass"}' | tr -d '"')

echo "Test users created"
echo ""

# Get leaderboard (default limit 10)
echo "2. Get leaderboard (default)..."
curl -s -X GET "$BASE_URL/api/leaderboard"
echo ""
echo ""

# Get leaderboard with limit
echo "3. Get leaderboard (limit 5)..."
curl -s -X GET "$BASE_URL/api/leaderboard?limit=5"
echo ""
echo ""

echo "Leaderboard tests complete!"
