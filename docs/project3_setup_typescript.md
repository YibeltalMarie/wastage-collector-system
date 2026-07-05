# Phase 3 — Project setup: Ethiopia waste collector system
# (TypeScript edition)

> This is where design becomes real structure.
> Every folder, every file, every naming decision here is intentional.
> Follow this once and it becomes muscle memory for every future system.

---

## What phase 3 produces

By the end of this phase you will have:

- A Git repository with a clean history
- A Spring Boot project with professional package structure
- A React + TypeScript project with professional folder structure
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
4. Do NOT add README, .gitignore, or license yet (we do this manually)
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

```
waste-collector-ethiopia/
  backend/     ← Spring Boot project
  frontend/    ← React + TypeScript project
  docs/        ← All your phase documents live here
  .gitignore   ← Root level gitignore
  README.md    ← Project overview
```

### Create the root .gitignore

```gitignore
# ─── Java / Maven ───────────────────────────────────
target/
*.class
*.jar
*.war
*.ear
*.log

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
.DS_Store
Thumbs.db

# ─── TypeScript build output ────────────────────────
*.tsbuildinfo

# ─── Database ───────────────────────────────────────
*.sql.bak
dump.sql
```

### Create the README.md

```markdown
# Waste Collector Ethiopia

A digital platform connecting citizens, waste collectors, and administrators
to make waste collection in Ethiopian cities reliable and accountable.

## Tech stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Frontend   | React 18 + TypeScript + Vite + Tailwind CSS |
| Backend    | Spring Boot 3 + Spring Security     |
| Database   | PostgreSQL 15                       |
| Auth       | JWT (access + refresh tokens)       |
| Maps       | Leaflet.js                          |
| Deployment | Docker + Railway                    |

## Roles
- **Citizen** — submits and tracks waste pickup requests
- **Collector** — receives assignments and updates pickup status
- **Admin** — assigns collectors, monitors operations, manages users

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

---

## Step 2 — Set up the Spring Boot backend

### Generate the project

Go to: https://start.spring.io

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

Create every folder now, before writing any feature code.
Most files will be empty at first — that is fine.

```
backend/
  src/
    main/
      java/com/wastecollector/api/

        ├── config/
        │     ├── SecurityConfig.java         ← Spring Security rules
        │     ├── JwtConfig.java              ← JWT settings bean
        │     ├── CorsConfig.java             ← Allow React to call the API
        │     └── OpenApiConfig.java          ← Swagger documentation setup

        ├── controller/
        │     ├── AuthController.java         ← /api/auth/*
        │     ├── CitizenRequestController.java    ← /api/requests/*
        │     ├── CollectorController.java    ← /api/collector/*
        │     ├── AdminController.java        ← /api/admin/*
        │     └── NotificationController.java ← /api/notifications/*

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
        │           ├── Role.java              ← CITIZEN, COLLECTOR, ADMIN
        │           ├── RequestStatus.java     ← PENDING, ASSIGNED, etc.
        │           ├── Availability.java      ← AVAILABLE, UNAVAILABLE, ON_DUTY
        │           └── NotificationType.java  ← REQUEST_SUBMITTED, etc.

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
        │     ├── JwtTokenProvider.java       ← Generate and validate JWT tokens
        │     ├── JwtAuthFilter.java          ← Intercepts every request, reads token
        │     ├── UserDetailsServiceImpl.java ← Loads user from DB for Spring Security
        │     └── CustomAuthEntryPoint.java   ← Returns 401 when token is missing/bad

        ├── exception/
        │     ├── GlobalExceptionHandler.java    ← Catches all exceptions, clean JSON
        │     ├── BusinessException.java         ← Custom business rule exception
        │     ├── ResourceNotFoundException.java ← 404 errors
        │     └── UnauthorizedActionException.java ← 403 errors

        ├── mapper/
        │     ├── UserMapper.java             ← User entity ↔ UserResponse DTO
        │     ├── PickupRequestMapper.java
        │     └── NotificationMapper.java

        ├── scheduler/
        │     └── RequestScheduler.java       ← Cron jobs (nightly checks, etc.)

        └── WasteCollectorApiApplication.java ← Main entry point

      resources/
        ├── application.yml                   ← Main config (uses env vars)
        ├── application-dev.yml               ← Dev overrides
        ├── application-prod.yml              ← Prod overrides
        └── db/
              └── migration/
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
| `repository/` | Database operations only. No logic. JPA generates most of it. |
| `model/entity/` | Java classes that map to database tables. No logic. |
| `model/enums/` | Type-safe constants. Never use raw strings for status/role. |
| `dto/request/` | Shapes of data the client sends in. Validated here with @Valid. |
| `dto/response/` | Shapes of data the server sends back. Never expose entities directly. |
| `security/` | Everything JWT and auth. Completely isolated here. |
| `exception/` | One place where all errors are caught and formatted as JSON. |
| `mapper/` | Converts between entities and DTOs. Keeps controllers clean. |
| `scheduler/` | Background jobs on a timer, not triggered by HTTP. |
| `config/` | CORS, security rules, Swagger. Spring configuration classes. |

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
    open-in-view: false         # Prevents lazy loading issues

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

server:
  port: ${PORT:8080}
  error:
    include-message: never      # Never expose internal error messages
    include-stacktrace: never

jwt:
  secret: ${JWT_SECRET}
  access-token-expiry-ms: ${JWT_ACCESS_EXPIRY:3600000}     # 1 hour
  refresh-token-expiry-days: ${JWT_REFRESH_EXPIRY:30}       # 30 days

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    com.wastecollector: DEBUG
    org.springframework: WARN
    org.hibernate.SQL: WARN
```

### backend/.env (never commit)

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/wastecollector
DB_USERNAME=postgres
DB_PASSWORD=your_local_password_here
JWT_SECRET=your-very-long-random-secret-key-at-least-64-characters-long
JWT_ACCESS_EXPIRY=3600000
JWT_REFRESH_EXPIRY=30
```

### backend/.env.example (DO commit this)

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/wastecollector
DB_USERNAME=postgres
DB_PASSWORD=
JWT_SECRET=
JWT_ACCESS_EXPIRY=3600000
JWT_REFRESH_EXPIRY=30
```

### pom.xml — extra dependencies to add

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

<!-- Flyway -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### First migration — V1__create_users_table.sql

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

### Verify the backend starts

```bash
cd backend
./mvnw spring-boot:run
```

Expected output:
```
Started WasteCollectorApiApplication in X seconds
Flyway: Migrating schema to version 1
```

---

## Step 3 — Set up the React + TypeScript frontend

### Generate the project with TypeScript

```bash
cd ../frontend
npm create vite@latest . -- --template react-ts
npm install
```

The `react-ts` template gives you:
- TypeScript configured out of the box
- `tsconfig.json` already set up correctly
- `.tsx` for components, `.ts` for everything else

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
  @tailwindcss/forms \
  @types/leaflet \
  @types/node

npx tailwindcss init -p
```

Note: `@types/leaflet` and `@types/node` are TypeScript type definitions
for libraries that do not ship their own types. Always check if a library
needs a `@types/` package when using TypeScript.

### Why each library

| Library | Why |
|---|---|
| `react-router-dom` | Client-side routing between pages |
| `axios` | HTTP client — easy interceptors for token attachment |
| `@tanstack/react-query` | API data fetching, caching, loading/error states |
| `react-hook-form` | Performant form state management |
| `zod` | Runtime schema validation — works perfectly with TypeScript |
| `@hookform/resolvers` | Bridges react-hook-form and zod |
| `leaflet` + `react-leaflet` | Free open-source maps, no API key needed |
| `lucide-react` | Clean icon library with TypeScript support |
| `clsx` | Conditional class name utility |
| `tailwindcss` | Utility-first CSS |

---

### The complete frontend folder structure (TypeScript)

```
frontend/
  public/
    favicon.ico

  src/
    ├── main.tsx                    ← Entry point
    ├── App.tsx                     ← Root component — routing
    ├── vite-env.d.ts               ← Vite type declarations (auto-generated)
    │
    ├── types/                      ← ALL TypeScript interfaces/types live here
    │     ├── auth.types.ts         ← User, Role, auth state
    │     ├── request.types.ts      ← PickupRequest, RequestStatus
    │     ├── collector.types.ts    ← CollectorProfile, Availability
    │     ├── notification.types.ts ← Notification, NotificationType
    │     ├── admin.types.ts        ← DashboardStats, admin-specific types
    │     └── api.types.ts          ← Generic API response wrappers
    │
    ├── api/
    │     ├── axiosInstance.ts      ← Axios config, interceptors, token handling
    │     ├── authApi.ts            ← /api/auth/* calls
    │     ├── requestsApi.ts        ← /api/requests/* calls
    │     ├── collectorApi.ts       ← /api/collector/* calls
    │     ├── adminApi.ts           ← /api/admin/* calls
    │     └── notificationsApi.ts   ← /api/notifications/* calls
    │
    ├── hooks/
    │     ├── useAuth.ts            ← Read current user from AuthContext
    │     ├── useRequests.ts        ← React Query hooks for pickup requests
    │     ├── useNotifications.ts   ← React Query hooks for notifications
    │     └── useCollector.ts       ← React Query hooks for collector data
    │
    ├── context/
    │     └── AuthContext.tsx       ← Global auth state (user, login, logout)
    │
    ├── pages/
    │     ├── auth/
    │     │     ├── LoginPage.tsx
    │     │     └── RegisterPage.tsx
    │     ├── citizen/
    │     │     ├── CitizenDashboard.tsx
    │     │     ├── SubmitRequestPage.tsx
    │     │     ├── RequestHistoryPage.tsx
    │     │     └── RequestDetailPage.tsx
    │     ├── collector/
    │     │     ├── CollectorDashboard.tsx
    │     │     ├── AssignmentDetailPage.tsx
    │     │     └── CollectorHistoryPage.tsx
    │     ├── admin/
    │     │     ├── AdminDashboard.tsx
    │     │     ├── RequestsListPage.tsx
    │     │     ├── RequestDetailPage.tsx
    │     │     ├── CollectorsListPage.tsx
    │     │     ├── CitizensListPage.tsx
    │     │     └── ReportsPage.tsx
    │     └── shared/
    │           ├── NotFoundPage.tsx
    │           └── UnauthorizedPage.tsx
    │
    ├── components/
    │     ├── layout/
    │     │     ├── Navbar.tsx
    │     │     ├── Sidebar.tsx
    │     │     └── PageWrapper.tsx
    │     ├── ui/
    │     │     ├── Button.tsx
    │     │     ├── Input.tsx
    │     │     ├── Badge.tsx
    │     │     ├── Card.tsx
    │     │     ├── Modal.tsx
    │     │     ├── Spinner.tsx
    │     │     ├── EmptyState.tsx
    │     │     └── ErrorMessage.tsx
    │     ├── requests/
    │     │     ├── RequestCard.tsx
    │     │     ├── RequestStatusBadge.tsx
    │     │     └── RequestTimeline.tsx
    │     ├── map/
    │     │     └── LocationPicker.tsx
    │     └── notifications/
    │           └── NotificationBell.tsx
    │
    ├── routes/
    │     ├── ProtectedRoute.tsx    ← Redirects to /login if not authenticated
    │     └── RoleGuard.tsx         ← Redirects if wrong role
    │
    ├── utils/
    │     ├── tokenStorage.ts       ← Read/write JWT token safely
    │     ├── formatDate.ts         ← Date formatting helpers
    │     ├── statusHelpers.ts      ← Status → color/label mapping
    │     └── cn.ts                 ← clsx className utility
    │
    └── constants/
          ├── routes.ts             ← Route path constants
          ├── queryKeys.ts          ← React Query cache key constants
          └── statusConfig.ts       ← Status colors, labels, icon config
```

---

### Key files — fully written in TypeScript

---

#### `src/types/auth.types.ts`

```typescript
export type Role = 'CITIZEN' | 'COLLECTOR' | 'ADMIN';

export interface AuthUser {
  id:          string;
  fullName:    string;
  phoneNumber: string;
  role:        Role;
}

export interface AuthResponse {
  accessToken:  string;
  refreshToken: string;
  userId:       string;
  role:         Role;
  fullName:     string;
  phoneNumber:  string;
}

export interface AuthContextType {
  user:     AuthUser | null;
  isLoading: boolean;
  login:    (userData: AuthUser, accessToken: string, refreshToken: string) => void;
  logout:   () => Promise<void>;
}
```

---

#### `src/types/request.types.ts`

```typescript
export type RequestStatus =
  | 'PENDING'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'FAILED';

export interface PickupRequestResponse {
  id:            string;
  status:        RequestStatus;
  subCity:       string;
  kebele:        string | null;
  address:       string;
  latitude:      number | null;
  longitude:     number | null;
  preferredDate: string;
  notes:         string | null;
  collectorName: string | null;
  failureReason: string | null;
  assignedAt:    string | null;
  startedAt:     string | null;
  completedAt:   string | null;
  createdAt:     string;
}

export interface SubmitRequestPayload {
  subCity:       string;
  kebele?:       string;
  address:       string;
  latitude?:     number;
  longitude?:    number;
  preferredDate: string;
  notes?:        string;
}

export interface StatusHistoryEntry {
  oldStatus:  RequestStatus | null;
  newStatus:  RequestStatus;
  changedBy:  string;
  note:       string | null;
  changedAt:  string;
}
```

---

#### `src/types/api.types.ts`

```typescript
// Generic paginated response wrapper
// Your Spring Boot Page<T> responses map to this shape
export interface PagedResponse<T> {
  content:        T[];
  totalElements:  number;
  totalPages:     number;
  currentPage:    number;
  size:           number;
}

// Generic API error response shape
export interface ApiError {
  status:    number;
  message:   string;
  timestamp: string;
  path:      string;
}
```

---

#### `src/types/admin.types.ts`

```typescript
export interface DashboardStats {
  totalRequestsToday:    number;
  pendingRequests:       number;
  assignedRequests:      number;
  inProgressRequests:    number;
  completedToday:        number;
  failedToday:           number;
  totalCollectors:       number;
  availableCollectors:   number;
  onDutyCollectors:      number;
  unavailableCollectors: number;
}
```

---

#### `src/api/axiosInstance.ts`

```typescript
import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { getToken, getRefreshToken, setToken, clearTokens } from '../utils/tokenStorage';

const axiosInstance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10_000,
});

// ─── Request interceptor ──────────────────────────────────────────
// Automatically attaches the JWT access token to every request
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Response interceptor ─────────────────────────────────────────
// On 401 (token expired): silently refresh the token and retry the request
// On refresh failure: clear tokens and redirect to login
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = getRefreshToken();
        const { data } = await axios.post<{ accessToken: string }>(
          `${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh`,
          { refreshToken }
        );
        setToken(data.accessToken);
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return axiosInstance(originalRequest);
      } catch {
        clearTokens();
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
```

---

#### `src/utils/tokenStorage.ts`

```typescript
// Access token: stored in memory only (most secure against XSS)
// Refresh token: stored in sessionStorage (cleared when tab closes)
// In production, consider httpOnly cookies for the refresh token

let inMemoryToken: string | null = null;

export const setToken = (token: string): void => {
  inMemoryToken = token;
};

export const getToken = (): string | null => inMemoryToken;

export const setRefreshToken = (token: string): void => {
  sessionStorage.setItem('refreshToken', token);
};

export const getRefreshToken = (): string | null => {
  return sessionStorage.getItem('refreshToken');
};

export const clearTokens = (): void => {
  inMemoryToken = null;
  sessionStorage.removeItem('refreshToken');
};
```

---

#### `src/context/AuthContext.tsx`

```typescript
import {
  createContext,
  useContext,
  useState,
  useCallback,
  ReactNode,
} from 'react';
import { AuthUser, AuthContextType } from '../types/auth.types';
import { setToken, setRefreshToken, clearTokens } from '../utils/tokenStorage';

const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const login = useCallback(
    (userData: AuthUser, accessToken: string, refreshToken: string): void => {
      setToken(accessToken);
      setRefreshToken(refreshToken);
      setUser(userData);
    },
    []
  );

  const logout = useCallback(async (): Promise<void> => {
    try {
      const { logoutApi } = await import('../api/authApi');
      await logoutApi();
    } catch {
      // Even if the API call fails, clear local state
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

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside <AuthProvider>');
  }
  return context;
}
```

---

#### `src/routes/ProtectedRoute.tsx`

```typescript
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Spinner from '../components/ui/Spinner';

export default function ProtectedRoute() {
  const { user, isLoading } = useAuth();

  if (isLoading) return <Spinner />;
  if (!user)     return <Navigate to="/login" replace />;

  return <Outlet />;
}
```

---

#### `src/routes/RoleGuard.tsx`

```typescript
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Role } from '../types/auth.types';

interface RoleGuardProps {
  allowedRoles: Role[];
}

export default function RoleGuard({ allowedRoles }: RoleGuardProps) {
  const { user } = useAuth();

  if (!user || !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
```

---

#### `src/api/authApi.ts`

```typescript
import axiosInstance from './axiosInstance';
import { AuthResponse } from '../types/auth.types';

export interface RegisterPayload {
  fullName:    string;
  phoneNumber: string;
  password:    string;
  subCity:     string;
  kebele?:     string;
  address:     string;
}

export interface LoginPayload {
  phoneNumber: string;
  password:    string;
}

export const registerApi = async (payload: RegisterPayload): Promise<AuthResponse> => {
  const { data } = await axiosInstance.post<AuthResponse>('/api/auth/register', payload);
  return data;
};

export const loginApi = async (payload: LoginPayload): Promise<AuthResponse> => {
  const { data } = await axiosInstance.post<AuthResponse>('/api/auth/login', payload);
  return data;
};

export const logoutApi = async (): Promise<void> => {
  await axiosInstance.post('/api/auth/logout');
};

export const refreshTokenApi = async (refreshToken: string): Promise<{ accessToken: string }> => {
  const { data } = await axiosInstance.post<{ accessToken: string }>(
    '/api/auth/refresh',
    { refreshToken }
  );
  return data;
};
```

---

#### `src/api/requestsApi.ts`

```typescript
import axiosInstance from './axiosInstance';
import { PickupRequestResponse, SubmitRequestPayload, StatusHistoryEntry } from '../types/request.types';
import { PagedResponse } from '../types/api.types';

export const getMyRequestsApi = async (
  status?: string,
  page = 0,
  size = 10
): Promise<PagedResponse<PickupRequestResponse>> => {
  const { data } = await axiosInstance.get<PagedResponse<PickupRequestResponse>>(
    '/api/requests',
    { params: { status, page, size } }
  );
  return data;
};

export const getMyRequestByIdApi = async (
  id: string
): Promise<PickupRequestResponse> => {
  const { data } = await axiosInstance.get<PickupRequestResponse>(`/api/requests/${id}`);
  return data;
};

export const submitRequestApi = async (
  payload: SubmitRequestPayload
): Promise<PickupRequestResponse> => {
  const { data } = await axiosInstance.post<PickupRequestResponse>('/api/requests', payload);
  return data;
};

export const cancelRequestApi = async (id: string): Promise<PickupRequestResponse> => {
  const { data } = await axiosInstance.patch<PickupRequestResponse>(
    `/api/requests/${id}/cancel`
  );
  return data;
};
```

---

#### `src/constants/queryKeys.ts`

```typescript
// Centralise all React Query cache keys
// Prevents typos and makes cache invalidation safe and predictable

export const QUERY_KEYS = {
  // Citizen
  MY_REQUESTS:       ['my-requests'] as const,
  MY_REQUEST_DETAIL: (id: string) => ['my-requests', id] as const,

  // Collector
  COLLECTOR_TASKS:   ['collector-tasks'] as const,
  COLLECTOR_HISTORY: ['collector-history'] as const,

  // Admin
  ALL_REQUESTS:      ['admin-requests'] as const,
  ALL_COLLECTORS:    ['admin-collectors'] as const,
  ALL_CITIZENS:      ['admin-citizens'] as const,
  DASHBOARD_STATS:   ['admin-dashboard'] as const,

  // Shared
  NOTIFICATIONS:     ['notifications'] as const,
  UNREAD_COUNT:      ['notifications', 'unread-count'] as const,
} as const;
```

---

#### `src/constants/statusConfig.ts`

```typescript
import { RequestStatus } from '../types/request.types';

interface StatusConfigEntry {
  label: string;
  color: string;   // Tailwind badge classes
  dot:   string;   // Tailwind dot color class
}

export const STATUS_CONFIG: Record<RequestStatus, StatusConfigEntry> = {
  PENDING: {
    label: 'Pending',
    color: 'bg-yellow-100 text-yellow-800',
    dot:   'bg-yellow-500',
  },
  ASSIGNED: {
    label: 'Assigned',
    color: 'bg-blue-100 text-blue-800',
    dot:   'bg-blue-500',
  },
  IN_PROGRESS: {
    label: 'In Progress',
    color: 'bg-orange-100 text-orange-800',
    dot:   'bg-orange-500',
  },
  COMPLETED: {
    label: 'Completed',
    color: 'bg-green-100 text-green-800',
    dot:   'bg-green-500',
  },
  CANCELLED: {
    label: 'Cancelled',
    color: 'bg-gray-100 text-gray-500',
    dot:   'bg-gray-400',
  },
  FAILED: {
    label: 'Failed',
    color: 'bg-red-100 text-red-800',
    dot:   'bg-red-500',
  },
};
```

---

#### `src/utils/cn.ts`

```typescript
import { clsx, type ClassValue } from 'clsx';

// Utility for conditional Tailwind class merging
// Usage: cn('base-class', condition && 'conditional-class', 'another-class')
export function cn(...inputs: ClassValue[]): string {
  return clsx(inputs);
}
```

---

#### `src/App.tsx` — full routing with TypeScript

```tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import RoleGuard from './routes/RoleGuard';

// Auth
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

// Citizen
import CitizenDashboard from './pages/citizen/CitizenDashboard';
import SubmitRequestPage from './pages/citizen/SubmitRequestPage';
import RequestHistoryPage from './pages/citizen/RequestHistoryPage';
import RequestDetailPage from './pages/citizen/RequestDetailPage';

// Collector
import CollectorDashboard from './pages/collector/CollectorDashboard';
import AssignmentDetailPage from './pages/collector/AssignmentDetailPage';
import CollectorHistoryPage from './pages/collector/CollectorHistoryPage';

// Admin
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
      retry:     1,
      staleTime: 60_000, // 1 minute
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>

            {/* Public */}
            <Route path="/login"        element={<LoginPage />} />
            <Route path="/register"     element={<RegisterPage />} />
            <Route path="/unauthorized" element={<UnauthorizedPage />} />

            {/* Authenticated */}
            <Route element={<ProtectedRoute />}>

              {/* Citizen only */}
              <Route element={<RoleGuard allowedRoles={['CITIZEN']} />}>
                <Route path="/dashboard"    element={<CitizenDashboard />} />
                <Route path="/requests/new" element={<SubmitRequestPage />} />
                <Route path="/requests"     element={<RequestHistoryPage />} />
                <Route path="/requests/:id" element={<RequestDetailPage />} />
              </Route>

              {/* Collector only */}
              <Route element={<RoleGuard allowedRoles={['COLLECTOR']} />}>
                <Route path="/collector"                    element={<CollectorDashboard />} />
                <Route path="/collector/assignments/:id"    element={<AssignmentDetailPage />} />
                <Route path="/collector/history"            element={<CollectorHistoryPage />} />
              </Route>

              {/* Admin only */}
              <Route element={<RoleGuard allowedRoles={['ADMIN']} />}>
                <Route path="/admin"             element={<AdminDashboard />} />
                <Route path="/admin/requests"    element={<RequestsListPage />} />
                <Route path="/admin/collectors"  element={<CollectorsListPage />} />
                <Route path="/admin/citizens"    element={<CitizensListPage />} />
              </Route>

            </Route>

            <Route path="/"  element={<Navigate to="/login" replace />} />
            <Route path="*"  element={<NotFoundPage />} />

          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
```

---

#### `src/vite-env.d.ts` — type your env variables

Replace the default content with this:

```typescript
/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
```

This tells TypeScript exactly what environment variables exist.
If you typo `VITE_API_BASE_ULR` your editor will catch it immediately.

---

#### `tsconfig.json` — ensure these settings are present

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "moduleResolution": "bundler",
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

The key settings:
- `"strict": true` — enables all strict TypeScript checks. Always use this.
- `"noUnusedLocals"` + `"noUnusedParameters"` — catches dead code
- `"noFallthroughCasesInSwitch"` — prevents accidental switch fall-throughs
- `"paths"` — lets you import with `@/components/...` instead of `../../components/...`

---

### Tailwind config

`frontend/tailwind.config.js`:

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
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
  plugins: [require('@tailwindcss/forms')],
};
```

### frontend/.env

```env
VITE_API_BASE_URL=http://localhost:8080
```

### Verify the frontend starts

```bash
cd frontend
npm run dev
```

Expected:
```
VITE v5.x  ready in Xms
→  Local:   http://localhost:5173/
```

Also run the TypeScript compiler check:

```bash
npm run build
```

Zero errors means your TypeScript config is clean.

---

## Step 4 — Set up PostgreSQL

```bash
psql -U postgres
```

```sql
CREATE DATABASE wastecollector;
CREATE USER wastecollector_user WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE wastecollector TO wastecollector_user;
\q
```

Update `backend/.env`:
```env
DB_USERNAME=wastecollector_user
DB_PASSWORD=your_password
```

Start Spring Boot — Flyway runs all migrations automatically.

---

## Step 5 — Git workflow

### Branch strategy

```
main          ← Always deployable. Never commit directly here.
  └── develop ← Integration branch. Merge features here first.
        ├── feature/auth-register
        ├── feature/submit-request
        └── feature/admin-assign
```

```bash
# Start from develop
git checkout -b develop

# Per feature
git checkout -b feature/auth-register

# Merge back when done
git checkout develop
git merge feature/auth-register
git push origin develop
```

### Commit message convention

```
<type>(<scope>): <short description>

Types:   feat | fix | chore | refactor | test | docs | style
Scopes:  auth | requests | collector | admin | db | ui | config | types

Examples:
  feat(auth): implement JWT token generation on login
  feat(types): add PickupRequest and RequestStatus TypeScript types
  fix(requests): prevent duplicate pending requests per citizen
  chore(db): add V2 migration for collector_profiles
  refactor(auth-context): extract token storage to separate utility
  test(auth): add unit tests for registration business rules
  docs: update README with TypeScript setup instructions
```

---

## Step 6 — Final verification checklist

### Backend
- [ ] Spring Boot starts without errors
- [ ] Flyway runs V1 migration and creates `users` table
- [ ] `http://localhost:8080/swagger-ui.html` loads
- [ ] `.env` is in `.gitignore` and not tracked by git

### Frontend
- [ ] `npm run dev` starts without errors
- [ ] `npm run build` completes with zero TypeScript errors
- [ ] `http://localhost:5173` loads in browser
- [ ] Tailwind styles are applying (test with a colored div)
- [ ] All folders created including `src/types/`

### Git
- [ ] `.gitignore` prevents `.env` files from being tracked
- [ ] `git status` shows clean working tree
- [ ] All changes committed to `develop` branch

```bash
git add .
git commit -m "chore: complete TypeScript project setup — backend, frontend, db connected"
git push origin develop
```

---

## What you have built

```
A Spring Boot backend that:
  ✓ Starts and connects to PostgreSQL
  ✓ Runs Flyway migrations automatically
  ✓ Has a clean package structure for 50+ classes without chaos
  ✓ Has Swagger UI auto-generated from your code
  ✓ Uses environment variables for all secrets

A React + TypeScript frontend that:
  ✓ Has strict TypeScript — bugs caught at compile time, not runtime
  ✓ Has typed API response interfaces matching your Spring Boot DTOs
  ✓ Has typed auth context — no guessing what user contains
  ✓ Has routing for all 3 roles with protected routes and role guards
  ✓ Has axios with automatic token attachment and silent refresh
  ✓ Has typed query keys — cache invalidation is safe and predictable
  ✓ Has typed status config — one place to change, updates everywhere

A Git setup that:
  ✓ Has a branch strategy (main → develop → feature/*)
  ✓ Has a commit message convention
  ✓ Never exposes secrets
```

The TypeScript types you defined in `src/types/` are the frontend mirror
of your Java DTOs in `dto/response/`. When your Spring Boot API changes a
field name, TypeScript will show you every frontend file that needs updating.
That is the power of types across the full stack.

---

## What comes next — Phase 4, Feature 1: Authentication

```
Backend (Spring Boot):
  → User.java entity
  → UserRepository.java
  → AuthService.java  (register, login, logout, refresh)
  → JwtTokenProvider.java
  → JwtAuthFilter.java
  → AuthController.java
  → Test every endpoint in Postman

Frontend (React + TypeScript):
  → authApi.ts (already written above)
  → LoginPage.tsx  (react-hook-form + zod validation)
  → RegisterPage.tsx
  → Wire AuthContext login() after successful API call
  → Test end to end: register → login → redirected by role
```

Authentication first. Everything else in the system depends on it.

---

*Document version: 2.0 (TypeScript edition)*
*Created: May 2026*
*Project: Ethiopia Waste Collector System*
*Stack: React 18 + TypeScript + Vite + Spring Boot 3 + PostgreSQL 15*
*Previous: phase2-system-design-revised.md*
*Next: feature-01-authentication.md*