# Professional software engineer — project workflow guide

> Keep this document. Before starting any project, read it top to bottom.
> It is not a rulebook — it is a map. Experienced engineers follow this
> naturally. You will too, once it becomes habit.

---

## Why this exists

Most developers who struggle do not struggle because they lack skill.
They struggle because they start coding too early, without thinking,
and then spend most of their time untangling decisions they made in a hurry.

This guide is what separates engineers who consistently ship working systems
from those who are always "almost done."

---

## The 7 phases of building a professional project

```
Phase 1 → Understand
Phase 2 → Plan and design
Phase 3 → Set up the project
Phase 4 → Build iteratively
Phase 5 → Test as you go
Phase 6 → Document
Phase 7 → Deploy and reflect
```

Each phase has a clear goal and a set of outputs (things you produce).
You do not move to the next phase until the current one is done.

---

## Phase 1 — Understand the problem

**Goal:** Know deeply what you are building and why, before touching any tools.

A real engineer spends time here. A rushed engineer skips it and pays for it
in bugs, rework, and confused architecture later.

### What to do

**1. Write the problem statement in plain language**
In one paragraph, explain the problem this system solves. Write it as if
explaining to a non-technical person. If you cannot do this clearly,
you do not understand the problem yet.

**2. Define what the system is NOT**
List at least 3 things your system will not do in version 1.
This is called scope management. Every professional project has explicit scope.

**3. Identify all users (actors)**
For each user, write:
- Who they are (age, role, technical level)
- What they care about
- What frustrates them today without this system

**4. Write user stories**
Use this exact format for every feature:
> As a [user], I want to [do something], so that [reason/benefit].

Prioritize each story:
- Must have — system does not work without it
- Should have — important but not blocking
- Nice to have — add only if time allows

**5. Define what each user CANNOT do**
This becomes your security and access control logic later.

### Outputs of phase 1
- [ ] Problem statement (1 paragraph)
- [ ] Scope: what this system is NOT
- [ ] User profiles (one per actor)
- [ ] User stories with priorities
- [ ] Permissions list (what each user can and cannot do)

### Red flags — do not move forward if:
- You cannot explain the problem in one paragraph
- You are unsure who the users are
- You have not written any user stories

---

## Phase 2 — Plan and design the system

**Goal:** Make every major technical decision on paper before writing code.
Decisions made on paper are free. Decisions made in running code are expensive.

### What to do

**1. Draw the system architecture**
Sketch the high-level picture: what are the major components and how do they
connect? Even a rough drawing on paper is enough.

For a full stack web app, this typically includes:
- Frontend (React, mobile app, etc.)
- Backend API (Spring Boot, Node, etc.)
- Database (PostgreSQL, MySQL, etc.)
- External services (maps, notifications, payments, etc.)

**2. Design the database schema**
Before writing a single entity class or migration, draw your tables:
- What tables exist?
- What columns does each table have?
- What are the data types (string, integer, boolean, timestamp)?
- What are the relationships (one-to-many, many-to-many)?
- What columns need indexes (things you will search or filter by often)?

Write it out — even in a text file — before opening your IDE.

**3. Design your API endpoints**
List every HTTP endpoint your backend will expose:

```
Method   | Endpoint                        | Who calls it  | What it does
---------|----------------------------------|---------------|-----------------------------
POST     | /api/auth/register              | Public        | Register a new user
POST     | /api/auth/login                 | Public        | Login, returns JWT token
GET      | /api/requests                   | Citizen       | Get my pickup requests
POST     | /api/requests                   | Citizen       | Submit a new pickup request
PATCH    | /api/requests/{id}/cancel       | Citizen       | Cancel a pending request
GET      | /api/admin/requests             | Admin         | Get all requests
POST     | /api/admin/requests/{id}/assign | Admin         | Assign request to collector
```

You do not need to list every single endpoint upfront. List the obvious ones.
The rest will emerge naturally as you build.

**4. Choose your tech stack (if not already chosen)**
For each layer, pick one technology and commit to it. Do not switch halfway.
Document your choices and the reason for each.

Example:
```
Layer       | Technology    | Reason
------------|---------------|-----------------------------------------------
Frontend    | React + Vite  | Component-based, large community, good for SPAs
Backend     | Spring Boot   | Mature, strong security, good for REST APIs
Database    | PostgreSQL    | Relational, reliable, free, good Spring support
Auth        | JWT           | Stateless, works well with REST
Maps        | Leaflet.js    | Free, open source, no API key needed
Deployment  | Railway       | Free tier, supports Docker, easy CI/CD
```

**5. Identify your biggest unknowns (risks)**
What parts of the system are you least sure how to build?
Write them down. These are what you will build first as small prototypes
(called spikes) to validate your approach before committing.

### Outputs of phase 2
- [ ] System architecture diagram (even hand-drawn)
- [ ] Database schema (tables, columns, relationships)
- [ ] API endpoint list
- [ ] Tech stack decision with reasons
- [ ] List of technical risks/unknowns

### Red flags — do not move forward if:
- You have not drawn your database schema
- You do not know your tech stack
- You cannot list at least 5 API endpoints

---

## Phase 3 — Set up the project properly

**Goal:** Create a clean, professional foundation before writing any feature code.
Setup done wrong will haunt you for the entire project.

### What to do

**1. Create the Git repository first**

```bash
git init
git remote add origin <your-github-url>
```

Do this before anything else. Every change from this point goes into Git.

**2. Create a meaningful .gitignore**
Never commit:
- `.env` files (secrets, API keys, passwords)
- `node_modules/` or build output directories
- IDE config files (`.idea/`, `.vscode/` in most cases)
- Database files or logs

**3. Write a basic README.md immediately**
Include at minimum:
- Project name and one-sentence description
- Tech stack
- How to run it locally (you will fill this in as you go)

**4. Set up your folder structure correctly**

For Spring Boot backend:
```
src/
  main/
    java/com/yourproject/
      controller/      ← HTTP layer — receives requests, returns responses
      service/         ← Business logic — the "brain" of each feature
      repository/      ← Database access — talks to PostgreSQL
      model/           ← Entity classes — map to database tables
      dto/             ← Data Transfer Objects — what the API sends/receives
      security/        ← Auth config, JWT filters
      config/          ← Spring config classes
    resources/
      application.yml  ← App config (database URL, JWT secret, etc.)
```

For React frontend:
```
src/
  components/          ← Reusable UI pieces
  pages/               ← Full page components (one per route)
  services/            ← API call functions (axios, fetch)
  hooks/               ← Custom React hooks
  context/             ← Global state (auth context, etc.)
  utils/               ← Helper functions
```

**5. Set up environment variables**
Never hardcode secrets in code. Use environment variables from day one.

Backend `application.yml`:
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

Create a `.env.example` file with the variable names but no real values,
so anyone cloning the repo knows what they need.

**6. Connect and verify your database**
Run the application. Confirm it connects to the database and creates tables.
Do not proceed until this works.

**7. Set up your first Git commit**

```bash
git add .
git commit -m "chore: initial project setup with folder structure and config"
git push origin main
```

### Outputs of phase 3
- [ ] Git repository created and pushed to GitHub
- [ ] .gitignore configured
- [ ] README.md created (even if basic)
- [ ] Folder structure in place
- [ ] Environment variables configured
- [ ] App connects to database successfully
- [ ] Initial commit pushed

### Red flags — do not move forward if:
- You have not created a Git repo
- Your app does not connect to the database
- You have hardcoded passwords or secrets in your code

---

## Phase 4 — Build one feature at a time

**Goal:** Deliver complete, working features — never two half-built ones.

This is the most important discipline of professional development.
The rule is: finish one feature fully before starting the next.

### The feature development cycle

For every single feature, follow this exact order:

```
1. Pick the next feature (smallest useful thing)
      ↓
2. Build the backend first
   - Create the database entity / migration
   - Create the repository
   - Write the service (business logic)
   - Write the controller (HTTP endpoint)
   - Test with Postman or curl
      ↓
3. Build the frontend
   - Create the API service function
   - Build the React component or page
   - Connect to the backend
      ↓
4. Test end-to-end
   - Does the full flow work? (frontend → API → database → response → UI)
      ↓
5. Commit to Git with a clear message
      ↓
6. Move to the next feature
```

### What "backend first" means

Always build the API and confirm it works before building any UI.
This way, if something breaks, you know immediately whether it is a frontend
problem or a backend problem. Mixing them makes debugging chaotic.

### How to pick the next feature

Build in this general order:
1. Authentication (register, login, JWT) — everything else depends on this
2. Core data model (the main entity: e.g. PickupRequest)
3. The most essential user action (e.g. submit a request)
4. The most essential admin action (e.g. assign a request)
5. Secondary features (history, filters, notifications)
6. Polish (error messages, loading states, empty states)

### Git commit discipline

Write commits that explain *what changed and why*, not just *what you did*.

```
Good commits:
  feat: add citizen pickup request submission endpoint
  feat: connect React form to POST /api/requests
  fix: prevent duplicate pending requests per citizen
  chore: add index on requests.status column
  refactor: extract request validation to separate service method

Bad commits:
  update
  fix stuff
  changes
  wip
  asdfgh
```

Use this prefix convention:
- `feat:` — new feature
- `fix:` — bug fix
- `chore:` — setup, config, tooling
- `refactor:` — restructuring existing code without changing behavior
- `docs:` — documentation updates
- `test:` — adding or updating tests

### Outputs of phase 4
For each feature:
- [ ] Database entity/migration created
- [ ] Service and repository written
- [ ] Controller endpoint working (tested with Postman)
- [ ] React component built and connected
- [ ] End-to-end flow verified
- [ ] Committed to Git with a clear message

### Red flags to watch for:
- You are building feature B before feature A is fully working
- You have uncommitted changes spanning multiple features
- Your commits say "update" or "changes"
- You are building the UI before testing the API

---

## Phase 5 — Test as you go

**Goal:** Catch bugs where they are born — not after 10 features are layered on top.

Testing is not a phase at the end. It is woven into every feature cycle.

### Levels of testing

**1. Manual API testing (always do this)**
After every backend endpoint, test it with Postman or curl before building
any frontend. Confirm:
- Does it return the right data?
- Does it return the right HTTP status code?
- Does it reject invalid input with a useful error message?
- Does it enforce permissions? (Try accessing it without a token)

**2. Unit tests (write for critical business logic)**
Test the service layer — the business rules.
Ask: "What could go wrong in this logic?"

Examples of things to unit test:
- A citizen cannot submit a request if they already have a pending one
- A request cannot be cancelled if it is already assigned
- Only an available collector can be assigned to a request

```java
// Example: Spring Boot JUnit test
@Test
void shouldThrowExceptionWhenCitizenAlreadyHasPendingRequest() {
    // arrange
    when(requestRepository.existsByUserAndStatus(user, Status.PENDING))
        .thenReturn(true);

    // act + assert
    assertThrows(BusinessException.class,
        () -> requestService.submitRequest(user, requestDto));
}
```

**3. Integration tests (write for key flows)**
Test that the full flow from HTTP request to database works correctly.
Spring Boot makes this relatively easy with `@SpringBootTest`.

**4. Frontend testing (basic)**
Manually walk through the UI for every feature after building it.
Check: Does the form submit? Does the error show? Does the success state work?

### Self code review habit

Before every commit, read your own code as if you did not write it.
Ask yourself:
- Is this readable? Would another developer understand it?
- Is there duplicated code I could extract?
- Are there edge cases I have not handled?
- Are there any hardcoded values that should be configurable?
- Is the error handling complete?

### Outputs of phase 5
- [ ] Every API endpoint tested in Postman before frontend is built
- [ ] Unit tests written for business rules in the service layer
- [ ] Self code review done before each commit
- [ ] No obvious unhandled edge cases

---

## Phase 6 — Document as you build

**Goal:** Make the project understandable to your future self and anyone else.

Professional codebases are documented. Not every line — but the important parts.

### What to document

**1. README.md — keep it updated**
```markdown
# Project name

One sentence description.

## Tech stack
- Frontend: React + Vite + Tailwind
- Backend: Spring Boot 3
- Database: PostgreSQL
- Auth: JWT

## How to run locally

### Prerequisites
- Java 21
- Node 20
- PostgreSQL 15

### Backend
```bash
cd backend
cp .env.example .env   # fill in your values
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## API documentation
Available at http://localhost:8080/swagger-ui.html when running locally.
```

**2. ARCHITECTURE.md — your technical decisions**
Record why you made key technical choices. Future you will forget.

```markdown
## Why PostgreSQL over MySQL
PostgreSQL has better support for advanced queries and is fully open source.
It also has native array types which may be useful for tags in future versions.

## Why JWT over sessions
Our API is stateless and may be consumed by mobile clients in future.
JWT is better suited for this use case than server-side sessions.
```

**3. Code comments — only where needed**
Do not comment what the code does. Comment why it does it.

```java
// Bad comment (explains WHAT — obvious from reading the code):
// Check if request exists
if (requestRepository.existsById(id)) { ... }

// Good comment (explains WHY — not obvious from reading):
// We check pending requests per citizen to enforce BR-01:
// a citizen may only have one active request at a time.
if (requestRepository.existsByUserAndStatus(user, Status.PENDING)) { ... }
```

**4. Swagger / OpenAPI — document your API automatically**
Add the Springdoc dependency to your Spring Boot project:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```
This generates interactive API documentation automatically at `/swagger-ui.html`.

### Outputs of phase 6
- [ ] README.md is complete and accurate
- [ ] Anyone can run the project locally by following README
- [ ] Key architectural decisions are recorded
- [ ] Swagger/OpenAPI is set up
- [ ] Critical business logic has explanatory comments

---

## Phase 7 — Deploy and reflect

**Goal:** Ship it. An undeployed project teaches you only half of what a deployed one does.

### Deploy the application

**Step 1: Containerize with Docker**

Create a `Dockerfile` for the backend:
```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Create a `docker-compose.yml` for local full-stack setup:
```yaml
version: '3.8'
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: wastecollector
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://db:5432/wastecollector
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - db

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
```

**Step 2: Choose a hosting platform**

For learning projects, these are free or near-free:

| Platform | Best for | Free tier |
|----------|---------|-----------|
| Railway | Spring Boot + PostgreSQL | Yes (limited hours) |
| Render | Spring Boot + PostgreSQL | Yes (slow cold start) |
| Fly.io | Docker containers | Yes (generous) |
| Vercel | React frontend only | Yes (excellent) |
| Netlify | React frontend only | Yes |

**Step 3: Set up CI/CD (optional but valuable)**

A basic GitHub Actions workflow that builds and tests on every push:

```yaml
# .github/workflows/ci.yml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build and test
        run: cd backend && ./mvnw test
```

### Reflect after shipping

This is the step most developers skip. It is also the step that compounds
your learning the fastest.

After deploying, write a short reflection document:

```markdown
# Project reflection — [Project name] — [Date]

## What I built
[One paragraph summary]

## What went well
- ...
- ...

## What was harder than expected
- ...
- ...

## What I would do differently
- ...
- ...

## What I learned that I will use in every future project
- ...
- ...

## What I want to learn next
- ...
```

This document is not for anyone else. It is for you. Engineers who reflect
grow faster than engineers who just move on.

### Outputs of phase 7
- [ ] Application is running in a deployed environment
- [ ] Docker setup is working
- [ ] A real URL exists that you can share
- [ ] Reflection document written

---

## Quick reference checklist

Use this before starting any project:

### Phase 1 — Understand
- [ ] Problem statement written in plain language
- [ ] Scope defined (what this is NOT)
- [ ] Users identified with profiles
- [ ] User stories written with priorities
- [ ] Permissions defined (can/cannot)

### Phase 2 — Plan
- [ ] System architecture drawn
- [ ] Database schema designed
- [ ] API endpoints listed
- [ ] Tech stack chosen and documented
- [ ] Risks/unknowns identified

### Phase 3 — Setup
- [ ] Git repo created and pushed
- [ ] .gitignore configured
- [ ] README created
- [ ] Folder structure established
- [ ] Environment variables configured
- [ ] Database connection verified

### Phase 4 — Build
- [ ] Building one feature at a time (backend first, then frontend)
- [ ] Every feature committed before starting the next
- [ ] Commit messages are clear and meaningful

### Phase 5 — Test
- [ ] Every API endpoint tested in Postman before frontend
- [ ] Business rules have unit tests
- [ ] Self code review before every commit

### Phase 6 — Document
- [ ] README is complete and accurate
- [ ] Architectural decisions recorded
- [ ] Swagger/OpenAPI set up

### Phase 7 — Deploy
- [ ] Docker setup working
- [ ] Application deployed
- [ ] Reflection document written

---

## The mindset that holds all of this together

**Think before you type.**
Every minute you spend planning saves ten minutes of debugging.

**Finish what you start.**
Half-built features create confusion. One working feature is better than
three broken ones.

**Git is your safety net.**
Commit often. Commit small. Write clear messages. You cannot undo what
you never saved.

**Read your own code.**
Before asking for help, read your code out loud. Explain what each part
does. You will often find the bug yourself.

**Ship it.**
A deployed project with 5 features is worth more for your learning than
a local project with 20. Deployment teaches you things no tutorial can.

**Reflect.**
After every project, write what you learned. This is how experience
becomes wisdom, and wisdom is what makes every next project easier.

---

*This document is a living reference. Update it as you learn more.*
*Version 1.0 — May 2026*