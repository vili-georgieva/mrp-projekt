# Protocol - Media Ratings Platform (MRP) Intermediate Submission

## Student Information
- **Name:** Velichka Georgieva
- **Student ID:** if24b265
- **Date:** October 20, 2025
- **Submission:** Intermediate (Class 13)

## Project Overview
Implementation of a RESTful HTTP server for a Media Ratings Platform using pure Java HTTP libraries (HttpServer) without web frameworks like Spring or JSP.

## Technical Architecture

### Architecture Decisions

#### 1. Layered Architecture
The project follows a classic 3-tier architecture:
- **Controller Layer**: Handles HTTP requests/responses
- **Service Layer**: Implements business logic
- **Repository Layer**: Manages database operations

This separation ensures:
- Clear separation of concerns
- Easy testability
- Maintainability and scalability

#### 2. Technology Stack
- **Java 21**: Modern LTS version with latest features
- **HttpServer (com.sun.net.httpserver)**: Pure HTTP implementation without web frameworks
- **PostgreSQL**: Robust relational database for data persistence
- **Jackson**: Industry-standard JSON serialization/deserialization

### Class Diagram

```
┌─────────────────┐
│   Main.java     │
└────────┬────────┘
         │ creates
         ▼
┌─────────────────┐
│  RestServer     │
└────────┬────────┘
         │ initializes
         ▼
┌──────────────────────────────────────┐
│  Controllers                          │
│  ├─ UserController                    │
│  └─ MediaController                   │
└────────┬─────────────────────────────┘
         │ uses
         ▼
┌──────────────────────────────────────┐
│  Services (Business Logic)            │
│  ├─ UserService                       │
│  └─ MediaService                      │
└────────┬─────────────────────────────┘
         │ uses
         ▼
┌──────────────────────────────────────┐
│  Repositories (Data Access)           │
│  ├─ UserRepository                    │
│  └─ MediaRepository                   │
└────────┬─────────────────────────────┘
         │ uses
         ▼
┌──────────────────────────────────────┐
│  PostgreSQL Database                  │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│  Models (Domain Objects)              │
│  ├─ User                              │
│  ├─ MediaEntry                        │
│  ├─ Rating                            │
│  └─ MediaType (enum)                  │
└──────────────────────────────────────┘
```

## Implementation Details

### 1. HTTP Server Implementation
**File:** `RestServer.java`

The server uses Java's built-in `HttpServer` class to create a pure HTTP server without any web framework:
- Listens on port 8080
- Routes requests to appropriate controllers
- Manages database connection lifecycle
- Uses default thread executor for handling concurrent requests

### 2. Routing Strategy
**Implementation:** Context-based routing using HttpServer contexts

Each endpoint is registered as a separate context:
```java
server.createContext("/api/users/register", userController::handleRegister);
server.createContext("/api/users/login", userController::handleLogin);
server.createContext("/api/media", mediaController::handleMedia);
```

Controllers parse the HTTP method and path to determine the specific operation.

### 3. Authentication System
**Implementation:** Token-based authentication

**Flow:**
1. User registers with username/password
2. User logs in → receives token in format: `{username}-mrpToken`
3. Token is stored in database
4. Subsequent requests include token in `Authorization: Bearer {token}` header
5. Server validates token before processing protected endpoints

### 4. Database Schema

**Users Table:**
```sql
CREATE TABLE users (
    username VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255)
);
```

**Media Entries Table:**
```sql
CREATE TABLE media_entries (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    media_type VARCHAR(50) NOT NULL,
    release_year INTEGER,
    genres TEXT,
    age_restriction INTEGER,
    creator VARCHAR(255) NOT NULL,
    FOREIGN KEY (creator) REFERENCES users(username)
);
```

### 5. HTTP Response Codes
Proper HTTP status codes are implemented:
- **200 OK**: Successful GET/PUT requests
- **201 Created**: Successful POST requests (user registration, media creation)
- **204 No Content**: Successful DELETE requests
- **400 Bad Request**: Invalid input data
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Valid auth but insufficient permissions
- **404 Not Found**: Resource doesn't exist
- **405 Method Not Allowed**: Wrong HTTP method
- **500 Internal Server Error**: Database or server errors

### 6. JSON Serialization
Jackson library handles all JSON operations:
- Automatic serialization of Java objects to JSON
- Deserialization of JSON request bodies to Java objects
- Support for Java 8 date/time types (LocalDateTime)

## SOLID Principles Implementation

### Single Responsibility Principle (SRP)
Each class has one clear responsibility:
- **Controllers**: Handle HTTP communication only
- **Services**: Implement business logic and validation
- **Repositories**: Manage database operations only
- **Models**: Represent domain data

### Open/Closed Principle (OCP)
- Services use dependency injection, allowing extension without modification
- New media types can be added to the enum without changing existing code
- Additional repositories can be created following the same pattern

### Liskov Substitution Principle (LSP)
- Repository implementations can be swapped (e.g., for different databases)
- Services work with abstractions, not concrete implementations

### Interface Segregation Principle (ISP)
- Service methods are focused and specific
- Controllers only expose necessary methods
- No bloated interfaces with unused methods

### Dependency Inversion Principle (DIP)
- High-level modules (Controllers) depend on abstractions (Services)
- Dependencies are injected via constructors

## Integration Tests

### Postman Collection
A complete Postman collection is provided (`MRP_Postman_Collection.json`) with the following requests:
- User Registration
- User Login
- Get User Profile
- Create Media Entry
- Get All Media
- Get Media by ID
- Update Media Entry
- Delete Media Entry

### curl Script
An automated curl script (`test_api.sh`) tests all endpoints:
- Registers a test user
- Logs in and retrieves token
- Creates multiple media entries (movie, series, game)
- Retrieves all media and specific entries
- Tests unauthorized access
- Tests invalid credentials

## Problems Encountered and Solutions

### Problem 1: Routing with HttpServer
**Issue:** Java's HttpServer doesn't have built-in routing like Spring.

**Solution:** 
- Used context paths for basic routing
- Implemented custom path parsing in controllers
- Split logic by HTTP method (GET, POST, PUT, DELETE)

### Problem 2: JSON Handling
**Issue:** Manual JSON parsing would be error-prone.

**Solution:** 
- Integrated Jackson library for automatic serialization
- Added datatype module for LocalDateTime support
- Created DTOs for request/response handling

### Problem 3: Database Connection Management
**Issue:** Need to share single connection across repositories.

**Solution:**
- Created connection in RestServer initialization
- Passed connection to repositories via constructor
- Ensured proper cleanup on server shutdown

### Problem 4: Token Storage
**Issue:** Deciding where to store authentication tokens.

**Solution:**
- Stored in database (persistent across restarts)
- Simple format: `{username}-mrpToken`
- Easy to validate with database query


## Git Repository
GitHub Link: https://github.com/vili-georgieva/mrp-projekt

