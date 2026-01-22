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
The project includes 20 unit tests distributed across layers:
- **Controller Layer**: 4 tests (20%)
- **Service Layer**: 16 tests (80%)
- **Repository Layer**: 0 tests (by design)

This distribution follows best practices:
- Repository tests would require real database (integration tests)
- Service layer contains most business logic (highest value to test)
- Controller tests validate HTTP handling basics

### Test Categories

#### UserServiceTest (6 tests)
- `registerTest`: Validates successful user registration with password hashing
- `registerWithExistingUsernameTest`: Ensures duplicate username prevention
- `registerWithEmptyUsernameTest`: Validates input validation
- `loginTest`: Verifies login flow and UUID token generation
- `validateTokenTest`: Tests token validation logic
- `validateTokenWithInvalidTokenTest`: Ensures invalid tokens are rejected

#### MediaServiceTest (8 tests)
- `createMediaTest`: Validates media creation with creator assignment
- `createMediaWithEmptyTitleTest`: Tests title validation
- `updateMediaTest`: Verifies ownership check on updates
- `updateMediaByDifferentUserTest`: Ensures only owner can update
- `updateNonexistentMediaTest`: Tests error handling for missing media
- `deleteMediaTest`: Validates deletion with ownership check
- `deleteMediaByDifferentUserTest`: Ensures only owner can delete
- `getAllMediaTest`: Tests retrieval of all media entries

#### RatingServiceTest (2 tests)
- `createRatingWithInvalidStarsTest`: Validates star value constraints (1-5)
- `createRatingWithTooManyStarsTest`: Tests upper bound validation

#### UserControllerTest (4 tests)
- `testHandleRegister`: Validates HTTP POST for registration
- `testHandleLogin`: Tests HTTP POST for login
- `testHandleGetUser`: Verifies authenticated GET request
- `testHandleGetUserUnauthorized`: Ensures unauthorized access is blocked

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

## Final Notes

This project successfully demonstrates a complete RESTful API implementation using pure Java HTTP libraries without web frameworks. All required features are implemented, tested, and documented.

The final application includes:
- 20 unit tests covering business logic
- Integration tests via curl scripts
- Docker-based PostgreSQL database
- Token-based authentication
- Complete CRUD operations for users and media
- Rating system with confirmation and likes
- Favorites system
- Search and filter functionality
- Leaderboard
- Genre-based recommendation system

All code adheres to SOLID principles and follows clean architecture patterns.

