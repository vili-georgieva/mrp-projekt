#!/bin/bash

# Complete Test Script for Media Ratings Platform
# Tests all endpoints systematically

BASE_URL="http://localhost:8080"
TEST_USER="testuser_$(date +%s)"
TEST_PASS="test123"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

# Function to print section headers
print_section() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# Function to print test results
print_test() {
    TESTS_RUN=$((TESTS_RUN + 1))
    echo -e "${YELLOW}Test $TESTS_RUN: $1${NC}"
}

# Function to check response
check_response() {
    local response="$1"
    local expected="$2"
    local test_name="$3"

    if echo "$response" | grep -q "$expected"; then
        echo -e "${GREEN}✓ PASSED${NC}"
        TESTS_PASSED=$((TESTS_PASSED + 1))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC}"
        echo -e "${RED}Response: $response${NC}"
        TESTS_FAILED=$((TESTS_FAILED + 1))
        return 1
    fi
}

# Check if server is running
print_section "PRE-CHECK: Server Status"
if curl -s "$BASE_URL/api/media" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Server is running${NC}"
else
    echo -e "${RED}✗ Server is not running on $BASE_URL${NC}"
    echo "Please start the server first!"
    exit 1
fi

# ==============================================
# 1. USER REGISTRATION & LOGIN
# ==============================================
print_section "1. USER REGISTRATION & LOGIN"

# Test 1: Register new user
print_test "User Registration"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}")
check_response "$RESPONSE" "successfully" "User Registration"

# Test 2: Login and get token
print_test "User Login"
TOKEN=$(curl -s -X POST "$BASE_URL/api/users/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" | tr -d '"')

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ FAILED - No token received${NC}"
    TESTS_FAILED=$((TESTS_FAILED + 1))
    exit 1
else
    echo -e "${GREEN}✓ PASSED - Token: ${TOKEN:0:20}...${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
fi

# Test 3: Get user profile
print_test "Get User Profile"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/$TEST_USER" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "$TEST_USER" "Get User Profile"

# Test 4: Login with wrong credentials
print_test "Login with Wrong Credentials (should fail)"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"wronguser","password":"wrongpass"}')
check_response "$RESPONSE" "error" "Wrong Credentials"

# ==============================================
# 2. MEDIA MANAGEMENT
# ==============================================
print_section "2. MEDIA MANAGEMENT"

# Test 5: Create Movie
print_test "Create Movie"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/media" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "title":"The Matrix",
        "mediaType":"MOVIE",
        "genres":["Sci-Fi","Action"],
        "releaseYear":1999,
        "ageRestriction":16,
        "description":"A computer hacker learns about reality"
    }')
MOVIE_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
check_response "$RESPONSE" "Matrix" "Create Movie"
echo "Movie ID: $MOVIE_ID"

# Test 6: Create Series
print_test "Create Series"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/media" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "title":"Breaking Bad",
        "mediaType":"SERIES",
        "genres":["Crime","Drama"],
        "releaseYear":2008,
        "ageRestriction":18,
        "description":"A chemistry teacher turns to crime"
    }')
SERIES_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
check_response "$RESPONSE" "Breaking Bad" "Create Series"
echo "Series ID: $SERIES_ID"

# Test 7: Create Game
print_test "Create Game"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/media" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "title":"The Witcher 3",
        "mediaType":"GAME",
        "genres":["RPG","Adventure"],
        "releaseYear":2015,
        "ageRestriction":18,
        "description":"Open world RPG"
    }')
GAME_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
check_response "$RESPONSE" "Witcher" "Create Game"
echo "Game ID: $GAME_ID"

# Test 8: Get all media
print_test "Get All Media"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/media")
check_response "$RESPONSE" "Matrix" "Get All Media"

# Test 9: Get specific media
print_test "Get Specific Media"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/media/$MOVIE_ID")
check_response "$RESPONSE" "Matrix" "Get Specific Media"

# Test 10: Update media
print_test "Update Media"
RESPONSE=$(curl -s -X PUT "$BASE_URL/api/media/$MOVIE_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "title":"The Matrix Reloaded",
        "mediaType":"MOVIE",
        "genres":["Sci-Fi","Action"],
        "releaseYear":2003,
        "ageRestriction":16,
        "description":"Neo returns"
    }')
check_response "$RESPONSE" "Reloaded" "Update Media"

# ==============================================
# 3. RATING SYSTEM
# ==============================================
print_section "3. RATING SYSTEM"

# Test 11: Create rating with comment
print_test "Create Rating with Comment"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/media/$MOVIE_ID/ratings" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "stars":5,
        "comment":"Excellent movie!"
    }')
RATING_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
check_response "$RESPONSE" "Excellent" "Create Rating"
echo "Rating ID: $RATING_ID"

# Test 12: Create rating without comment
print_test "Create Rating without Comment"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/media/$SERIES_ID/ratings" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"stars":5}')
check_response "$RESPONSE" "\"stars\":5" "Create Rating without Comment"

# Test 13: Get all ratings for media
print_test "Get All Ratings for Media"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/media/$MOVIE_ID/ratings")
check_response "$RESPONSE" "Excellent" "Get Ratings"

# Test 14: Get user rating history
print_test "Get User Rating History"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/$TEST_USER/rating-history" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "stars\|Excellent" "Get Rating History"

# Test 15: Update rating comment
print_test "Update Rating Comment"
RESPONSE=$(curl -s -X PATCH "$BASE_URL/api/ratings/$RATING_ID/comment" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"comment":"Updated: Still an excellent movie!"}')
check_response "$RESPONSE" "Updated" "Update Comment"

# Test 16: Invalid rating score (should fail)
print_test "Invalid Rating Score (should fail)"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/media/$MOVIE_ID/ratings" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"stars":6}')
check_response "$RESPONSE" "error" "Invalid Score"

# ==============================================
# 4. FAVORITES SYSTEM
# ==============================================
print_section "4. FAVORITES SYSTEM"

# Test 17: Add to favorites
print_test "Add Media to Favorites"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/$TEST_USER/favorites/$MOVIE_ID" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "added\|success" "Add Favorite"

# Test 18: Get all favorites
print_test "Get All Favorites"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/$TEST_USER/favorites" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "favorites\|success\|Matrix" "Get Favorites"

# Test 19: Check if media is favorite
print_test "Check if Media is Favorite"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/$TEST_USER/favorites/check/$MOVIE_ID" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "true\|favorite" "Check Favorite"

# Test 20: Toggle favorite (remove)
print_test "Toggle Favorite Status (remove)"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/$TEST_USER/favorites/$MOVIE_ID/toggle" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "removed\|success" "Toggle Favorite"

# Test 21: Toggle favorite (add back)
print_test "Toggle Favorite Status (add back)"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/$TEST_USER/favorites/$MOVIE_ID/toggle" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "added\|success" "Toggle Favorite Back"

# Test 22: Add second favorite
print_test "Add Second Favorite"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/$TEST_USER/favorites/$SERIES_ID" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "added\|success" "Add Second Favorite"

# ==============================================
# 5. ERROR HANDLING & EDGE CASES
# ==============================================
print_section "5. ERROR HANDLING & EDGE CASES"

# Test 23: Access without token
print_test "Access without Token (should fail)"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/media" \
    -H "Content-Type: application/json" \
    -d '{"title":"Test"}')
check_response "$RESPONSE" "Unauthorized\|error" "No Token"

# Test 24: Invalid token
print_test "Invalid Token (should fail)"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/$TEST_USER" \
    -H "Authorization: Bearer invalid-token-123")
check_response "$RESPONSE" "Unauthorized\|error" "Invalid Token"

# Test 25: Get non-existent media
print_test "Get Non-existent Media (should fail)"
RESPONSE=$(curl -s -X GET "$BASE_URL/api/media/99999")
check_response "$RESPONSE" "not found\|error" "Non-existent Media"

# Test 26: Duplicate user registration
print_test "Duplicate User Registration (should fail)"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}")
check_response "$RESPONSE" "exists\|error" "Duplicate User"

# ==============================================
# 6. CLEANUP & DELETE OPERATIONS
# ==============================================
print_section "6. CLEANUP & DELETE OPERATIONS"

# Test 27: Delete rating comment
print_test "Delete Rating Comment"
RESPONSE=$(curl -s -X DELETE "$BASE_URL/api/ratings/$RATING_ID/comment" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "comment\|stars\|success" "Delete Comment"

# Test 28: Remove from favorites
print_test "Remove Media from Favorites"
RESPONSE=$(curl -s -X DELETE "$BASE_URL/api/users/$TEST_USER/favorites/$MOVIE_ID" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "removed\|success" "Remove Favorite"

# Test 29: Delete rating
print_test "Delete Rating"
RESPONSE=$(curl -s -X DELETE "$BASE_URL/api/ratings/$RATING_ID" \
    -H "Authorization: Bearer $TOKEN")
check_response "$RESPONSE" "deleted\|success\|message" "Delete Rating"

# Test 30: Delete media
print_test "Delete Media"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/api/media/$GAME_ID" \
    -H "Authorization: Bearer $TOKEN")
if [ "$HTTP_CODE" = "204" ] || [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ PASSED${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ FAILED (HTTP $HTTP_CODE)${NC}"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

# ==============================================
# FINAL REPORT
# ==============================================
print_section "TEST SUMMARY"

echo ""
echo -e "${BLUE}Total Tests Run: ${NC}$TESTS_RUN"
echo -e "${GREEN}Tests Passed: ${NC}$TESTS_PASSED"
echo -e "${RED}Tests Failed: ${NC}$TESTS_FAILED"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}ALL TESTS PASSED! ✓${NC}"
    echo -e "${GREEN}========================================${NC}"
    exit 0
else
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}SOME TESTS FAILED! ✗${NC}"
    echo -e "${RED}========================================${NC}"
    exit 1
fi
