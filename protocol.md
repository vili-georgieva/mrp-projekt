# Protocol - Media Ratings Platform (MRP)

## Student Information
- **Name:** Velichka Georgieva
- **Student ID:** if24b265
- **Date:** January 24, 2026
- **Submission:** Final

## Project Overview
Implementation of a RESTful HTTP server for a Media Ratings Platform using pure Java HTTP libraries (HttpServer) without web frameworks like Spring or JSP.

## Implemented Features

### Core Features 
- **User Management**: Registration, login, token-based authentication
- **Media Management**: CRUD operations (Create, Read, Update, Delete) with ownership validation
- **Model Classes**: User, MediaEntry, Rating, MediaType (enum)
- **HTTP Server**: Pure Java HttpServer on port 8080
- **Database**: PostgreSQL with proper schema and foreign keys
- **SQL Injection Protection**: All queries use PreparedStatements

### Rating System 
- Create/update rating (1-5 stars) with comment
- One rating per user per media (UNIQUE constraint, editable)
- Like ratings (1 like per rating per user)
- Comment moderation (confirmed flag before public visibility)
- Delete own ratings
- Update/delete comments separately
- Rating history per user
- Automatic average rating calculation per media

### Favorites System 
- Add media to favorites
- Remove media from favorites
- Toggle favorite status (add/remove in one call)
- List all user favorites with full media details
- Check if media is favorite

### Profile & Statistics 
- View user profile with statistics
- Statistics include: total media count, rating count, favorite count, average stars given
- Edit profile (update password)

### Search & Filter 
- Search media by partial title match
- Filter by genre, media type, release year, age restriction
- Filter by minimum rating
- Multiple filters combinable
- Dynamic SQL query building

### Business Logic 
- **Ownership validation** (2 points): Only creator can modify/delete media
- **One rating per user** (1 point): Enforced by UNIQUE constraint, allows updates
- **Comment moderation** (2 points): Comments only public after confirmation
- **Average rating** (3 points): Automatically calculated and stored per media

### Advanced Features
- **Leaderboard** (2 points): Top users by rating count with ranking
- **Recommendation System** (4 points): Genre-based recommendations using user's highly-rated media (4-5 stars), excludes already rated media

### API Endpoints (40+ Endpoints)
#### User Management (4 endpoints)
- POST /api/users/register
- POST /api/users/login
- GET /api/users/{username}
- PUT /api/users/{username}

#### Media Management (5 endpoints)
- GET /api/media (with optional search/filter parameters)
- GET /api/media/{id}
- POST /api/media
- PUT /api/media/{id}
- DELETE /api/media/{id}

#### Rating System (10 endpoints)
- POST /api/media/{id}/ratings
- GET /api/media/{id}/ratings
- DELETE /api/ratings/{id}
- PUT /api/ratings/{id}
- PATCH /api/ratings/{id}/comment
- DELETE /api/ratings/{id}/comment
- POST /api/ratings/{id}/like
- POST /api/ratings/{id}/confirm
- GET /api/users/{username}/rating-history

#### Favorites (5 endpoints)
- POST /api/users/{username}/favorites/{mediaId}
- DELETE /api/users/{username}/favorites/{mediaId}
- POST /api/users/{username}/favorites/{mediaId}/toggle
- GET /api/users/{username}/favorites
- GET /api/users/{username}/favorites/check/{mediaId}

#### Leaderboard & Recommendations (2 endpoints)
- GET /api/leaderboard?limit={n}
- GET /api/recommendations?username={user}&limit={n}

### Non-Functional Requirements
- **Security**: Token-based authentication (UUID tokens)
- **Password Security**: SHA-256 hashing
- **Data Persistence**: PostgreSQL in Docker
- **Testing**: 52 unit tests (JUnit 5 + Mockito)
- **Integration Testing**: Curl scripts for all endpoints
- **Documentation**: Complete README.md and protocol.md
- **SOLID Principles**: Clear implementation with examples
- **Clean Architecture**: Controller-Service-Repository pattern

## Points Overview (According to Checklist)

### Must Haves (Essential)
- Uses Java ✓
- HTTP Server listening to clients ✓
- Pure HTTP library (no Spring/JSP) ✓
- PostgreSQL Database ✓
- Prevents SQL injection ✓
- No OR-Mapping ✓
- 20+ unit tests ✓ (52 tests)

### Functional Requirements (Points)
- Model classes: Essential ✓
- Register/Login: Essential ✓
- Create/Update/Delete media (owner): 1 point ✓
- Rate media + comments + likes: 2 points ✓
- Favorites: 2 points ✓
- Profile & statistics: 1 point ✓
- Rating history & favorites list: 2 points ✓

### Business Logic (Points)
- One rating per user: 1 point ✓
- Comment moderation: 2 points ✓
- Average rating: 3 points ✓
- Recommendation system: 4 points ✓
- Search & filter: 3 points ✓
- Ownership logic: 2 points ✓
- Leaderboard: 2 points ✓

### Non-Functional (Points)
- Token security: Essential ✓
- PostgreSQL + Docker: 7 points ✓
- Unit test quality: 4 points ✓
- SOLID principles: 2 points ✓
- Integration tests: 2 points ✓

### Protocol (Points)
- App design: 0.5 points ✓
- Lessons learned: 1 point ✓
- Unit test strategy: 1 point ✓
- SOLID examples: 1 point ✓
- Time tracking: 0.5 points ✓
- Git link: Essential ✓

**Total Possible Points: 44**

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
┌──────────────────────────────────────────┐
│  Controllers                              │
│  ├─ UserController                        │
│  ├─ MediaController                       │
│  ├─ RatingController                      │
│  ├─ FavoriteController                    │
│  ├─ LeaderboardController                 │
│  └─ RecommendationController              │
└────────┬─────────────────────────────────┘
         │ uses
         ▼
┌──────────────────────────────────────────┐
│  Services (Business Logic)                │
│  ├─ UserService                           │
│  ├─ MediaService                          │
│  ├─ RatingService                         │
│  ├─ FavoriteService                       │
│  ├─ LeaderboardService                    │
│  └─ RecommendationService                 │
└────────┬─────────────────────────────────┘
         │ uses
         ▼
┌──────────────────────────────────────────┐
│  Repositories (Data Access)               │
│  ├─ UserRepository                        │
│  ├─ MediaRepository                       │
│  ├─ RatingRepository                      │
│  └─ FavoriteRepository                    │
└────────┬─────────────────────────────────┘
         │ uses
         ▼
┌──────────────────────────────────────────┐
│  PostgreSQL Database                      │
└──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│  Models (Domain Objects)                  │
│  ├─ User                                  │
│  ├─ MediaEntry                            │
│  ├─ Rating                                │
│  └─ MediaType (enum)                      │
└──────────────────────────────────────────┘
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
    genre VARCHAR(255),
    age_restriction INTEGER,
    director VARCHAR(255),
    creator VARCHAR(255) NOT NULL,
    average_rating DECIMAL(3,2) DEFAULT 0.0,
    FOREIGN KEY (creator) REFERENCES users(username) ON DELETE CASCADE
);
```

**Ratings Table:**
```sql
CREATE TABLE ratings (
    id SERIAL PRIMARY KEY,
    media_id INTEGER NOT NULL,
    username VARCHAR(255) NOT NULL,
    stars INTEGER NOT NULL CHECK (stars >= 1 AND stars <= 5),
    comment TEXT,
    confirmed BOOLEAN DEFAULT FALSE,
    likes INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (media_id) REFERENCES media_entries(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    UNIQUE(media_id, username)
);
```

**Favorites Table:**
```sql
CREATE TABLE favorites (
    username VARCHAR(255) NOT NULL,
    media_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (username, media_id),
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (media_id) REFERENCES media_entries(id) ON DELETE CASCADE
);
```

**Key Design Decisions:**
- UNIQUE constraint on (media_id, username) in ratings ensures one rating per user per media
- CASCADE deletes maintain referential integrity
- CHECK constraint validates star rating range (1-5)
- average_rating stored in media_entries for performance (updated automatically)
- confirmed flag enables comment moderation

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
- UUID-based format: Random UUID generated for each login
- Easy to validate with database query
- Token invalidation possible (logout functionality)
- Secure: unpredictable random tokens instead of username-based patterns


## Git Repository
GitHub Link: https://github.com/vili-georgieva/mrp-projekt

---

## Lessons Learned

### What Went Well

#### Repository Pattern
The decision to separate data access into repository classes proved highly beneficial:
- Clear separation between database logic and business logic
- Easy to test services by mocking repositories
- Simple to add new database operations without affecting business logic

#### Token-Based Authentication
The UUID-based token system was straightforward to implement:
- Secure random tokens instead of predictable patterns
- Simple validation through database lookup
- Easy to extend with token expiration if needed

#### Transaction Handling
Using DatabaseConnection.executeInTransaction() centralized transaction management:
- Consistent error handling across all database operations
- Automatic rollback on exceptions
- Clean code without repetitive try-catch blocks

#### Jackson for JSON
Automatic JSON serialization/deserialization saved significant development time:
- No manual JSON parsing needed
- Type-safe conversions
- Easy to add new DTOs

### What Was Challenging

#### HttpExchange Mocking for Controller Tests
Testing controllers required complex mocking of HttpExchange:
- Many interdependent methods to mock (getRequestMethod, getRequestBody, getResponseHeaders, etc.)
- Had to create helper methods to simplify test setup
- Eventually focused more on service layer testing

#### SQL Schema Design
Designing the database schema with proper foreign keys and constraints:
- UNIQUE constraint for (media_id, username) in ratings table to enforce one rating per user
- Deciding between CASCADE and RESTRICT for foreign key deletions
- Balancing normalization with query performance

#### Average Rating Calculation
Implementing real-time average rating updates:
- Initially calculated on-the-fly (slow for many ratings)
- Changed to store average_rating in media_entries table
- Update average after each rating change (better performance)

#### Genre Matching for Recommendations
Implementing genre-based recommendations with comma-separated genre strings:
- PostgreSQL doesn't have native array support in all contexts
- Used LIKE queries with SUBSTRING for genre matching
- Trade-off between simplicity and perfect accuracy

### What I Would Do Differently

#### Test-Driven Development (TDD)
Start with tests before implementing features:
- Would have caught edge cases earlier
- Better test coverage from the beginning
- Cleaner interfaces driven by test requirements

#### DTO Pattern
Use dedicated DTOs for request/response instead of domain models:
- Separation of API contract from internal models
- Better control over what data is exposed
- Easier to version the API

#### Connection Pooling
Implement database connection pooling:
- Current approach creates new connections frequently
- Connection pool would improve performance
- HikariCP or similar library would be beneficial

#### Logging Framework
Use a proper logging framework (SLF4J, Log4j) instead of System.out.println:
- Better log levels (DEBUG, INFO, WARN, ERROR)
- Configurable output (file, console, remote)
- Easier debugging in production

---

## Unit Testing Strategy and Coverage

### Test Distribution
The project includes 52 unit tests distributed across layers:
- **Service Layer (BLL)**: 27 tests (52%)
- **Controller Layer (PL)**: 19 tests (37%)
- **Integration Tests (HTTP Routes)**: 6 tests (11%, skipped if server not running)

This distribution follows the lecturer's recommendation:
- Strong focus on Business Logic Layer (Service tests)
- Good coverage on Presentation Layer (Controller tests)
- Integration tests validate HTTP routing and end-to-end flows

**Test Results:**
- 46 tests run successfully (all pass)
- 6 integration tests skipped when server not running
- 0 failures, 0 errors
- Total coverage of all critical business logic

### Test Categories

#### UserServiceTest (7 tests)
- `registerTest`: Validates successful user registration with password hashing
- `registerWithExistingUsernameTest`: Ensures duplicate username prevention
- `registerWithEmptyUsernameTest`: Validates input validation
- `loginTest`: Verifies login flow and UUID token generation
- `loginWithWrongPasswordTest`: Tests wrong password rejection
- `validateTokenTest`: Tests token validation logic
- `validateTokenWithInvalidTokenTest`: Ensures invalid tokens are rejected

#### MediaServiceTest (11 tests)
- `createMediaTest`: Validates media creation with creator assignment
- `createMediaWithEmptyTitleTest`: Tests title validation
- `createMediaWithoutMediaTypeTest`: Tests media type validation
- `updateMediaTest`: Verifies ownership check on updates
- `updateMediaByDifferentUserTest`: Ensures only owner can update
- `updateNonexistentMediaTest`: Tests error handling for missing media
- `deleteMediaTest`: Validates deletion with ownership check
- `deleteMediaByDifferentUserTest`: Ensures only owner can delete
- `getAllMediaTest`: Tests retrieval of all media entries
- `getMediaByIdTest`: Tests retrieval by ID
- `getMediaByIdNotFoundTest`: Tests non-existent media handling

#### RatingServiceTest (5 tests)
- `createRatingWithInvalidStarsTest`: Validates star value constraints (< 1)
- `createRatingWithTooManyStarsTest`: Tests upper bound validation (> 5)
- `createRatingWithMinStarsTest`: Tests minimum valid stars (1)
- `createRatingWithMaxStarsTest`: Tests maximum valid stars (5)
- `createRatingWithNullCommentTest`: Tests null comment handling

#### FavoriteServiceTest (4 tests)
- `addFavoriteTest`: Tests adding media to favorites
- `addFavoriteAlreadyExistsTest`: Tests duplicate favorite prevention
- `toggleFavoriteAddTest`: Tests toggle to add
- `toggleFavoriteRemoveTest`: Tests toggle to remove

#### UserControllerTest (4 tests)
- `handleRegisterTest`: Validates HTTP POST for registration (201)
- `handleRegisterWithExistingUsernameTest`: Tests duplicate user (400)
- `handleLoginTest`: Tests HTTP POST for login (200)
- `handleLoginWithInvalidCredentialsTest`: Tests invalid login (401)

#### MediaControllerTest (6 tests)
- `handleGetAllMediaTest`: Tests GET /api/media (200)
- `handleCreateMediaWithoutTokenTest`: Tests unauthorized create (401)
- `handleCreateMediaWithValidTokenTest`: Tests authorized create (201)
- `handleDeleteMediaWithoutTokenTest`: Tests unauthorized delete (401)
- `handleUpdateMediaWithoutTokenTest`: Tests unauthorized update (401)
- `handleUnsupportedMethodTest`: Tests invalid method (405)

#### RatingControllerTest (5 tests)
- `handleGetRatingsForMediaTest`: Tests GET ratings (200)
- `handleCreateRatingWithoutTokenTest`: Tests unauthorized rating (401)
- `handleCreateRatingWithValidTokenTest`: Tests authorized rating (201)
- `handleInvalidMediaIdTest`: Tests invalid ID (400)
- `handleUnsupportedMethodTest`: Tests invalid method (405)

#### FavoriteControllerTest (4 tests)
- `handleGetFavoritesWithoutTokenTest`: Tests unauthorized access (401)
- `handleAddFavoriteWithoutTokenTest`: Tests unauthorized add (401)
- `handleToggleFavoriteWithValidTokenTest`: Tests toggle with token (200)
- `handleCheckFavoriteTest`: Tests check favorite (200)

#### RouteIntegrationTest (6 tests)
- `registerUserViaRouteTest`: Tests real HTTP POST /api/users/register
- `loginUserViaRouteTest`: Tests real HTTP POST /api/users/login
- `getMediaViaRouteTest`: Tests real HTTP GET /api/media
- `createMediaWithoutTokenViaRouteTest`: Tests unauthorized access (401)
- `getUserProfileViaRouteTest`: Tests real HTTP GET /api/users/{username}
- `loginWithWrongCredentialsViaRouteTest`: Tests wrong credentials (401)

**Note:** Integration tests are skipped when the server is not running, ensuring unit tests can run independently.

### Mockito Strategy
Mockito is used extensively to isolate unit tests:
- **Mock repositories** in service tests to avoid database dependency
- **Mock services** in controller tests to focus on HTTP handling
- Use `when().thenReturn()` for method stubbing
- Use `verify()` to ensure correct method calls
- Use `never()` to verify methods are not called in error cases

Example from MediaServiceTest:
```java
@Mock
private MediaRepository mediaRepository;

@Test
void createMediaTest() {
    when(mediaRepository.save(any(MediaEntry.class))).thenReturn(1);
    MediaEntry result = mediaService.createMedia(media, testUser);
    verify(mediaRepository).save(any(MediaEntry.class));
}
```

### Naming Convention
Tests follow consistent naming:
- Test classes: `{ClassName}Test.java`
- Test methods: `{functionality}Test()`
- Clear, descriptive names indicating what is tested

### Test Pattern
All tests follow Arrange-Act-Assert pattern:
```java
@Test
void registerWithEmptyUsernameTest() {
    // Arrange
    when(userRepository.findByUsername("")).thenReturn(Optional.empty());
    
    // Act & Assert
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.register("", "password")
    );
    
    assertEquals("Username cannot be empty", exception.getMessage());
}
```

### Test Coverage Focus
Tests prioritize:
- **Business logic validation**: Input validation, constraints
- **Error handling**: Exception cases, edge cases
- **Security checks**: Ownership validation, authentication
- **State changes**: Data persistence, updates

Tests do not cover:
- Database operations (would be integration tests)
- JSON serialization (Jackson responsibility)
- HTTP protocol details (HttpServer responsibility)

---

## SOLID Principles in Detail

The codebase demonstrates clear adherence to SOLID principles. Here are concrete examples from the project:

### 1. Single Responsibility Principle (SRP)

Each class has exactly one reason to change.

#### Example: UserController
**File:** `UserController.java`

**Responsibility:** Handle HTTP requests and responses only

```java
public class UserController {
    private final UserService userService;
    private final ObjectMapper objectMapper;
    
    public void handleRegister(HttpExchange exchange) throws IOException {
        // Only handles: parsing HTTP request, calling service, sending HTTP response
        String body = new String(exchange.getRequestBody().readAllBytes());
        RegisterRequest request = objectMapper.readValue(body, RegisterRequest.class);
        User user = userService.register(request.getUsername(), request.getPassword());
        sendResponse(exchange, 201, "User registered successfully");
    }
}
```

**No business logic here** - just HTTP handling.

#### Example: UserService
**File:** `UserService.java`

**Responsibility:** Implement business logic and validation

```java
public class UserService {
    private final UserRepository userRepository;
    
    public User register(String username, String password) {
        // Business logic: validation, password hashing
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword);
        userRepository.save(user);
        return user;
    }
}
```

**No HTTP handling, no SQL** - just business logic.

#### Example: UserRepository
**File:** `UserRepository.java`

**Responsibility:** Manage database operations only

```java
public class UserRepository {
    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash, token) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getToken());
            stmt.executeUpdate();
        }
    }
}
```

**No validation, no HTTP** - just database access.

**Result:** Each layer can change independently. Changing database from PostgreSQL to MySQL only affects repositories. Changing from HTTP to WebSockets only affects controllers.

### 2. Dependency Inversion Principle (DIP)

High-level modules depend on abstractions (injected dependencies), not concrete implementations.

#### Example: Service with Constructor Injection
**File:** `UserService.java`

**Before (Bad):**
```java
public class UserService {
    private UserRepository userRepository;
    
    public UserService() {
        this.userRepository = new UserRepository(); // Hard-coded dependency
    }
}
```

**After (Good):**
```java
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository; // Injected dependency
    }
}
```

**Benefits:**
- Easy to test: can inject mock repository
- Can swap implementations without changing UserService
- Follows Dependency Inversion Principle

#### Example: Testing with Mock
**File:** `UserServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository; // Mock injected
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository); // Inject mock
    }
    
    @Test
    void registerTest() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        userService.register("user", "pass");
        verify(userRepository).save(any(User.class)); // Verify interaction
    }
}
```

**Result:** Tests run without database. Services are decoupled from repositories.

### 3. Open/Closed Principle (OCP)

Software entities should be open for extension, closed for modification.

#### Example: MediaType Enum
**File:** `MediaType.java`

```java
public enum MediaType {
    MOVIE,
    SERIES,
    GAME
}
```

**Adding new media types:**
```java
public enum MediaType {
    MOVIE,
    SERIES,
    GAME,
    BOOK,      // New type added
    PODCAST    // Another new type
}
```

**No changes needed in:**
- MediaService (works with any MediaType)
- MediaRepository (stores type as string)
- MediaController (accepts any valid enum value)

**Result:** System can be extended without modifying existing code.

### 4. Other SOLID Principles

#### Liskov Substitution Principle (LSP)
Not heavily demonstrated due to lack of inheritance, but:
- Repository implementations could be swapped
- Different database implementations would work identically

#### Interface Segregation Principle (ISP)
- Service methods are focused and specific
- Controllers only expose necessary HTTP methods
- No bloated interfaces forcing implementation of unused methods

**Example:** RatingService doesn't force implementing media management methods.

### SOLID Benefits Realized

1. **Testability**: Easy to write unit tests with mocks
2. **Maintainability**: Changes are localized to specific layers
3. **Scalability**: New features can be added without breaking existing code
4. **Clarity**: Each class has a clear, single purpose

---

## Time Tracking

### Estimated vs. Actual Time

| Phase | Estimated | Actual | Notes |
|-------|-----------|--------|-------|
| Project Setup & Planning | 2h | 3h | Setup took longer (Docker, PostgreSQL, Maven) |
| Database Schema Design | 3h | 4h | Multiple iterations on foreign keys and constraints |
| Repository Layer | 4h | 5h | Transaction handling more complex than expected |
| Service Layer (Basic) | 3h | 4h | Password hashing and validation added time |
| Controller Layer (Basic) | 3h | 4h | HTTP routing more manual than expected |
| Authentication System | 2h | 3h | Token generation and validation logic |
| Unit Tests (Basic) | 4h | 5h | Mockito setup had learning curve |
| **Intermediate Submission** | **21h** | **28h** | |
| Rating System | 3h | 4h | Average rating calculation logic |
| Favorites System | 2h | 2h | Straightforward implementation |
| Profile & Statistics | 1h | 1.5h | SQL aggregation queries |
| Search & Filter | 2h | 2.5h | Query parameter parsing |
| Leaderboard | 1h | 1h | Simple SQL with GROUP BY |
| Recommendation System | 4h | 3h | Simplified genre-based approach |
| Additional Unit Tests | 3h | 3h | Brought total to 20 tests |
| Integration Testing | 2h | 2h | Curl scripts for all endpoints |
| Documentation (Protocol) | 2h | 2h | This document |
| **Final Submission** | **20h** | **21h** | |
| **TOTAL** | **41h** | **49h** | |

### Key Takeaways

**Overestimated:**
- Recommendation System (simplified approach was faster)

**Underestimated:**
- Project Setup (Docker configuration, environment setup)
- Repository Layer (transaction handling complexity)
- Unit Tests (Mockito learning curve)

**Most Time-Consuming:**
- Repository Layer: 5 hours
- Unit Tests: 8 hours total (5h + 3h)
- Service Layer: 4 hours

**Quickest Tasks:**
- Favorites System: 2 hours
- Leaderboard: 1 hour

### Development Phases

1. **Setup Phase** (3h): Environment, tools, dependencies
2. **Data Layer Phase** (9h): Database + Repositories
3. **Business Logic Phase** (8h): Services + validation
4. **API Layer Phase** (7h): Controllers + routing
5. **Feature Phase** (10h): Rating, Favorites, Search, Recommendations
6. **Testing Phase** (8h): Unit tests + Integration tests
7. **Documentation Phase** (4h): README, Protocol, Comments

**Total Development Time: 49 hours over 4 weeks**

---

## Submission Completeness

### Delivered Files
- **Source Code**: Complete Java project with Maven configuration
- **README.md**: User documentation with API reference and quick start guide
- **protocol.md**: This development report with architecture, testing, and reflections
- **docker-compose.yml**: PostgreSQL database configuration
- **MRP_Postman_Collection.json**: Complete Postman collection for all endpoints
- **Integration Test Scripts**:
  - `test_all_endpoints.sh`: Master test script
  - `test_api.sh`: Basic API tests
  - `test_search.sh`: Search & filter tests
  - `test_leaderboard.sh`: Leaderboard tests
  - `test_recommendations.sh`: Recommendation tests
  - `test_favorites.sh`: Favorites tests
  - `test_rating_system.sh`: Rating system tests

### GitHub Repository
- **Link**: https://github.com/vili-georgieva/mrp-projekt
- **Commit History**: Complete development history with meaningful commit messages
- **Branches**: Main branch with stable code

### Requirements Checklist
✓ Java implementation
✓ Pure HTTP server (no web frameworks)
✓ PostgreSQL database in Docker
✓ SQL injection prevention (PreparedStatements)
✓ No OR-Mapping library
✓ 52 unit tests (requirement: minimum 20)
✓ Token-based authentication
✓ All functional requirements implemented
✓ All business logic implemented
✓ Complete documentation
✓ Integration tests (curl scripts)
✓ SOLID principles demonstrated
✓ Time tracking included
✓ Lessons learned documented

### Presentation Readiness
- ✓ Working solution tested and ready
- ✓ Docker environment configured
- ✓ Postman collection prepared
- ✓ Architecture diagrams available
- ✓ Demo data can be created via test scripts

---

## Final Notes

This project successfully demonstrates a complete RESTful API implementation using pure Java HTTP libraries without web frameworks. All required features are implemented, tested, and documented.

The final application includes:
- **52 unit tests** covering business logic (46 active + 6 integration tests)
- **6 Controllers**: User, Media, Rating, Favorite, Leaderboard, Recommendation
- **6 Services**: Complete business logic layer
- **4 Repositories**: Database access with PreparedStatements (SQL injection protection)
- Integration tests via curl scripts
- Docker-based PostgreSQL database
- Token-based authentication (UUID format)
- Complete CRUD operations for users and media
- **Rating system** with confirmation, likes, and comment moderation
- **Favorites system** with toggle functionality
- **Search and filter** functionality (title, genre, type, age restriction, rating)
- **Leaderboard** (top users by rating count)
- **Genre-based recommendation system**
- **User statistics** (media count, rating count, average stars, favorites)

All code adheres to SOLID principles and follows clean architecture patterns with clear separation between Controller, Service, and Repository layers.

