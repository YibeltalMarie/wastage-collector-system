# Phase 2 — System design (revised and complete)
# Ethiopia waste collector system

> This is the corrected version of the Phase 2 document.
> All gaps identified during design review have been addressed.
> This is the version you code from.

---

## What changed from the first version

| # | Gap found | Fix applied |
|---|-----------|-------------|
| 1 | Notifications only covered citizen | `notifications` table now serves all 3 roles |
| 2 | No audit trail for status changes (violated BR-10) | Added `request_status_history` table |
| 3 | No logout / token revocation | Added `refresh_tokens` table |
| 4 | Missing collector history API (CO-08) | Added `GET /api/collector/history` |
| 5 | Missing logout and token refresh APIs | Added `POST /api/auth/logout` and `/api/auth/refresh` |
| 6 | Missing admin register citizen API (A-11) | Added `POST /api/admin/citizens` |
| 7 | No availability filter on collector list | Added `?availability=` query param |

---

## Final table count: 7 tables

```
1. users                   (kept, unchanged)
2. collector_profiles      (kept, unchanged)
3. pickup_requests         (kept, unchanged)
4. notifications           (updated — now for all 3 roles, not just citizen)
5. request_status_history  (NEW — audit trail for BR-10)
6. refresh_tokens          (NEW — logout and session control)
7. app_settings            (NEW — optional admin config, v1 can skip if needed)
```

---

## Complete database schema

### Table 1: `users`

```sql
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(100)             NOT NULL,
    phone_number  VARCHAR(20)              NOT NULL UNIQUE,
    email         VARCHAR(150)             UNIQUE,
    password_hash VARCHAR(255)             NOT NULL,
    role          VARCHAR(20)              NOT NULL,
                  -- values: CITIZEN | COLLECTOR | ADMIN
    sub_city      VARCHAR(100),
    kebele        VARCHAR(50),
    address       TEXT,
    is_active     BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_phone  ON users(phone_number);
CREATE INDEX idx_users_role   ON users(role);
CREATE INDEX idx_users_active ON users(is_active);
```

No change from v1. One table for all three roles.
The `role` column controls what each user can see and do.
`is_active = false` means the account is deactivated — the user cannot log in.

---

### Table 2: `collector_profiles`

```sql
CREATE TABLE collector_profiles (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL UNIQUE REFERENCES users(id),
    availability      VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
                      -- values: AVAILABLE | UNAVAILABLE | ON_DUTY
    assigned_sub_city VARCHAR(100),
    vehicle_type      VARCHAR(50),
    notes             TEXT,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_collector_user         ON collector_profiles(user_id);
CREATE INDEX idx_collector_availability ON collector_profiles(availability);
CREATE INDEX idx_collector_sub_city     ON collector_profiles(assigned_sub_city);
```

No change from v1.
When admin assigns a request → collector availability becomes ON_DUTY.
When collector marks complete/failed → availability goes back to AVAILABLE.
Admin can manually set UNAVAILABLE (e.g. day off, sick leave).

---

### Table 3: `pickup_requests`

```sql
CREATE TABLE pickup_requests (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_id       UUID NOT NULL REFERENCES users(id),
    collector_id     UUID REFERENCES users(id),  -- NULL until assigned
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                     -- values: PENDING | ASSIGNED | IN_PROGRESS
                     --         COMPLETED | CANCELLED | FAILED
    sub_city         VARCHAR(100) NOT NULL,
    kebele           VARCHAR(50),
    address          TEXT NOT NULL,
    latitude         DECIMAL(10, 8),
    longitude        DECIMAL(11, 8),
    preferred_date   DATE NOT NULL,
    notes            TEXT,
    failure_reason   TEXT,
    assigned_at      TIMESTAMP WITH TIME ZONE,
    started_at       TIMESTAMP WITH TIME ZONE,
    completed_at     TIMESTAMP WITH TIME ZONE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_requests_citizen       ON pickup_requests(citizen_id);
CREATE INDEX idx_requests_collector     ON pickup_requests(collector_id);
CREATE INDEX idx_requests_status        ON pickup_requests(status);
CREATE INDEX idx_requests_sub_city      ON pickup_requests(sub_city);
CREATE INDEX idx_requests_preferred_date ON pickup_requests(preferred_date);
CREATE INDEX idx_requests_created_at    ON pickup_requests(created_at DESC);
```

No change from v1.
This is the central table. Everything in the system revolves around it.

Status state machine (these are the only valid transitions):
```
PENDING   → ASSIGNED      (admin assigns a collector)
PENDING   → CANCELLED     (citizen cancels)
ASSIGNED  → IN_PROGRESS   (collector starts the job)
ASSIGNED  → PENDING       (admin reassigns — collector_id set to NULL)
IN_PROGRESS → COMPLETED   (collector finishes)
IN_PROGRESS → FAILED      (collector cannot complete)
FAILED    → PENDING       (admin decides to retry)
```

Any other transition must be rejected by the service layer.

---

### Table 4: `notifications` (UPDATED)

```sql
CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id),
                -- This can be a CITIZEN, COLLECTOR, or ADMIN
    request_id  UUID REFERENCES pickup_requests(id),
    type        VARCHAR(50) NOT NULL,
                -- CITIZEN notifications:
                --   REQUEST_SUBMITTED, REQUEST_ASSIGNED,
                --   REQUEST_IN_PROGRESS, REQUEST_COMPLETED, REQUEST_FAILED,
                --   REQUEST_CANCELLED
                -- COLLECTOR notifications:
                --   NEW_ASSIGNMENT, ASSIGNMENT_REMOVED, REASSIGNED
                -- ADMIN notifications:
                --   NEW_REQUEST, REQUEST_FAILED, REQUEST_CANCELLED
    title       VARCHAR(150) NOT NULL,
    message     TEXT NOT NULL,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user    ON notifications(user_id);
CREATE INDEX idx_notifications_unread  ON notifications(user_id, is_read)
    WHERE is_read = FALSE;
CREATE INDEX idx_notifications_request ON notifications(request_id);
```

**What changed:** Added `title` column for display. Added notification types
for collector (NEW_ASSIGNMENT, ASSIGNMENT_REMOVED, REASSIGNED) and admin
(NEW_REQUEST, REQUEST_FAILED, REQUEST_CANCELLED).

**Who receives what notification and when:**

| Event | Citizen | Collector | Admin |
|-------|---------|-----------|-------|
| Citizen submits request | REQUEST_SUBMITTED | — | NEW_REQUEST |
| Admin assigns collector | REQUEST_ASSIGNED | NEW_ASSIGNMENT | — |
| Collector starts pickup | REQUEST_IN_PROGRESS | — | — |
| Collector completes | REQUEST_COMPLETED | — | — |
| Collector marks failed | REQUEST_FAILED | — | REQUEST_FAILED |
| Citizen cancels | REQUEST_CANCELLED | ASSIGNMENT_REMOVED | REQUEST_CANCELLED |
| Admin reassigns | REQUEST_ASSIGNED (new) | REASSIGNED (old) + NEW_ASSIGNMENT (new) | — |

---

### Table 5: `request_status_history` (NEW)

```sql
CREATE TABLE request_status_history (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id   UUID NOT NULL REFERENCES pickup_requests(id),
    changed_by   UUID NOT NULL REFERENCES users(id),
    old_status   VARCHAR(20),         -- NULL for the first entry (creation)
    new_status   VARCHAR(20) NOT NULL,
    note         TEXT,                -- Optional context (failure reason, etc.)
    changed_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_history_request ON request_status_history(request_id);
CREATE INDEX idx_history_changed ON request_status_history(changed_at DESC);
```

**Why this table exists:**
Business rule BR-10 says all status changes must be timestamped.
The `pickup_requests` table only stores the latest timestamps.
This table stores the full history — every change, who made it, when,
and what it changed from and to.

This is what powers admin story A-10:
*"view the full history of any specific pickup request to investigate complaints."*

Example of what this table contains for one request:
```
request_id | changed_by   | old_status  | new_status  | changed_at
-----------|--------------|-------------|-------------|----------------------------
uuid-req-1 | uuid-citizen | NULL        | PENDING     | 2026-06-01 08:00:00
uuid-req-1 | uuid-admin   | PENDING     | ASSIGNED    | 2026-06-01 09:15:00
uuid-req-1 | uuid-collect | ASSIGNED    | IN_PROGRESS | 2026-06-01 10:30:00
uuid-req-1 | uuid-collect | IN_PROGRESS | COMPLETED   | 2026-06-01 11:45:00
```

**Every time the service layer changes a request status, it must also
insert a row into this table. This is non-negotiable.**

---

### Table 6: `refresh_tokens` (NEW)

```sql
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_user    ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token   ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_expires ON refresh_tokens(expires_at);
```

**Why this table exists:**
JWT access tokens are short-lived (15–60 minutes).
Refresh tokens are long-lived (7–30 days) and allow the user to get a
new access token without logging in again.

More importantly: this table allows logout.
Without it, once you issue a JWT, there is no way to invalidate it before
it expires. With this table, logout means marking the refresh token as
`revoked = true`. The next time someone tries to use it, the server checks
this table and rejects it.

**Token flow:**
```
Login
  → Issue access token (JWT, 60 min, stateless)
  → Issue refresh token (stored in this table, 30 days)
  → Store token_hash (never the raw token)

Access token expires
  → Client sends refresh token to POST /api/auth/refresh
  → Server checks: exists? not revoked? not expired?
  → If valid → issue new access token

Logout
  → Client sends refresh token to POST /api/auth/logout
  → Server sets revoked = true in this table
  → Old access token expires naturally in ≤ 60 min
```

---

### Table 7: `app_settings` (NEW — optional for v1)

```sql
CREATE TABLE app_settings (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key         VARCHAR(100) NOT NULL UNIQUE,
    value       TEXT NOT NULL,
    description TEXT,
    updated_by  UUID REFERENCES users(id),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Seed data
INSERT INTO app_settings (key, value, description) VALUES
  ('max_pending_requests_per_citizen', '1',  'BR-01: max active requests per citizen'),
  ('request_expiry_hours',            '72', 'Hours before a PENDING request auto-expires'),
  ('working_hours_start',             '07', 'Earliest hour collectors are assigned'),
  ('working_hours_end',               '18', 'Latest hour collectors are assigned');
```

**Why this table exists:**
Instead of hardcoding business rules in your Java code (e.g. `if (count >= 1)`),
you store configurable values in the database. An admin can change them without
a code deployment.

This is optional for v1. You can hardcode the values first and add this table
later. It is listed here so you know it belongs in the design.

---

## Entity Relationship Diagram (complete)

```
users (id PK)
  │
  ├─────────────────────────────────────────────────────┐
  │ 1:1                                                 │ 1:many
  ▼                                                     ▼
collector_profiles                           pickup_requests
  (user_id FK → users.id)                     (citizen_id  FK → users.id)
                                              (collector_id FK → users.id, nullable)
                                               │
                              ┌────────────────┼──────────────────┐
                              │ 1:many         │ 1:many           │ 1:many
                              ▼                ▼                  ▼
                    notifications    request_status_history    (notifications
                    (request_id FK)  (request_id FK)           also links to
                    (user_id FK)     (changed_by FK → users)   users directly)

users (id PK)
  │ 1:many
  ▼
refresh_tokens
  (user_id FK → users.id)
```

---

## Complete API endpoint list (corrected)

### Auth endpoints (public)

```
POST  /api/auth/register   Register new citizen account
POST  /api/auth/login      Login, returns access + refresh tokens
POST  /api/auth/refresh    Exchange refresh token for new access token  ← NEW
POST  /api/auth/logout     Revoke refresh token (logout)                ← NEW
```

---

### Citizen endpoints (role: CITIZEN)

```
GET   /api/requests                   My pickup requests (filterable by status)
POST  /api/requests                   Submit new pickup request
GET   /api/requests/{id}              Get details of one request
PATCH /api/requests/{id}/cancel       Cancel a PENDING request

GET   /api/profile                    My profile
PUT   /api/profile                    Update my profile

GET   /api/notifications              My notifications
PATCH /api/notifications/{id}/read    Mark one notification as read
PATCH /api/notifications/read-all     Mark all my notifications as read  ← NEW
```

---

### Collector endpoints (role: COLLECTOR)

```
GET   /api/collector/requests              My assigned requests (active)
GET   /api/collector/requests/{id}         Details of one assigned request
PATCH /api/collector/requests/{id}/start   Mark as IN_PROGRESS
PATCH /api/collector/requests/{id}/complete Mark as COMPLETED
PATCH /api/collector/requests/{id}/fail    Mark as FAILED with reason

GET   /api/collector/history               My completed/failed history  ← NEW
PATCH /api/collector/availability          Update my availability status

GET   /api/collector/profile               My collector profile
PUT   /api/collector/profile               Update my profile

GET   /api/notifications                   My notifications             ← NEW
PATCH /api/notifications/{id}/read         Mark as read                 ← NEW
```

---

### Admin endpoints (role: ADMIN)

```
GET   /api/admin/requests                        All requests (filterable)
GET   /api/admin/requests/{id}                   Full details of any request
GET   /api/admin/requests/{id}/history           Status change history    ← NEW
POST  /api/admin/requests/{id}/assign            Assign to collector
POST  /api/admin/requests/{id}/reassign          Reassign to different collector
PATCH /api/admin/requests/{id}/close             Close a FAILED request without retry ← NEW

GET   /api/admin/collectors                      All collectors (filter by availability)
POST  /api/admin/collectors                      Register new collector
GET   /api/admin/collectors/{id}                 One collector's details
PATCH /api/admin/collectors/{id}/deactivate      Deactivate account
PATCH /api/admin/collectors/{id}/activate        Re-activate account      ← NEW

GET   /api/admin/citizens                        All citizens (searchable) ← NEW
POST  /api/admin/citizens                        Register citizen manually  ← NEW
GET   /api/admin/citizens/{id}                   One citizen's details      ← NEW

GET   /api/admin/dashboard                       Summary statistics
GET   /api/admin/reports/requests                Request volume report      ← NEW
GET   /api/admin/reports/collectors              Collector performance report ← NEW

GET   /api/notifications                         Admin notifications
PATCH /api/notifications/{id}/read              Mark as read
```

---

## Complete notification matrix

Every event in the system and who gets notified:

| Event triggered by | Notification sent to | Type | Message example |
|-------------------|---------------------|------|----------------|
| Citizen submits request | Citizen | REQUEST_SUBMITTED | "Your request has been received" |
| Citizen submits request | Admin | NEW_REQUEST | "New pickup request in Bole" |
| Admin assigns collector | Citizen | REQUEST_ASSIGNED | "A collector has been assigned" |
| Admin assigns collector | Collector | NEW_ASSIGNMENT | "New pickup assigned to you in Bole" |
| Collector starts pickup | Citizen | REQUEST_IN_PROGRESS | "Your collector is on the way" |
| Collector completes | Citizen | REQUEST_COMPLETED | "Your waste has been collected" |
| Collector marks failed | Citizen | REQUEST_FAILED | "Collection failed: Gate locked" |
| Collector marks failed | Admin | REQUEST_FAILED | "Request #101 failed — needs reassignment" |
| Citizen cancels | Citizen | REQUEST_CANCELLED | "Your request has been cancelled" |
| Citizen cancels | Collector | ASSIGNMENT_REMOVED | "Pickup in Bole was cancelled by citizen" |
| Citizen cancels | Admin | REQUEST_CANCELLED | "Request #101 cancelled by citizen" |
| Admin reassigns | Old collector | REASSIGNED | "Your Bole pickup has been reassigned" |
| Admin reassigns | New collector | NEW_ASSIGNMENT | "New pickup assigned to you in Bole" |
| Admin closes failed | Citizen | REQUEST_CANCELLED | "Your request has been closed by admin" |

---

## Complete security matrix

```
Endpoint                                    | Public | CITIZEN | COLLECTOR | ADMIN
--------------------------------------------|--------|---------|-----------|------
POST /api/auth/register                     |   ✓    |         |           |
POST /api/auth/login                        |   ✓    |         |           |
POST /api/auth/refresh                      |   ✓    |         |           |
POST /api/auth/logout                       |        |    ✓    |     ✓     |  ✓
GET  /api/requests                          |        |    ✓    |           |
POST /api/requests                          |        |    ✓    |           |
GET  /api/requests/{id}                     |        |    ✓    |           |
PATCH /api/requests/{id}/cancel             |        |    ✓    |           |
GET  /api/profile                           |        |    ✓    |     ✓     |
PUT  /api/profile                           |        |    ✓    |     ✓     |
GET  /api/notifications                     |        |    ✓    |     ✓     |  ✓
PATCH /api/notifications/{id}/read          |        |    ✓    |     ✓     |  ✓
PATCH /api/notifications/read-all           |        |    ✓    |     ✓     |  ✓
GET  /api/collector/requests                |        |         |     ✓     |
GET  /api/collector/requests/{id}           |        |         |     ✓     |
PATCH /api/collector/requests/{id}/start    |        |         |     ✓     |
PATCH /api/collector/requests/{id}/complete |        |         |     ✓     |
PATCH /api/collector/requests/{id}/fail     |        |         |     ✓     |
GET  /api/collector/history                 |        |         |     ✓     |
PATCH /api/collector/availability           |        |         |     ✓     |
GET  /api/collector/profile                 |        |         |     ✓     |
PUT  /api/collector/profile                 |        |         |     ✓     |
GET  /api/admin/requests                    |        |         |           |  ✓
GET  /api/admin/requests/{id}               |        |         |           |  ✓
GET  /api/admin/requests/{id}/history       |        |         |           |  ✓
POST /api/admin/requests/{id}/assign        |        |         |           |  ✓
POST /api/admin/requests/{id}/reassign      |        |         |           |  ✓
PATCH /api/admin/requests/{id}/close        |        |         |           |  ✓
GET  /api/admin/collectors                  |        |         |           |  ✓
POST /api/admin/collectors                  |        |         |           |  ✓
GET  /api/admin/collectors/{id}             |        |         |           |  ✓
PATCH /api/admin/collectors/{id}/deactivate |        |         |           |  ✓
PATCH /api/admin/collectors/{id}/activate   |        |         |           |  ✓
GET  /api/admin/citizens                    |        |         |           |  ✓
POST /api/admin/citizens                    |        |         |           |  ✓
GET  /api/admin/citizens/{id}               |        |         |           |  ✓
GET  /api/admin/dashboard                   |        |         |           |  ✓
GET  /api/admin/reports/requests            |        |         |           |  ✓
GET  /api/admin/reports/collectors          |        |         |           |  ✓
```

---

## Final checklist before Phase 3

You are ready to move to Phase 3 when you can answer YES to all of these:

### Schema
- [ ] I understand what all 7 tables store
- [ ] I understand the status state machine and all valid transitions
- [ ] I understand that every status change writes to request_status_history
- [ ] I understand how refresh_tokens enables logout
- [ ] I understand that notifications now go to all 3 roles

### API
- [ ] I can name all auth endpoints (4 total)
- [ ] I can name all citizen endpoints (8 total)
- [ ] I can name all collector endpoints (9 total)
- [ ] I can name all admin endpoints (15 total)
- [ ] I understand the security matrix (who can call what)

### Architecture
- [ ] I understand why the frontend never touches the database directly
- [ ] I understand the controller → service → repository flow
- [ ] I understand how JWT works (login → token → every request)
- [ ] I understand what the service layer is responsible for

If you can answer YES to all of the above, proceed to Phase 3:
setting up the Git repository, Spring Boot project, React project,
and making the first database connection.

---

*Document version: 2.0 (revised)*
*Created: May 2026*
*Project: Ethiopia Waste Collector System*
*Stack: React + Spring Boot + PostgreSQL*
*Previous: phase1-project-understanding.md*
*Next: phase3-project-setup.md*