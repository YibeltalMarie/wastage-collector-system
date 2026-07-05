# Phase 3 — Project setup: Ethiopia waste collector system

> This is where design becomes real structure.
> Every folder, every file, every naming decision here is intentional.
> Follow this once and it becomes muscle memory for every future system.

---

## What phase 3 produces

By the end of this phase you will have:

- A Git repository with a clean history
- A Spring Boot project with professional package structure
- A React project with professional folder structure
- A PostgreSQL database connected and verified
- Environment variables configured correctly
- A running application (even if it does nothing yet)
- Your first meaningful commit

Nothing fancy. No features. Just a rock-solid foundation.

---

## Step 1 — Create the Git repository

Do this before anything else. Always.

### On GitHub

1. Go to github.com → New repository
2. Name it: `waste-collector-ethiopia`
3. Set to Public (for your portfolio) or Private
4. Do NOT add README, .gitignore, or license yet (we'll do this manually)
5. Click Create repository

### On your machine

```bash
mkdir waste-collector-ethiopia
cd waste-collector-ethiopia
git init
git remote add origin https://github.com/YOUR_USERNAME/waste-collector-ethiopia.git
```

### Create the monorepo structure immediately

```bash
mkdir backend
mkdir frontend
mkdir docs
```

Your repo will hold both backend and frontend in one place.
This is called a monorepo. It is the right choice for a project this size.

```
waste-collector-ethiopia/
  backend/     ← Spring Boot project
  frontend/    ← React project
  docs/        ← Your Phase 1 and Phase 2 documents live here
  .gitignore   ← Root level gitignore
  README.md    ← Project overview
```

### Create the root .gitignore

```bash
touch .gitignore
```

Paste this into it:

```gitignore
# ─── Java / Maven ───────────────────────────────────
target/
*.class
*.jar
*.war
*.ear
*.log

# ─── Spring Boot ────────────────────────────────────
backend/.mvn/
!backend/.mvn/wrapper/
backend/mvnw
backend/mvnw.cmd

# ─── Node / React ───────────────────────────────────
node_modules/
frontend/dist/
frontend/.env
frontend/.env.local
frontend/.env.*.local

# ─── Environment files (NEVER commit these) ─────────
.env
.env.local
.env.production
*.env

# ─── IDE files ──────────────────────────────────────
.idea/
.vscode/settings.json
*.iml
*.iws
*.ipr
.DS_Store
Thumbs.db

# ─── Database ───────────────────────────────────────
*.sql.bak
dump.sql
```

### Create the README.md

```bash
touch README.md
```

Paste this into it:

```markdown
# Waste Collector Ethiopia

A digital platform connecting citizens, waste collectors, and administrators
to make waste collection in Ethiopian cities reliable and accountable.

## Tech stack

| Layer      | Technology              |
|------------|-------------------------|
| Frontend   | React 18 + Vite + Tailwind CSS |
| Backend    | Spring Boot 3 + Spring Security |
| Database   | PostgreSQL 15           |
| Auth       | JWT (access + refresh tokens) |
| Maps       | Leaflet.js              |
| Deployment | Docker + Railway        |

## Roles

- **Citizen** — submits and tracks waste pickup requests
- **Collector** — receives assignments and updates pickup status
- **Admin** — assigns collectors, monitors operations, manages users

## Running locally

See [docs/local-setup.md](docs/local-setup.md) for instructions.

## Project documents

- [Phase 1 — Problem understanding](docs/phase1-project-understanding.md)
- [Phase 2 — System design](docs/phase2-system-design-revised.md)
```

### Copy your docs into the docs folder

```bash
cp /path/to/phase1-project-understanding.md docs/
cp /path/to/phase2-system-design-revised.md docs/
cp /path/to/professional-engineer-workflow.md docs/
```

### First commit

```bash
git add .
git commit -m "chore: initial repo setup with structure and docs"
git push -u origin main
```

You now have a clean starting point on GitHub.

---

## Step 2 — Set up the Spring Boot backend

### Generate the project

Go to: https://start.spring.io

Fill in:

```
Project:         Maven
Language:        Java
Spring Boot:     3.2.x (latest stable)
Group:           com.wastecollector
Artifact:        api
Name:            api
Description:     Waste Collector Ethiopia API
Package name:    com.wastecollector.api
Packaging:       Jar
Java:            21
```

Add these dependencies:

```
Spring Web               ← REST API controllers
Spring Security          ← Authentication and authorization
Spring Data JPA          ← Database access / ORM
PostgreSQL Driver        ← Connect to PostgreSQL
Validation               ← @Valid, @NotBlank, @NotNull annotations
Spring Boot DevTools     ← Hot reload during development
Lombok                   ← Remove boilerplate (getters, setters, builders)
```

Click Generate → download the zip → extract into your `backend/` folder.

---

### The complete backend folder structure

This is the structure you set up NOW, even before writing any feature code.
Create every folder. Most files will be empty at first — that is fine.

```
backend/
  src/
    main/
      java/com/wastecollector/api/

        ├── config/
        │     ├── SecurityConfig.java        ← Spring Security rules
        │     ├── JwtConfig.java             ← JWT settings bean
        │     ├── CorsConfig.java            ← Allow React to call the API
        │     └── OpenApiConfig.java         ← Swagger documentation setup

        ├── controller/
        │     ├── AuthController.java        ← /api/auth/*
        │     ├── CitizenRequestController.java   ← /api/requests/*
        │     ├── CollectorController.java   ← /api/collector/*
        │     ├── AdminController.java       ← /api/admin/*
        │     └── NotificationController.java    ← /api/notifications/*

        ├── service/
        │     ├── AuthService.java
        │     ├── PickupRequestService.java
        │     ├── CollectorService.java
        │     ├── AdminService.java
        │     ├── NotificationService.java
        │     └── UserService.java

        ├── repository/
        │     ├── UserRepository.java
        │     ├── PickupRequestRepository.java
        │     ├── CollectorProfileRepository.java
        │     ├── NotificationRepository.java
        │     ├── RequestStatusHistoryRepository.java
        │     └── RefreshTokenRepository.java

        ├── model/
        │     ├── entity/
        │     │     ├── User.java
        │     │     ├── CollectorProfile.java
        │     │     ├── PickupRequest.java
        │     │     ├── Notification.java
        │     │     ├── RequestStatusHistory.java
        │     │     └── RefreshToken.java
        │     └── enums/
        │           ├── Role.java             ← CITIZEN, COLLECTOR, ADMIN
        │           ├── RequestStatus.java    ← PENDING, ASSIGNED, etc.
        │           ├── Availability.java     ← AVAILABLE, UNAVAILABLE, ON_DUTY
        │           └── NotificationType.java ← REQUEST_SUBMITTED, etc.

        ├── dto/
        │     ├── request/
        │     │     ├── RegisterRequest.java
        │     │     ├── LoginRequest.java
        │     │     ├── PickupRequestDto.java
        │     │     ├── AssignCollectorDto.java
        │     │     └── FailureReasonDto.java
        │     └── response/
        │           ├── AuthResponse.java
        │           ├── UserResponse.java
        │           ├── PickupRequestResponse.java
        │           ├── CollectorProfileResponse.java
        │           ├── NotificationResponse.java
        │           └── DashboardStatsResponse.java

        ├── security/
        │     ├── JwtTokenProvider.java      ← Generate and validate JWT tokens
        │     ├── JwtAuthFilter.java         ← Intercepts every request, reads token
        │     ├── UserDetailsServiceImpl.java ← Loads user from DB for Spring Security
        │     └── CustomAuthEntryPoint.java  ← Returns 401 when token is missing/bad

        ├── exception/
        │     ├── GlobalExceptionHandler.java   ← Catches all exceptions, returns clean JSON
        │     ├── BusinessException.java        ← Your custom business rule exception
        │     ├── ResourceNotFoundException.java ← 404 errors
        │     └── UnauthorizedActionException.java ← 403 errors

        ├── mapper/
        │     ├── UserMapper.java            ← Convert User entity ↔ UserResponse DTO
        │     ├── PickupRequestMapper.java
        │     └── NotificationMapper.java

        ├── scheduler/
        │     └── RequestScheduler.java      ← Cron jobs (nightly checks, etc.)

        └── WasteCollectorApiApplication.java  ← Main entry point

      resources/
        ├── application.yml                  ← Main config (uses env vars)
        ├── application-dev.yml              ← Dev overrides
        ├── application-prod.yml             ← Prod overrides
        └── db/
              └── migration/                 ← Flyway SQL migrations
                    ├── V1__create_users_table.sql
                    ├── V2__create_collector_profiles.sql
                    ├── V3__create_pickup_requests.sql
                    ├── V4__create_notifications.sql
                    ├── V5__create_request_status_history.sql
                    ├── V6__create_refresh_tokens.sql
                    └── V7__seed_admin_user.sql

    test/
      java/com/wastecollector/api/
        ├── service/
        │     ├── AuthServiceTest.java
        │     └── PickupRequestServiceTest.java
        └── controller/
              └── AuthControllerIntegrationTest.java
```

### Why each package exists

| Package | Single responsibility |
|---|---|
| `controller/` | Receives HTTP requests, calls service, returns response. Zero logic. |
| `service/` | All business rules live here. The brain of the application. |
| `repository/` | Database operations only. No logic. Spring Data JPA generates most of it. |
| `model/entity/` | Java classes that map to database tables. No logic. |
| `model/enums/` | Type-safe constants. Use these instead of raw strings everywhere. |
| `dto/request/` | Shapes of data the client sends in. Validated here. |
| `dto/response/` | Shapes of data the server sends back. Never expose entities directly. |
| `security/` | Everything related to JWT and authentication. Isolated here. |
| `exception/` | One place where all errors are caught and formatted. |
| `mapper/` | Converts between entities and DTOs. Keeps controllers and services clean. |
| `scheduler/` | Background jobs that run on a timer, not triggered by HTTP. |
| `config/` | Spring configuration classes. CORS, security rules, Swagger. |

---

### application.yml

```yaml
spring:
  application:
    name: waste-collector-api

  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/wastecollector}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate        # NEVER use 'create' or 'create-drop' in production
    show-sql: false             # Set to true only when debugging queries
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    open-in-view: false         # Important: prevents lazy loading issues

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

server:
  port: ${PORT:8080}
  error:
    include-message: never      # Never expose internal error messages in production
    include-stacktrace: never

jwt:
  secret: ${JWT_SECRET}
  access-token-expiry-ms: ${JWT_ACCESS_EXPIRY:3600000}       # 1 hour
  refresh-token-expiry-days: ${JWT_REFRESH_EXPIRY:30}         # 30 days

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    com.wastecollector: DEBUG   # Your code — verbose
    org.springframework: WARN   # Framework — quiet
    org.hibernate.SQL: WARN     # SQL — quiet unless debugging
```

### .env file (never commit this)

```bash
touch backend/.env
echo "backend/.env" >> .gitignore
```

Contents of `backend/.env`:

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/wastecollector
DB_USERNAME=postgres
DB_PASSWORD=your_local_password_here
JWT_SECRET=your-very-long-random-secret-key-at-least-64-characters-long-here
JWT_ACCESS_EXPIRY=3600000
JWT_REFRESH_EXPIRY=30
```

Create `.env.example` (this one you DO commit):

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/wastecollector
DB_USERNAME=postgres
DB_PASSWORD=
JWT_SECRET=
JWT_ACCESS_EXPIRY=3600000
JWT_REFRESH_EXPIRY=30
```

### First migration file — V1

`src/main/resources/db/migration/V1__create_users_table.sql`:

```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(100)             NOT NULL,
    phone_number  VARCHAR(20)              NOT NULL UNIQUE,
    email         VARCHAR(150)             UNIQUE,
    password_hash VARCHAR(255)             NOT NULL,
    role          VARCHAR(20)              NOT NULL,
    sub_city      VARCHAR(100),
    kebele        VARCHAR(50),
    address       TEXT,
    is_active     BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ              NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ              NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_phone  ON users(phone_number);
CREATE INDEX idx_users_role   ON users(role);
CREATE INDEX idx_users_active ON users(is_active);
```

---

### pom.xml — add these dependencies manually

Add to your `pom.xml` inside `<dependencies>`:

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Swagger / OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

<!-- Flyway (database migrations) -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

---

### Verify the backend starts

```bash
cd backend
./mvnw spring-boot:run
```

You should see:
```
Started WasteCollectorApiApplication in X seconds
Flyway: Migrating schema to version 1
```

If it starts without errors — you have a working foundation.

---

## Step 3 — Set up the React frontend

### Generate the project

```bash
cd ../frontend
npm create vite@latest . -- --template react
npm install
```

### Install all dependencies at once

```bash
npm install \
  react-router-dom \
  axios \
  @tanstack/react-query \
  react-hook-form \
  @hookform/resolvers \
  zod \
  leaflet \
  react-leaflet \
  lucide-react \
  clsx

npm install -D \
  tailwindcss \
  postcss \
  autoprefixer \
  @tailwindcss/forms

npx tailwindcss init -p
```

### Why each library

| Library | Why |
|---|---|
| `react-router-dom` | Client-side routing between pages |
| `axios` | HTTP client — cleaner than fetch, easy interceptors |
| `@tanstack/react-query` | API data fetching, caching, loading/error states |
| `react-hook-form` | Form state management — performant, clean |
| `zod` | Schema validation — validates forms and API responses |
| `@hookform/resolvers` | Connects react-hook-form with zod validation |
| `leaflet` + `react-leaflet` | Free open-source maps — no API key needed |
| `lucide-react` | Clean icon library |
| `clsx` | Conditionally join CSS class names cleanly |
| `tailwindcss` | Utility-first CSS — fast to write, consistent |

---

### The complete frontend folder structure

```
frontend/
  public/
    favicon.ico

  src/
    ├── main.jsx                  ← Entry point — renders <App />
    ├── App.jsx                   ← Root component — sets up routing
    │
    ├── api/
    │     ├── axiosInstance.js    ← Axios config — base URL, interceptors, token
    │     ├── authApi.js          ← All /api/auth/* calls
    │     ├── requestsApi.js      ← All /api/requests/* calls
    │     ├── collectorApi.js     ← All /api/collector/* calls
    │     ├── adminApi.js         ← All /api/admin/* calls
    │     └── notificationsApi.js ← All /api/notifications/* calls
    │
    ├── hooks/
    │     ├── useAuth.js          ← Read current user from context
    │     ├── useRequests.js      ← React Query hooks for pickup requests
    │     ├── useNotifications.js ← React Query hooks for notifications
    │     └── useCollector.js     ← React Query hooks for collector data
    │
    ├── context/
    │     └── AuthContext.jsx     ← Global auth state (user, token, login, logout)
    │
    ├── pages/
    │     ├── auth/
    │     │     ├── LoginPage.jsx
    │     │     └── RegisterPage.jsx
    │     │
    │     ├── citizen/
    │     │     ├── CitizenDashboard.jsx
    │     │     ├── SubmitRequestPage.jsx
    │     │     ├── RequestHistoryPage.jsx
    │     │     └── RequestDetailPage.jsx
    │     │
    │     ├── collector/
    │     │     ├── CollectorDashboard.jsx
    │     │     ├── AssignmentDetailPage.jsx
    │     │     └── CollectorHistoryPage.jsx
    │     │
    │     ├── admin/
    │     │     ├── AdminDashboard.jsx
    │     │     ├── RequestsListPage.jsx
    │     │     ├── RequestDetailPage.jsx
    │     │     ├── CollectorsListPage.jsx
    │     │     ├── CitizensListPage.jsx
    │     │     └── ReportsPage.jsx
    │     │
    │     └── shared/
    │           ├── NotFoundPage.jsx
    │           └── UnauthorizedPage.jsx
    │
    ├── components/
    │     ├── layout/
    │     │     ├── Navbar.jsx
    │     │     ├── Sidebar.jsx
    │     │     └── PageWrapper.jsx
    │     │
    │     ├── ui/
    │     │     ├── Button.jsx
    │     │     ├── Input.jsx
    │     │     ├── Badge.jsx          ← Status badges (PENDING, ASSIGNED, etc.)
    │     │     ├── Card.jsx
    │     │     ├── Modal.jsx
    │     │     ├── Spinner.jsx        ← Loading indicator
    │     │     ├── EmptyState.jsx     ← "No requests yet" placeholder
    │     │     └── ErrorMessage.jsx
    │     │
    │     ├── requests/
    │     │     ├── RequestCard.jsx
    │     │     ├── RequestStatusBadge.jsx
    │     │     └── RequestTimeline.jsx
    │     │
    │     ├── map/
    │     │     └── LocationPicker.jsx ← Leaflet map for picking location
    │     │
    │     └── notifications/
    │           └── NotificationBell.jsx
    │
    ├── routes/
    │     ├── ProtectedRoute.jsx    ← Redirects to login if not authenticated
    │     └── RoleGuard.jsx         ← Redirects if wrong role
    │
    ├── utils/
    │     ├── tokenStorage.js       ← Read/write JWT token safely
    │     ├── formatDate.js         ← Ethiopian date formatting helpers
    │     ├── statusHelpers.js      ← Map status strings to colors, labels
    │     └── cn.js                 ← clsx helper (className utility)
    │
    └── constants/
          ├── routes.js             ← Route path constants
          ├── queryKeys.js          ← React Query cache key constants
          └── statusConfig.js       ← Status colors, labels, icons config
```

---

### Key files explained and pre-written for you

#### `src/api/axiosInstance.js`

This is the most important file in your frontend.
Every API call goes through this. Configure it once, benefit everywhere.

```javascript
import axios from 'axios';
import { getToken, getRefreshToken, setToken, clearTokens } from '../utils/tokenStorage';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 seconds
});

// ─── Request interceptor ───────────────────────────────────────────
// Runs before EVERY request
// Automatically attaches the JWT token if it exists
axiosInstance.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Response interceptor ─────────────────────────────────────────
// Runs after EVERY response
// Handles 401 (token expired) → tries to refresh the token automatically
axiosInstance.interceptors.response.use(
  (response) => response,  // success — pass through
  async (error) => {
    const originalRequest = error.config;

    // If 401 and we haven't already tried to refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = getRefreshToken();
        const { data } = await axios.post(
          `${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh`,
          { refreshToken }
        );
        setToken(data.accessToken);
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return axiosInstance(originalRequest); // retry the original request
      } catch {
        clearTokens();
        window.location.href = '/login'; // force logout if refresh also fails
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
```

#### `src/utils/tokenStorage.js`

```javascript
// Store tokens in memory (most secure) and fall back to sessionStorage
// Never use localStorage for tokens in production — XSS vulnerable

let inMemoryToken = null;

export const setToken = (token) => {
  inMemoryToken = token;
};

export const getToken = () => inMemoryToken;

export const setRefreshToken = (token) => {
  // Refresh token lives in sessionStorage (clears when tab closes)
  // In production, use httpOnly cookies instead
  sessionStorage.setItem('refreshToken', token);
};

export const getRefreshToken = () => {
  return sessionStorage.getItem('refreshToken');
};

export const clearTokens = () => {
  inMemoryToken = null;
  sessionStorage.removeItem('refreshToken');
};
```

#### `src/context/AuthContext.jsx`

```jsx
import { createContext, useContext, useState, useCallback } from 'react';
import { setToken, setRefreshToken, clearTokens } from '../utils/tokenStorage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);     // { id, fullName, role, phoneNumber }
  const [isLoading, setIsLoading] = useState(false);

  const login = useCallback((userData, accessToken, refreshToken) => {
    setToken(accessToken);
    setRefreshToken(refreshToken);
    setUser(userData);
  }, []);

  const logout = useCallback(async () => {
    try {
      // Tell the server to revoke the refresh token
      await import('../api/authApi').then(m => m.logoutApi());
    } catch {
      // Even if API call fails, clear local tokens
    } finally {
      clearTokens();
      setUser(null);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// Custom hook — use this everywhere instead of useContext(AuthContext) directly
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
```

#### `src/routes/ProtectedRoute.jsx`

```jsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Spinner from '../components/ui/Spinner';

// Redirects to /login if not authenticated
export default function ProtectedRoute() {
  const { user, isLoading } = useAuth();

  if (isLoading) return <Spinner />;
  if (!user) return <Navigate to="/login" replace />;

  return <Outlet />;
}
```

#### `src/routes/RoleGuard.jsx`

```jsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Redirects to /unauthorized if user's role is not in the allowed list
export default function RoleGuard({ allowedRoles }) {
  const { user } = useAuth();

  if (!user || !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
```

#### `src/App.jsx` — full routing structure

```jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import RoleGuard from './routes/RoleGuard';

// Auth pages
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

// Citizen pages
import CitizenDashboard from './pages/citizen/CitizenDashboard';
import SubmitRequestPage from './pages/citizen/SubmitRequestPage';
import RequestHistoryPage from './pages/citizen/RequestHistoryPage';
import RequestDetailPage from './pages/citizen/RequestDetailPage';

// Collector pages
import CollectorDashboard from './pages/collector/CollectorDashboard';
import AssignmentDetailPage from './pages/collector/AssignmentDetailPage';
import CollectorHistoryPage from './pages/collector/CollectorHistoryPage';

// Admin pages
import AdminDashboard from './pages/admin/AdminDashboard';
import RequestsListPage from './pages/admin/RequestsListPage';
import CollectorsListPage from './pages/admin/CollectorsListPage';
import CitizensListPage from './pages/admin/CitizensListPage';

// Shared
import NotFoundPage from './pages/shared/NotFoundPage';
import UnauthorizedPage from './pages/shared/UnauthorizedPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,             // Retry failed requests once
      staleTime: 1000 * 60, // Data is fresh for 1 minute
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>

            {/* Public routes */}
            <Route path="/login"    element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/unauthorized" element={<UnauthorizedPage />} />

            {/* Protected routes — must be logged in */}
            <Route element={<ProtectedRoute />}>

              {/* Citizen only */}
              <Route element={<RoleGuard allowedRoles={['CITIZEN']} />}>
                <Route path="/dashboard"        element={<CitizenDashboard />} />
                <Route path="/requests/new"     element={<SubmitRequestPage />} />
                <Route path="/requests"         element={<RequestHistoryPage />} />
                <Route path="/requests/:id"     element={<RequestDetailPage />} />
              </Route>

              {/* Collector only */}
              <Route element={<RoleGuard allowedRoles={['COLLECTOR']} />}>
                <Route path="/collector"           element={<CollectorDashboard />} />
                <Route path="/collector/assignments/:id" element={<AssignmentDetailPage />} />
                <Route path="/collector/history"   element={<CollectorHistoryPage />} />
              </Route>

              {/* Admin only */}
              <Route element={<RoleGuard allowedRoles={['ADMIN']} />}>
                <Route path="/admin"              element={<AdminDashboard />} />
                <Route path="/admin/requests"     element={<RequestsListPage />} />
                <Route path="/admin/collectors"   element={<CollectorsListPage />} />
                <Route path="/admin/citizens"     element={<CitizensListPage />} />
              </Route>

            </Route>

            {/* Default redirect */}
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="*" element={<NotFoundPage />} />

          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
```

#### `src/constants/queryKeys.js`

```javascript
// Centralize all React Query cache keys
// This prevents typos and makes cache invalidation predictable

export const QUERY_KEYS = {
  // Citizen
  MY_REQUESTS:        ['my-requests'],
  MY_REQUEST_DETAIL:  (id) => ['my-requests', id],

  // Collector
  COLLECTOR_TASKS:    ['collector-tasks'],
  COLLECTOR_HISTORY:  ['collector-history'],

  // Admin
  ALL_REQUESTS:       ['admin-requests'],
  ALL_COLLECTORS:     ['admin-collectors'],
  ALL_CITIZENS:       ['admin-citizens'],
  DASHBOARD_STATS:    ['admin-dashboard'],

  // Shared
  NOTIFICATIONS:      ['notifications'],
  UNREAD_COUNT:       ['notifications', 'unread-count'],
};
```

#### `src/constants/statusConfig.js`

```javascript
// Map request status strings to UI properties
// Change a color here and it updates everywhere in the app

export const STATUS_CONFIG = {
  PENDING: {
    label:  'Pending',
    color:  'bg-yellow-100 text-yellow-800',
    dot:    'bg-yellow-500',
  },
  ASSIGNED: {
    label:  'Assigned',
    color:  'bg-blue-100 text-blue-800',
    dot:    'bg-blue-500',
  },
  IN_PROGRESS: {
    label:  'In Progress',
    color:  'bg-orange-100 text-orange-800',
    dot:    'bg-orange-500',
  },
  COMPLETED: {
    label:  'Completed',
    color:  'bg-green-100 text-green-800',
    dot:    'bg-green-500',
  },
  CANCELLED: {
    label:  'Cancelled',
    color:  'bg-gray-100 text-gray-500',
    dot:    'bg-gray-400',
  },
  FAILED: {
    label:  'Failed',
    color:  'bg-red-100 text-red-800',
    dot:    'bg-red-500',
  },
};
```

---

### `.env` file for the frontend

```bash
touch frontend/.env
```

```env
VITE_API_BASE_URL=http://localhost:8080
```

---

### Tailwind config

`frontend/tailwind.config.js`:

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Your brand colors — Ethiopian green theme
        brand: {
          50:  '#f0fdf4',
          100: '#dcfce7',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
          900: '#14532d',
        },
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
```

---

### Verify the frontend starts

```bash
cd frontend
npm run dev
```

You should see:
```
VITE v5.x  ready in Xms
→  Local:   http://localhost:5173/
```

---

## Step 4 — Set up the PostgreSQL database

### Create the database locally

```bash
psql -U postgres
```

Inside psql:
```sql
CREATE DATABASE wastecollector;
CREATE USER wastecollector_user WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE wastecollector TO wastecollector_user;
\q
```

Update your `backend/.env`:
```env
DATABASE_URL=jdbc:postgresql://localhost:5432/wastecollector
DB_USERNAME=wastecollector_user
DB_PASSWORD=your_password
```

When you run the Spring Boot app, Flyway automatically runs your migration
files in order (V1, V2, V3...) and creates all your tables.

---

## Step 5 — Git workflow from here forward

### Branch strategy (simple and professional)

```
main          ← Always deployable. Never commit directly here.
  └── develop ← Integration branch. Merge features here first.
        └── feature/auth-register     ← One branch per feature
        └── feature/submit-request
        └── feature/admin-assign
```

```bash
# Create and switch to develop branch
git checkout -b develop

# When starting a new feature
git checkout -b feature/auth-register

# After feature is done — merge back to develop
git checkout develop
git merge feature/auth-register
git push origin develop

# When develop is stable — merge to main (deploy)
git checkout main
git merge develop
git push origin main
```

### Commit message format (use every single time)

```
<type>(<scope>): <short description>

Types:
  feat      New feature
  fix       Bug fix
  chore     Setup, config, tooling
  refactor  Restructure without changing behavior
  test      Adding or fixing tests
  docs      Documentation
  style     Formatting only (no logic change)

Scope (optional): which part of the system
  auth, requests, collector, admin, db, ui, config

Examples:
  feat(auth): implement user registration endpoint
  feat(auth): add JWT token generation on login
  fix(requests): prevent duplicate pending requests per citizen
  chore(db): add V2 migration for collector_profiles table
  refactor(service): extract request validation to separate method
  test(auth): add unit tests for registration business rules
  docs: update README with local setup instructions
```

---

## Step 6 — Final verification checklist

Before writing your first feature, confirm all of these:

### Backend
- [ ] Spring Boot starts without errors
- [ ] Flyway runs V1 migration and creates `users` table
- [ ] `http://localhost:8080/swagger-ui.html` loads (Swagger UI)
- [ ] `.env` file exists and is in `.gitignore`
- [ ] All packages (controller, service, repository, etc.) created

### Frontend
- [ ] `npm run dev` starts without errors
- [ ] `http://localhost:5173` loads in browser
- [ ] Tailwind CSS is working (add a test class to App.jsx)
- [ ] `.env` file exists with `VITE_API_BASE_URL`
- [ ] All folders created (pages, components, api, hooks, etc.)

### Git
- [ ] All changes committed to `develop` branch
- [ ] `.gitignore` is preventing `.env` files from being tracked
- [ ] `git status` shows clean working tree
- [ ] GitHub shows the repository with all folders

### Commit when everything is green
```bash
git add .
git commit -m "chore: complete project setup — backend, frontend, db connected"
git push origin develop
```

---

## What you have built

At this point you have zero features but a professional foundation:

```
A Spring Boot app that:
  ✓ Starts and connects to PostgreSQL
  ✓ Runs database migrations automatically
  ✓ Has a clean package structure that will hold 50+ classes without chaos
  ✓ Has Swagger UI for API documentation
  ✓ Uses environment variables for all secrets

A React app that:
  ✓ Has routing set up for all three roles
  ✓ Has protected routes and role guards
  ✓ Has a global auth context
  ✓ Has an axios instance with automatic token attachment
  ✓ Has automatic token refresh on 401
  ✓ Has all folder structure for every feature

A Git setup that:
  ✓ Has a branch strategy
  ✓ Has a commit message convention
  ✓ Never exposes secrets
```

Every professional system you build after this
will follow the same structure. The names change. The pattern does not.

---

## What comes next — Phase 4, Feature 1

The first feature you build is always authentication:

```
Feature 1: Register and login (backend)
  → V1__create_users_table.sql         ← already done
  → User.java entity
  → UserRepository.java
  → AuthService.java (register + login logic)
  → JwtTokenProvider.java
  → AuthController.java
  → Test with Postman

Feature 2: Register and login (frontend)
  → authApi.js (API calls)
  → LoginPage.jsx
  → RegisterPage.jsx
  → AuthContext wired up
  → Test end to end
```

Authentication first. Everything else depends on it.

---

*Document version: 1.0*
*Created: May 2026*
*Project: Ethiopia Waste Collector System*
*Stack: React 18 + Vite + Spring Boot 3 + PostgreSQL 15*
*Previous: phase2-system-design-revised.md*
*Next: feature-01-authentication.md*