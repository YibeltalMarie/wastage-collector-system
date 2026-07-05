# Phase 2 — System design: Ethiopia waste collector system

> This document is written before any code exists.
> Every decision here is made on paper — cheap to change, easy to reason about.
> Once you start coding, come back here when you are confused about how
> something fits together.

---

## What phase 2 is really about

Phase 1 answered: *what* are we building and *for whom*?
Phase 2 answers: *how* will it be built technically?

There are three things you must design before writing code:

1. **System architecture** — the big picture of all components and how they talk
2. **Database schema** — the structure of your data and how it relates
3. **API endpoints** — the contract between your frontend and backend

Think of these as three levels of zoom:
- Architecture = the whole city (districts, roads, buildings)
- Schema = the blueprint of one building (rooms, walls, doors)
- API endpoints = the doors and windows (what goes in and out)

---

## Part 1 — System architecture

### What is system architecture?

System architecture is the high-level map of your entire application.
It shows every major component (frontend, backend, database, external services)
and how they communicate with each other.

Before drawing anything, you must answer three questions:

1. Who talks to what?
2. How do they talk? (HTTP? WebSocket? Direct DB connection?)
3. Where does each component live? (same server? different service?)

### The architecture of your waste collector system

Here is the complete picture:

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│                                                                 │
│   ┌─────────────────┐          ┌──────────────────────┐         │
│   │  Citizen App    │          │   Admin Dashboard    │         │
│   │  (React)        │          │   (React)            │         │
│   │  - Request form │          │   - Manage requests  │         │
│   │  - Track status │          │   - Assign collectors│         │
│   │  - History      │          │   - View stats       │         │
│   └────────┬────────┘          └──────────┬───────────┘         │
│            │                              │                     │
│            └──────────────┬───────────────┘                     │
│                           │ HTTPS / REST API                    │
└───────────────────────────┼─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                       BACKEND LAYER                             │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                   Spring Boot API                       │   │
│   │                                                         │   │
│   │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │   │
│   │  │  Controller  │  │   Service    │  │  Repository  │  │   │
│   │  │  Layer       │→ │   Layer      │→ │  Layer       │  │   │
│   │  │  (HTTP in)   │  │  (Logic)     │  │  (DB access) │  │   │
│   │  └──────────────┘  └──────────────┘  └──────┬───────┘  │   │
│   │                                              │          │   │
│   │  ┌──────────────┐  ┌──────────────┐          │          │   │
│   │  │  Security    │  │  Scheduler   │          │          │   │
│   │  │  (JWT Auth)  │  │  (Cron jobs) │          │          │   │
│   │  └──────────────┘  └──────────────┘          │          │   │
│   └──────────────────────────────────────────────┼──────────┘   │
│                                                  │              │
└──────────────────────────────────────────────────┼──────────────┘
                                                   │
┌──────────────────────────────────────────────────▼──────────────┐
│                        DATA LAYER                               │
│                                                                 │
│   ┌──────────────────────────┐                                  │
│   │      PostgreSQL          │                                  │
│   │      Database            │                                  │
│   │  - users                 │                                  │
│   │  - pickup_requests       │                                  │
│   │  - notifications         │                                  │
│   └──────────────────────────┘                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Explaining each layer in detail

---

#### Layer 1 — Client (Frontend: React)

**What it is:**
The React application is what users see and interact with in their browser.
It is a Single Page Application (SPA), meaning the browser loads one HTML file
and React dynamically updates what is shown without full page reloads.

**What it does:**
- Renders the UI (forms, buttons, tables, maps)
- Manages local state (what the user typed, loading indicators, error messages)
- Makes HTTP requests to the backend API
- Stores the JWT token (in memory or localStorage) and sends it with every request
- Handles routing (which page to show based on the URL)

**Key point — the frontend has NO business logic.**
It does not decide whether a user is allowed to do something.
It does not validate business rules.
It only presents data and sends requests.
All real decisions happen in the backend.

**Why one React app, not two?**
You could build one app for citizens and one for admins, but for learning
purposes we build one React app with role-based routing:
- If the logged-in user is a CITIZEN → show citizen pages
- If the logged-in user is an ADMIN → show admin pages
- If the logged-in user is a COLLECTOR → show collector pages

**Technology decisions:**
```
React + Vite       → Fast development build tool, modern standard
React Router v6    → Client-side routing between pages
Tailwind CSS       → Utility-first styling, fast to write
React Query        → Handles API calls, caching, loading/error states
Axios              → HTTP client for making API requests
Leaflet.js         → Free open-source maps (no API key needed)
```

---

#### Layer 2 — Backend (Spring Boot API)

**What it is:**
The Spring Boot application is your server. It runs on a machine (or container),
listens for HTTP requests from the frontend, processes them, and returns responses.

The frontend never touches the database directly.
Everything goes through the backend. This is a fundamental rule of web security.

**The internal layers of Spring Boot (this is important):**

```
HTTP Request comes in
        ↓
┌───────────────┐
│  Controller   │  ← Receives the request. Extracts data from URL/body.
│               │    Calls the service. Returns the HTTP response.
│               │    Does NOT contain business logic.
└───────┬───────┘
        ↓
┌───────────────┐
│   Service     │  ← The brain. Contains all business logic and rules.
│               │    "Can this user do this?" lives here.
│               │    "What should happen when X occurs?" lives here.
│               │    Calls the repository to read/write data.
└───────┬───────┘
        ↓
┌───────────────┐
│  Repository   │  ← Talks to the database. Just reads and writes data.
│               │    No logic here — only database operations.
│               │    Spring Data JPA generates most of this automatically.
└───────┬───────┘
        ↓
┌───────────────┐
│  PostgreSQL   │  ← Stores the data permanently.
└───────────────┘
```

**Why this separation matters:**
Each layer has one job. This makes the code easier to test, easier to debug,
and easier to change. When a bug exists in business logic, you look in the
service. When a bug exists in how data is fetched, you look in the repository.
You always know where to look.

**The Security layer (Spring Security + JWT):**

JWT (JSON Web Token) is how your system knows who is making a request.

Here is how it works:
```
1. User logs in → sends email + password to POST /api/auth/login
2. Backend verifies credentials → generates a JWT token (a long string)
3. Backend returns the token to the frontend
4. Frontend stores the token
5. On every future request, frontend sends the token in the HTTP header:
      Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
6. Backend reads the token → knows who this user is and what their role is
7. Backend decides: is this user allowed to do what they are asking?
   - If yes → process the request
   - If no  → return 403 Forbidden
```

**The Scheduler layer (Cron jobs):**
Some things in your system need to happen automatically on a schedule,
not triggered by a user action. Examples:
- Every night at midnight: find all PENDING requests older than 48 hours
  and send a reminder notification to admins
- Every morning: reset collector availability status

Spring Boot has built-in scheduling via `@Scheduled` annotations.

**Technology decisions:**
```
Spring Boot 3          → Backend framework
Spring Security        → Authentication and authorization
Spring Data JPA        → Database access (ORM)
JWT (jjwt library)     → Token generation and validation
Springdoc OpenAPI      → Auto-generates Swagger API documentation
Lombok                 → Reduces boilerplate code (getters, setters, etc.)
PostgreSQL driver      → Connects Spring Boot to PostgreSQL
```

---

#### Layer 3 — Data (PostgreSQL Database)

**What it is:**
PostgreSQL is a relational database. It stores your data in tables (like
spreadsheets), and the tables are connected by relationships.

**Why PostgreSQL and not something else:**
```
vs MySQL:      PostgreSQL is fully open source, more standards-compliant,
               better for complex queries
vs MongoDB:    Your data is relational (a request belongs to a user, belongs
               to a collector). Relational databases are the right tool.
vs H2 (in-memory): H2 is only for testing. Data disappears when the app stops.
```

**The database is the single source of truth.**
Everything the application knows, it knows because it is in the database.
The frontend shows it. The backend reads and writes it. But it lives here.

---

### How the components communicate

**Frontend → Backend:**
- Protocol: HTTPS (HTTP in development)
- Format: JSON (JavaScript Object Notation)
- Authentication: JWT token in the Authorization header
- Pattern: REST API (each URL + HTTP method = one operation)

**Backend → Database:**
- Protocol: JDBC (Java Database Connectivity)
- ORM: JPA/Hibernate (maps Java objects to database tables automatically)
- The backend never sends raw SQL — it uses repository methods that JPA
  translates into SQL

**Example of one full round trip (citizen submits a request):**
```
1. Citizen clicks "Submit Request" in React
2. React collects form data: { location: "Bole", date: "2026-06-01" }
3. React sends: POST https://api.wastecollector.com/api/requests
                Header: Authorization: Bearer <jwt-token>
                Body: { "location": "Bole", "preferredDate": "2026-06-01" }
4. Spring Boot receives the request
5. JWT filter reads the token → identifies the user as citizen ID 42
6. PickupRequestController receives the request body
7. PickupRequestController calls PickupRequestService.submitRequest(user, dto)
8. PickupRequestService checks: does this citizen already have a PENDING request?
   - If yes → throw BusinessException("You already have a pending request")
   - If no  → continue
9. PickupRequestService creates a new PickupRequest entity, sets status = PENDING
10. PickupRequestService calls pickupRequestRepository.save(request)
11. Spring Data JPA runs: INSERT INTO pickup_requests (...) VALUES (...)
12. Database saves the record and returns the generated ID
13. Service returns the saved entity to the controller
14. Controller maps it to a DTO (removes sensitive fields) and returns:
    HTTP 201 Created
    Body: { "id": 101, "status": "PENDING", "location": "Bole", ... }
15. React receives the response
16. React updates the UI: shows "Request submitted successfully"
```

This is one feature. Every feature follows this same path.

---

## Part 2 — Database schema

### What is a database schema?

A schema is the structure of your database — what tables exist, what columns
each table has, and how tables relate to each other.

Designing this before coding is critical. The database structure affects
everything: your entity classes, your queries, your API responses, your
business rules. A poorly designed schema causes pain throughout the entire project.

### Core principles of schema design

**1. Every table has a primary key**
A primary key uniquely identifies each row. We use UUID (universally unique ID)
instead of auto-incrementing integers for these reasons:
- UUIDs are unique across systems (safe if you ever merge databases)
- They do not expose how many records you have (security)
- They work well in distributed systems

**2. Use timestamps for everything important**
Always record when a record was created and when it was last updated.
You will always want this information and will regret not having it.

**3. Never delete — soft delete instead**
Instead of deleting rows, mark them as deleted with a boolean flag or
a `deleted_at` timestamp. This preserves your audit trail.
(Exception: truly temporary or disposable data)

**4. Normalize but do not over-normalize**
Related data should reference each other via foreign keys, not be duplicated.
But do not split things so far that every query requires 10 joins.

**5. Index the columns you filter and sort by**
If you frequently run queries like `WHERE status = 'PENDING'` or
`ORDER BY created_at DESC`, those columns need indexes.
Without indexes, the database scans every row — slow at scale.

---

### The tables

#### Table 1: `users`

This is the central table. Every person in the system — citizen, collector,
and admin — is a user. Their role determines what they can do.

```sql
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(100)        NOT NULL,
    phone_number  VARCHAR(20)         NOT NULL UNIQUE,
    email         VARCHAR(150)        UNIQUE,
    password_hash VARCHAR(255)        NOT NULL,
    role          VARCHAR(20)         NOT NULL,  -- CITIZEN | COLLECTOR | ADMIN
    sub_city      VARCHAR(100),                  -- Addis Ababa sub-city (Bole, Yeka, etc.)
    kebele        VARCHAR(50),                   -- Local area identifier
    address       TEXT,                          -- Free-text address description
    is_active     BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Indexes
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_role  ON users(role);
```

**Column explanations:**

| Column | Why it exists |
|--------|--------------|
| `id` | Unique identifier — never expose this in URLs if possible |
| `full_name` | Display name for the user |
| `phone_number` | Primary login identifier in Ethiopian context (more common than email) |
| `email` | Optional — some users may not have email |
| `password_hash` | NEVER store plain text passwords. Store a bcrypt hash only. |
| `role` | Determines permissions: CITIZEN, COLLECTOR, or ADMIN |
| `sub_city` | Ethiopian administrative division — useful for routing collectors |
| `kebele` | Smaller local area within a sub-city |
| `address` | Human-readable location description |
| `is_active` | Soft delete / deactivation flag. False = cannot log in. |
| `created_at` | When the account was created |
| `updated_at` | When the account was last modified |

**Design decision — one table for all roles:**
You could have separate tables for citizens, collectors, and admins.
But since they share most fields and the role column distinguishes them,
one table is simpler and sufficient for v1.

---

#### Table 2: `collector_profiles`

Collectors have additional information that citizens and admins do not have.
We store this in a separate table linked to the user.

```sql
CREATE TABLE collector_profiles (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL UNIQUE REFERENCES users(id),
    availability      VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
                      -- AVAILABLE | UNAVAILABLE | ON_DUTY
    assigned_sub_city VARCHAR(100),  -- Primary area this collector covers
    vehicle_type      VARCHAR(50),   -- e.g. tricycle, truck
    notes             TEXT,          -- Admin notes about this collector
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_collector_profile_user ON collector_profiles(user_id);
CREATE INDEX idx_collector_availability ON collector_profiles(availability);
```

**Why a separate table?**
The `users` table should stay clean and generic. Collector-specific fields
(availability, vehicle, assigned area) do not apply to citizens or admins.
Separating them keeps each table focused on one thing.

---

#### Table 3: `pickup_requests`

This is the core table of the entire system. Everything revolves around it.

```sql
CREATE TABLE pickup_requests (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_id        UUID NOT NULL REFERENCES users(id),
    collector_id      UUID REFERENCES users(id),  -- NULL until assigned
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                      -- PENDING | ASSIGNED | IN_PROGRESS | COMPLETED | CANCELLED | FAILED
    sub_city          VARCHAR(100) NOT NULL,
    kebele            VARCHAR(50),
    address           TEXT NOT NULL,
    latitude          DECIMAL(10, 8),   -- GPS coordinates (optional)
    longitude         DECIMAL(11, 8),
    preferred_date    DATE NOT NULL,
    notes             TEXT,             -- Citizen notes: "large item", "gate is locked"
    failure_reason    TEXT,             -- Filled in if status = FAILED
    assigned_at       TIMESTAMP WITH TIME ZONE,   -- When admin assigned a collector
    started_at        TIMESTAMP WITH TIME ZONE,   -- When collector started
    completed_at      TIMESTAMP WITH TIME ZONE,   -- When collector marked done
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Indexes
CREATE INDEX idx_requests_citizen   ON pickup_requests(citizen_id);
CREATE INDEX idx_requests_collector ON pickup_requests(collector_id);
CREATE INDEX idx_requests_status    ON pickup_requests(status);
CREATE INDEX idx_requests_sub_city  ON pickup_requests(sub_city);
CREATE INDEX idx_requests_date      ON pickup_requests(preferred_date);
```

**Column explanations:**

| Column | Why it exists |
|--------|--------------|
| `citizen_id` | Foreign key — which citizen submitted this request |
| `collector_id` | Foreign key — which collector is assigned (NULL if not yet assigned) |
| `status` | The current state of the request — the most important column |
| `sub_city` | Where the pickup is — used for matching collectors to areas |
| `address` | Human-readable location description |
| `latitude / longitude` | Optional GPS coordinates for map display |
| `preferred_date` | When the citizen wants the pickup to happen |
| `notes` | Anything the citizen wants the collector to know |
| `failure_reason` | If the collector could not complete it, why? |
| `assigned_at` | Timestamp of when admin assigned it — for performance tracking |
| `started_at` | Timestamp of when collector started — for tracking |
| `completed_at` | Timestamp of completion — the most important audit record |

**The status lifecycle — this is a state machine:**
```
                    ┌─── CANCELLED  (citizen cancelled while PENDING)
                    │
PENDING ──→ ASSIGNED ──→ IN_PROGRESS ──→ COMPLETED
                              │
                              └─── FAILED  (collector could not complete)
                                      │
                                      └──→ PENDING  (admin reassigns)
```

Every status transition must be validated in the service layer.
You cannot jump from PENDING directly to COMPLETED.
You cannot cancel a request that is already IN_PROGRESS.

---

#### Table 4: `notifications`

```sql
CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id),
    request_id  UUID REFERENCES pickup_requests(id),
    type        VARCHAR(50) NOT NULL,
                -- REQUEST_SUBMITTED | REQUEST_ASSIGNED | REQUEST_COMPLETED
                -- REQUEST_FAILED | REQUEST_CANCELLED
    message     TEXT NOT NULL,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user   ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, is_read)
    WHERE is_read = FALSE;
```

**Design note:**
This is a simple in-app notification store. When status changes happen,
your service layer creates a notification record for the affected user.
The frontend polls this table (or uses WebSocket in a future version)
to show unread notifications.

---

### Entity Relationship Diagram (ERD)

This shows how the tables connect:

```
users
  ├── id (PK)
  ├── phone_number (UNIQUE)
  ├── role
  └── ...
    │
    │  1:1 (one user can be one collector)
    ▼
collector_profiles
  ├── id (PK)
  ├── user_id (FK → users.id)  ← one collector profile per user
  └── availability

users (as citizen)
  │  1:many (one citizen has many requests)
  ▼
pickup_requests
  ├── id (PK)
  ├── citizen_id  (FK → users.id)
  ├── collector_id (FK → users.id, nullable)
  ├── status
  └── ...
    │
    │  1:many (one request has many notifications)
    ▼
notifications
  ├── id (PK)
  ├── user_id     (FK → users.id)
  └── request_id  (FK → pickup_requests.id)
```

**Relationships explained:**

| Relationship | Type | Meaning |
|---|---|---|
| user → collector_profile | One-to-One | One user has at most one collector profile |
| user → pickup_requests (as citizen) | One-to-Many | One citizen can submit many requests |
| user → pickup_requests (as collector) | One-to-Many | One collector can be assigned many requests |
| pickup_request → notifications | One-to-Many | One request can generate many notifications |

---

### Java entity classes (what the schema becomes in Spring Boot)

Understanding how a database table maps to Java code is essential.

```java
// The users table becomes this Java class
@Entity
@Table(name = "users")
@Data                    // Lombok: generates getters, setters, toString
@NoArgsConstructor       // Lombok: generates no-args constructor
@AllArgsConstructor      // Lombok: generates all-args constructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(length = 150, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;  // Enum: CITIZEN, COLLECTOR, ADMIN

    @Column(name = "sub_city", length = 100)
    private String subCity;

    @Column(length = 50)
    private String kebele;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
```

```java
// The Role enum
public enum Role {
    CITIZEN,
    COLLECTOR,
    ADMIN
}

// The RequestStatus enum
public enum RequestStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    FAILED
}
```

---

## Part 3 — API endpoints

### What is a REST API?

REST (Representational State Transfer) is a set of conventions for how
your frontend and backend communicate over HTTP.

**The four core HTTP methods and what they mean:**

| Method | Meaning | Example |
|--------|---------|---------|
| `GET` | Read data — fetch something | Get my list of requests |
| `POST` | Create data — send something new | Submit a new pickup request |
| `PUT` | Replace data — update everything | Replace my full profile |
| `PATCH` | Update data — change part of something | Change only the request status |
| `DELETE` | Remove data | Delete something |

**HTTP status codes you will use constantly:**

| Code | Meaning | When to use |
|------|---------|-------------|
| `200 OK` | Success | Successful GET, PUT, PATCH |
| `201 Created` | New resource created | Successful POST |
| `204 No Content` | Success, nothing to return | Successful DELETE |
| `400 Bad Request` | Client sent invalid data | Validation failed |
| `401 Unauthorized` | Not logged in | No token or bad token |
| `403 Forbidden` | Logged in but not allowed | Wrong role |
| `404 Not Found` | Resource does not exist | Request ID does not exist |
| `409 Conflict` | State conflict | Citizen already has pending request |
| `500 Internal Server Error` | Something broke in the server | Unexpected exception |

**URL naming conventions:**

```
Use nouns, not verbs in URLs:
  GOOD: POST /api/requests          (create a request)
  BAD:  POST /api/submitRequest     (verb in URL)

Use plural nouns:
  GOOD: GET /api/requests
  BAD:  GET /api/request

Use path parameters for specific resources:
  GET /api/requests/{id}            (get one specific request)

Use query parameters for filtering:
  GET /api/requests?status=PENDING&subCity=Bole

Nest resources when they belong to a parent:
  GET /api/collectors/{id}/requests (all requests for a specific collector)
```

---

### Complete API endpoint list

#### Authentication endpoints (public — no token required)

```
POST   /api/auth/register
POST   /api/auth/login
```

---

**POST /api/auth/register**

```
Description:  Register a new citizen account
Access:       Public (no token needed)

Request body:
{
  "fullName":     "Abebe Girma",
  "phoneNumber":  "0911234567",
  "password":     "SecurePass123",
  "subCity":      "Bole",
  "kebele":       "03",
  "address":      "Near Edna Mall, house with green gate"
}

Success response: 201 Created
{
  "id":          "uuid-here",
  "fullName":    "Abebe Girma",
  "phoneNumber": "0911234567",
  "role":        "CITIZEN",
  "subCity":     "Bole",
  "createdAt":   "2026-05-10T10:00:00Z"
}

Error responses:
  400 Bad Request  → missing required fields, password too short
  409 Conflict     → phone number already registered
```

---

**POST /api/auth/login**

```
Description:  Log in and receive a JWT token
Access:       Public (no token needed)

Request body:
{
  "phoneNumber": "0911234567",
  "password":    "SecurePass123"
}

Success response: 200 OK
{
  "token":     "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId":    "uuid-here",
  "role":      "CITIZEN",
  "fullName":  "Abebe Girma"
}

Error responses:
  401 Unauthorized → wrong phone number or password
  403 Forbidden    → account is deactivated (isActive = false)
```

---

#### Citizen endpoints (requires CITIZEN role)

```
GET    /api/requests              → Get my pickup requests
POST   /api/requests              → Submit a new pickup request
GET    /api/requests/{id}         → Get details of one request
PATCH  /api/requests/{id}/cancel  → Cancel a pending request
GET    /api/profile               → Get my profile
PUT    /api/profile               → Update my profile
GET    /api/notifications         → Get my notifications
PATCH  /api/notifications/{id}/read → Mark notification as read
```

---

**GET /api/requests**

```
Description:  Get all pickup requests submitted by the logged-in citizen
Access:       CITIZEN role required

Query parameters (optional):
  status=PENDING          → filter by status
  page=0&size=10          → pagination

Success response: 200 OK
{
  "content": [
    {
      "id":            "uuid",
      "status":        "ASSIGNED",
      "subCity":       "Bole",
      "address":       "Near Edna Mall",
      "preferredDate": "2026-06-01",
      "notes":         "Large item",
      "collectorName": "Tadesse W.",  ← included only if assigned
      "createdAt":     "2026-05-10T10:00:00Z"
    }
  ],
  "totalElements": 5,
  "totalPages":    1,
  "currentPage":   0
}
```

---

**POST /api/requests**

```
Description:  Submit a new waste pickup request
Access:       CITIZEN role required

Business rules enforced:
  - Citizen must not already have a PENDING or ASSIGNED request (BR-01)
  - preferredDate must not be in the past

Request body:
{
  "subCity":       "Bole",
  "kebele":        "03",
  "address":       "Near Edna Mall, house with green gate",
  "latitude":      9.0105,     ← optional
  "longitude":     38.7635,    ← optional
  "preferredDate": "2026-06-01",
  "notes":         "I have a large broken sofa as well"
}

Success response: 201 Created
{
  "id":            "uuid",
  "status":        "PENDING",
  "subCity":       "Bole",
  "address":       "Near Edna Mall",
  "preferredDate": "2026-06-01",
  "createdAt":     "2026-05-10T10:00:00Z"
}

Error responses:
  400 Bad Request  → validation failed (missing fields, past date)
  409 Conflict     → citizen already has an active request
```

---

**PATCH /api/requests/{id}/cancel**

```
Description:  Cancel a pending pickup request
Access:       CITIZEN role required (must be the owner of the request)

Business rules enforced:
  - Request must belong to the logged-in citizen
  - Request must be in PENDING status (cannot cancel ASSIGNED or later)

Success response: 200 OK
{
  "id":     "uuid",
  "status": "CANCELLED"
}

Error responses:
  403 Forbidden → request belongs to a different citizen
  404 Not Found → request ID does not exist
  409 Conflict  → request is not in PENDING status
```

---

#### Collector endpoints (requires COLLECTOR role)

```
GET    /api/collector/requests              → Get my assigned requests
GET    /api/collector/requests/{id}         → Get details of one assigned request
PATCH  /api/collector/requests/{id}/start   → Mark request as IN_PROGRESS
PATCH  /api/collector/requests/{id}/complete → Mark request as COMPLETED
PATCH  /api/collector/requests/{id}/fail    → Mark request as FAILED
PATCH  /api/collector/availability          → Update my availability status
GET    /api/collector/profile               → Get my collector profile
```

---

**PATCH /api/collector/requests/{id}/complete**

```
Description:  Mark an assigned request as completed
Access:       COLLECTOR role required (must be the assigned collector)

Business rules enforced:
  - Request must be assigned to the logged-in collector
  - Request must be in IN_PROGRESS status

Request body: (none required)

Success response: 200 OK
{
  "id":          "uuid",
  "status":      "COMPLETED",
  "completedAt": "2026-05-10T14:30:00Z"
}

Side effects (handled in service layer):
  - Creates a COMPLETED notification for the citizen
  - Updates admin dashboard statistics
```

---

**PATCH /api/collector/requests/{id}/fail**

```
Description:  Mark a request as failed with a reason
Access:       COLLECTOR role required (must be the assigned collector)

Request body:
{
  "reason": "GATE_LOCKED"
  ← options: LOCATION_NOT_FOUND | GATE_LOCKED | CITIZEN_NOT_PRESENT | OTHER
}

Success response: 200 OK
{
  "id":            "uuid",
  "status":        "FAILED",
  "failureReason": "GATE_LOCKED"
}
```

---

#### Admin endpoints (requires ADMIN role)

```
GET    /api/admin/requests                        → Get all requests (filterable)
GET    /api/admin/requests/{id}                   → Get full details of any request
POST   /api/admin/requests/{id}/assign            → Assign request to collector
POST   /api/admin/requests/{id}/reassign          → Reassign to different collector
GET    /api/admin/collectors                      → Get all collectors
POST   /api/admin/collectors                      → Register a new collector
PATCH  /api/admin/collectors/{id}/deactivate      → Deactivate a collector account
GET    /api/admin/dashboard                       → Get dashboard statistics
GET    /api/admin/citizens                        → Get all citizens (with search)
```

---

**GET /api/admin/requests**

```
Description:  Get all pickup requests with filtering options
Access:       ADMIN role required

Query parameters:
  status=PENDING          → filter by status
  subCity=Bole            → filter by area
  collectorId=uuid        → filter by assigned collector
  date=2026-06-01         → filter by preferred date
  page=0&size=20          → pagination

Success response: 200 OK
{
  "content": [
    {
      "id":            "uuid",
      "citizenName":   "Abebe Girma",
      "citizenPhone":  "0911234567",
      "collectorName": "Tadesse W.",
      "status":        "PENDING",
      "subCity":       "Bole",
      "address":       "Near Edna Mall",
      "preferredDate": "2026-06-01",
      "createdAt":     "2026-05-10T10:00:00Z"
    }
  ],
  "totalElements": 48,
  "totalPages":    3,
  "currentPage":   0
}
```

---

**POST /api/admin/requests/{id}/assign**

```
Description:  Assign a PENDING request to a collector
Access:       ADMIN role required

Business rules enforced:
  - Request must be in PENDING status
  - Collector must exist and be AVAILABLE
  - Collector must have role = COLLECTOR

Request body:
{
  "collectorId": "uuid-of-collector"
}

Success response: 200 OK
{
  "id":            "uuid",
  "status":        "ASSIGNED",
  "collectorName": "Tadesse Worku",
  "assignedAt":    "2026-05-10T10:05:00Z"
}

Side effects:
  - Creates an ASSIGNED notification for the citizen
  - Updates collector status to ON_DUTY

Error responses:
  400 Bad Request → request not in PENDING status
  404 Not Found   → request or collector ID does not exist
  409 Conflict    → collector is not available
```

---

**GET /api/admin/dashboard**

```
Description:  Get summary statistics for the admin dashboard
Access:       ADMIN role required

Success response: 200 OK
{
  "totalRequestsToday":      12,
  "pendingRequests":          5,
  "assignedRequests":         4,
  "inProgressRequests":       2,
  "completedToday":           8,
  "failedToday":              1,
  "totalCollectors":         15,
  "availableCollectors":      7,
  "onDutyCollectors":         6,
  "unavailableCollectors":    2
}
```

---

### API security matrix

This table shows exactly which roles can access which endpoints.
This becomes your `@PreAuthorize` annotations in Spring Boot.

```
Endpoint                                 | Public | CITIZEN | COLLECTOR | ADMIN
-----------------------------------------|--------|---------|-----------|------
POST /api/auth/register                  |   ✓    |         |           |
POST /api/auth/login                     |   ✓    |         |           |
GET  /api/requests                       |        |    ✓    |           |
POST /api/requests                       |        |    ✓    |           |
GET  /api/requests/{id}                  |        |    ✓    |           |
PATCH /api/requests/{id}/cancel          |        |    ✓    |           |
GET  /api/profile                        |        |    ✓    |    ✓      |
PUT  /api/profile                        |        |    ✓    |    ✓      |
GET  /api/notifications                  |        |    ✓    |    ✓      |  ✓
GET  /api/collector/requests             |        |         |    ✓      |
PATCH /api/collector/requests/{id}/start |        |         |    ✓      |
PATCH /api/collector/requests/{id}/complete|      |         |    ✓      |
PATCH /api/collector/requests/{id}/fail  |        |         |    ✓      |
PATCH /api/collector/availability        |        |         |    ✓      |
GET  /api/admin/requests                 |        |         |           |  ✓
POST /api/admin/requests/{id}/assign     |        |         |           |  ✓
GET  /api/admin/collectors               |        |         |           |  ✓
POST /api/admin/collectors               |        |         |           |  ✓
GET  /api/admin/dashboard                |        |         |           |  ✓
```

---

## Part 4 — DTOs (Data Transfer Objects)

### What is a DTO and why do you need one?

A DTO is the shape of the data you send and receive through the API.
It is different from your entity (the database class) for important reasons:

- You do not want to expose `password_hash` in an API response
- You do not want to accept `id` or `createdAt` from the client (server sets these)
- The API format may be different from the database format
- DTOs give you control over exactly what goes in and out

**Rule: Never return entity objects directly from a controller.
Always use DTOs.**

```java
// BAD — exposes password hash, internal fields
@GetMapping("/profile")
public User getProfile() {
    return userRepository.findById(id).get();  // DO NOT do this
}

// GOOD — returns only what the client needs
@GetMapping("/profile")
public UserResponseDto getProfile() {
    User user = userRepository.findById(id).get();
    return userMapper.toResponseDto(user);     // clean, controlled
}
```

**Example DTOs for your system:**

```java
// What the client sends when submitting a request
public record PickupRequestDto(
    @NotBlank String subCity,
    String kebele,
    @NotBlank String address,
    BigDecimal latitude,
    BigDecimal longitude,
    @NotNull @Future LocalDate preferredDate,
    String notes
) {}

// What the server sends back after creating a request
public record PickupRequestResponseDto(
    UUID id,
    String status,
    String subCity,
    String address,
    LocalDate preferredDate,
    String collectorName,  // null if not yet assigned
    OffsetDateTime createdAt
) {}

// What the server sends back for a user profile
public record UserResponseDto(
    UUID id,
    String fullName,
    String phoneNumber,
    String role,
    String subCity,
    String kebele,
    String address,
    OffsetDateTime createdAt
    // NOTE: password_hash is NOT here
) {}
```

---

## Summary — what phase 2 produced

By completing this document you now have:

| Output | What it becomes in code |
|--------|------------------------|
| System architecture diagram | Your project structure and technology choices |
| Layer descriptions | Your Spring Boot package structure (controller/service/repository) |
| JWT flow | Your Spring Security configuration |
| Database tables | Your `@Entity` classes and Flyway/Liquibase migrations |
| Column types and indexes | Your `@Column` annotations and `schema.sql` |
| Status lifecycle | Your `RequestStatus` enum and service layer validations |
| API endpoints | Your `@RestController` classes and `@RequestMapping` annotations |
| Request/response shapes | Your DTO record classes |
| Security matrix | Your `@PreAuthorize` annotations on each endpoint |
| Business rules | Your service layer `if` statements and exception throws |

Every box in this document maps to real code.
That is the purpose of phase 2 — to make your code inevitable rather than improvised.

---

## What comes next — Phase 3

With this document complete, you are ready to:

1. Set up your Spring Boot project (Spring Initializr)
2. Set up your React project (Vite)
3. Create the Git repository
4. Set up the database and write your first migration
5. Start building — beginning with authentication

---

*Document created: May 2026*
*Project: Ethiopia Waste Collector System*
*Stack: React + Spring Boot + PostgreSQL*
*Previous: phase1-project-understanding.md*
*Next: phase3-project-setup.md*
