#!/bin/bash

# Database Check Script - Shows all data in the database

echo "========================================="
echo "DATABASE STATUS CHECK"
echo "========================================="
echo ""

# Check if Docker is running
if ! sudo docker ps | grep -q mrp_postgres; then
    echo "ERROR: PostgreSQL container is not running!"
    echo "Start it with: sudo docker-compose up -d"
    exit 1
fi

echo "✓ Docker container is running"
echo ""

# Function to run SQL query
run_query() {
    sudo docker-compose exec -T postgres psql -U postgres -d mrp_db -c "$1" 2>/dev/null
}

echo "========================================="
echo "1. TABLES IN DATABASE"
echo "========================================="
run_query "\dt"
echo ""

echo "========================================="
echo "2. USERS TABLE"
echo "========================================="
run_query "SELECT username, email, LEFT(token, 30) as token_preview, created_at FROM users ORDER BY created_at DESC LIMIT 10;"
echo ""
run_query "SELECT COUNT(*) as total_users FROM users;"
echo ""

echo "========================================="
echo "3. MEDIA ENTRIES TABLE"
echo "========================================="
run_query "SELECT id, title, media_type, release_year, creator, average_rating FROM media_entries ORDER BY id DESC LIMIT 10;"
echo ""
run_query "SELECT COUNT(*) as total_media FROM media_entries;"
echo ""

echo "========================================="
echo "4. RATINGS TABLE"
echo "========================================="
run_query "SELECT id, media_id, username, stars, LEFT(comment, 30) as comment_preview, confirmed, likes FROM ratings ORDER BY id DESC LIMIT 10;"
echo ""
run_query "SELECT COUNT(*) as total_ratings FROM ratings;"
echo ""

echo "========================================="
echo "5. FAVORITES TABLE"
echo "========================================="
run_query "SELECT f.username, f.media_id, m.title, f.added_at FROM favorites f LEFT JOIN media_entries m ON f.media_id = m.id ORDER BY f.added_at DESC LIMIT 10;"
echo ""
run_query "SELECT COUNT(*) as total_favorites FROM favorites;"
echo ""

echo "========================================="
echo "6. STATISTICS"
echo "========================================="
run_query "SELECT
    (SELECT COUNT(*) FROM users) as users,
    (SELECT COUNT(*) FROM media_entries) as media,
    (SELECT COUNT(*) FROM ratings) as ratings,
    (SELECT COUNT(*) FROM favorites) as favorites;"
echo ""

echo "========================================="
echo "7. MEDIA BY TYPE"
echo "========================================="
run_query "SELECT media_type, COUNT(*) as count FROM media_entries GROUP BY media_type;"
echo ""

echo "========================================="
echo "8. TOP RATED MEDIA"
echo "========================================="
run_query "SELECT id, title, media_type, average_rating, creator FROM media_entries WHERE average_rating > 0 ORDER BY average_rating DESC LIMIT 5;"
echo ""

echo "========================================="
echo "DATABASE CHECK COMPLETE"
echo "========================================="
