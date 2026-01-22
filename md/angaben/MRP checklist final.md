# Checklist Media Ratings Platform (MRP) Final

**Student Name:** David
**Personal Identifier:** if25b113

---

### Must Haves

* [ ] Uses Java
* [ ] Implements a server listening to incoming clients
* [ ] Builds without errors and runs successfully
* [ ] Uses pure-HTTP library only (no web-framework like ASP, Spring, JSP,.. allowed)
* [ ] Uses a Postgres Database for storing data
* [ ] Prevents SQL injection
* [ ] Does not use an OR-Mapping Library
* [ ] Implements at least 20 meaningful unit tests

---

### Features

| REST Server | Points | Max. Points |
| --- | --- | --- |
| Server, listening to incoming clients |  | essential |
| Endpoints use HTTP path, params, headers and content correctly |  | essential |
| Routing functionality regarding HTTP path |  | essential |
| REST API Endpoints (register, login, ...) defined according to specs |  | essential |

| Functional Requirements | Points | Max. Points |
| --- | --- | --- |
| Model classes (User, MediaEntry, Rating, etc.) |  | essential |
| Register and login users, user state management |  | essential |
| Create, update, delete media entries (by owner) |  | 1 |
| Rate media (1-5 stars), add/edit/delete comment, like ratings |  | 2 |
| Mark and unmark media as favorites |  | 2 |
| View profile and statistics |  | 1 |
| View rating history and favorites list |  | 2 |

| Business Logic | Points | Max. Points |
| --- | --- | --- |
| One rating per user per media (editable) |  | 1 |
| Comments require confirmation before public visibility |  | 2 |
| Average rating calculation per media |  | 3 |
| Recommendation system based on rating history & similarity |  | 4 |
| Search and filter media (title, genre, type, age restriction, rating) |  | 3 |
| Ownership logic (only creator can modify media) |  | 2 |
| Leaderboard |  | 2 |

---

### Non-functional Requirements

| Requirement | Points | Max. Points |
| --- | --- | --- |
| Token-based security |  | essential |
| Data is persisted using PostgreSQL and Docker |  | 7 |
| Quality of unit-tests (usefulness, no duplicates, ...) |  | 4 |
| Code clearly reflects at least 2 SOLID principles |  | 2 |
| Integration Tests (Postman Collection or Curl script) |  | 2 |

---

### Protocol

| Requirement | Points | Max. Points |
| --- | --- | --- |
| Describes app design (decisions, structure, class diagrams) |  | 0.5 |
| Describes lessons learned |  | 1 |
| Explains unit testing strategy and coverage |  | 1 |
| Explains at least 2 SOLID principles with real examples |  | 1 |
| Contains tracked time for major tasks |  | 0.5 |
| Contains link to GIT |  | essential |

---

**Sum Points:** 44