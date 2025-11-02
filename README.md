# Media Ratings Platform (MRP) - Intermediate Submission

## Project Description
A RESTful HTTP server for managing media content (movies, series, games) with user registration, authentication, and CRUD operations.

## GitHub Repository
[Your GitHub Repository Link Here]

## Technologies
- Java 21
- Pure HTTP (com.sun.net.httpserver.HttpServer)
- PostgreSQL Database
- Jackson (JSON serialization)

## Prerequisites
- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+

## Database Setup

Create a PostgreSQL database:
```sql
CREATE DATABASE mrp_db;
```

Or use Docker:
```bash
docker run --name mrp-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=mrp_db -p 5432:5432 -d postgres:15
```

## Configuration
Update database credentials in `src/main/java/org/example/Main.java` if needed:
```java
String dbUrl = "jdbc:postgresql://localhost:5432/mrp_db";
String dbUser = "postgres";
String dbPassword = "postgres";
```

## Build and Run

### Build the project
```bash
mvn clean compile
```

### Run the server
```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

The server will start on `http://localhost:8080`

## Testing

### Using the curl script
```bash
chmod +x test_api.sh
./test_api.sh
```

### Using Postman
Import `MRP_Postman_Collection.json` into Postman and run the requests.

## API Endpoints

### User Management
- `POST /api/users/register` - Register a new user
- `POST /api/users/login` - Login and get authentication token
- `GET /api/users/{username}` - Get user profile (requires authentication)

### Media Management
- `POST /api/media` - Create a new media entry (requires authentication)
- `GET /api/media` - Get all media entries
- `GET /api/media/{id}` - Get a specific media entry
- `PUT /api/media/{id}` - Update a media entry (requires authentication, owner only)
- `DELETE /api/media/{id}` - Delete a media entry (requires authentication, owner only)

## Authentication
The API uses Bearer token authentication. After logging in, include the token in the Authorization header:
```
Authorization: Bearer {token}
```

## Project Structure
```
src/main/java/org/example/
├── Main.java                    # Application entry point
├── controller/                  # HTTP request handlers
│   ├── MediaController.java
│   └── UserController.java
├── dto/                         # Data Transfer Objects
│   ├── LoginRequest.java
│   └── RegisterRequest.java
├── model/                       # Domain models
│   ├── MediaEntry.java
│   ├── MediaType.java
│   ├── Rating.java
│   └── User.java
├── repository/                  # Database access layer
│   ├── MediaRepository.java
│   └── UserRepository.java
├── server/                      # HTTP server
│   └── RestServer.java
└── service/                     # Business logic
    ├── MediaService.java
    └── UserService.java
```

## Example Usage

### 1. Register a user
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'
```

### 3. Create a media entry
```bash
curl -X POST http://localhost:8080/api/media \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer testuser-mrpToken" \
  -d '{
    "title":"The Matrix",
    "description":"A sci-fi action movie",
    "mediaType":"MOVIE",
    "releaseYear":1999,
    "genres":["Action","Sci-Fi"],
    "ageRestriction":16
  }'
```

## Author
Velichka Georgieva

## License
Academic project for FH Technikum Wien

