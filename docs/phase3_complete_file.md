# Phase 3 — Complete project setup from scratch
# Tailored for: Arch Linux | Java 21 | Maven 3.9.14 | PostgreSQL | VS Code

> Every command in this document is written for YOUR machine.
> Run them in order. Do not skip steps.
> By the end, you will have a running backend and frontend
> that talk to each other — before writing a single feature.

---

## Your machine profile

| Tool        | Version / Detail                          |
|-------------|-------------------------------------------|
| OS          | Arch Linux                                |
| Java        | 21 LTS (via source use-java21.sh)         |
| Maven       | 3.9.14                                    |
| PostgreSQL  | Installed and running                     |
| Database    | wastecollector                            |
| DB User     | wastecollector_user                       |
| Editor      | VS Code                                   |
| Frontend    | React 18 + TypeScript + Vite              |

---

## The mental model before you touch the keyboard

Before running a single command, understand what you are building:

```
Your machine
│
├── Terminal window 1: Spring Boot running on port 8080
│     └── Talks to PostgreSQL on port 5432
│
├── Terminal window 2: React (Vite) running on port 5173
│     └── Makes HTTP requests to Spring Boot on port 8080
│
└── VS Code: editing both backend/ and frontend/ in one window
```

When you open your browser at localhost:5173 and click a button:
```
Browser (React)
  → HTTP request to localhost:8080 (Spring Boot)
    → Spring Boot queries PostgreSQL
      → PostgreSQL returns data
    → Spring Boot returns JSON
  → React displays the data
```

That full circle is what you are setting up today.
No features yet. Just the pipe that all future features flow through.

---

## Step 1 — Install Node.js

```bash
sudo pacman -S nodejs npm

# Verify both
node --version    # v20.x.x or higher
npm --version     # 10.x.x or higher
```

---

## Step 2 — Create the project root

```bash
# Go to wherever you keep your projects
# Based on your terminal path, you already have this folder:
cd ~/Full-Stack-Project/Wastage-Collector-System

# Confirm you are in the right place
pwd
# Should print: /home/yourname/Full-Stack-Project/Wastage-Collector-System

# Create the three top-level folders
mkdir -p backend frontend docs

# Verify
ls
# backend/  frontend/  docs/
```

---

## Step 3 — Set up Java 21 for this project

Every time you open a new terminal to work on this project, run this first.
We create the script once and source it every time.

```bash
# Create the switcher script in the project root
cat > use-java21.sh << 'EOF'
#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH
echo "✓ Java switched to:"
java -version
echo ""
echo "✓ Maven is using:"
mvn -version
EOF

chmod +x use-java21.sh

# Use it now
source use-java21.sh
```

Expected output:
```
✓ Java switched to:
openjdk version "21.x.x" ...

✓ Maven is using:
Apache Maven 3.9.14 ...
Java version: 21.x.x
```

**Important:** Every new terminal session needs `source use-java21.sh`
before running any `mvn` command. Add it to your workflow habit.

---

## Step 4 — Create the root Git files

```bash
# Still in the project root
# Create .gitignore
cat > .gitignore << 'EOF'
# ── Java / Maven ─────────────────────────────────────
target/
*.class
*.jar
*.war
*.ear
*.log

# ── Spring Boot ──────────────────────────────────────
!backend/.mvn/wrapper/
HELP.md

# ── Node / React ─────────────────────────────────────
node_modules/
frontend/dist/
frontend/build/

# ── Environment files (NEVER commit these) ───────────
.env
.env.local
.env.production
.env.development
*.env

# ── VS Code ──────────────────────────────────────────
.vscode/settings.json
.vscode/launch.json
# Keep .vscode/extensions.json (share recommended extensions)

# ── TypeScript build ─────────────────────────────────
*.tsbuildinfo

# ── OS files ─────────────────────────────────────────
.DS_Store
Thumbs.db
*~

# ── IDE ──────────────────────────────────────────────
.idea/
*.iml
EOF

# Create the README
cat > README.md << 'EOF'
# Waste Collector Ethiopia

A digital platform connecting citizens, waste collectors, and administrators
to make waste collection in Ethiopian cities reliable and accountable.

## Tech stack

| Layer      | Technology                                |
|------------|-------------------------------------------|
| Frontend   | React 18 + TypeScript + Vite + Tailwind   |
| Backend    | Spring Boot 3.2 + Spring Security         |
| Database   | PostgreSQL                                |
| Auth       | JWT (access + refresh tokens)             |
| Maps       | Leaflet.js                                |

## Roles
- **Citizen** — submits and tracks waste pickup requests
- **Collector** — receives assignments and updates pickup status
- **Admin** — assigns collectors, monitors operations, manages users

## Prerequisites
- Java 21 LTS
- Maven 3.9+
- PostgreSQL
- Node.js 20+

## Quick start

### Switch to Java 21
```bash
source use-java21.sh
```

### Backend
```bash
cd backend
cp .env.example .env      # fill in your values
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Project documents
- [Phase 1 — Problem understanding](docs/phase1-project-understanding.md)
- [Phase 2 — System design](docs/phase2-system-design-revised.md)
- [Phase 3 — Project setup](docs/phase3-project-setup.md)
EOF
```

---

## Step 5 — Set up VS Code for this project

Create the VS Code workspace config:

```bash
mkdir -p .vscode

# Recommended extensions for this project
cat > .vscode/extensions.json << 'EOF'
{
  "recommendations": [
    "vscjava.vscode-java-pack",
    "vmware.vscode-spring-boot",
    "vscjava.vscode-spring-initializr",
    "vscjava.vscode-spring-boot-dashboard",
    "dbaeumer.vscode-eslint",
    "esbenp.prettier-vscode",
    "bradlc.vscode-tailwindcss",
    "ms-vscode.vscode-typescript-next",
    "humao.rest-client",
    "mtxr.sqltools",
    "mtxr.sqltools-driver-pg",
    "eamodio.gitlens",
    "pkief.material-icon-theme"
  ]
}
EOF

# Workspace settings (shared with team — safe to commit)
cat > .vscode/settings.json << 'EOF'
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.tabSize": 2,
  "editor.rulers": [100],
  "files.trimTrailingWhitespace": true,
  "files.insertFinalNewline": true,

  "java.configuration.runtimes": [
    {
      "name": "JavaSE-21",
      "path": "/usr/lib/jvm/java-21-openjdk",
      "default": true
    }
  ],
  "java.compile.nullAnalysis.mode": "automatic",
  "spring-boot.ls.java.home": "/usr/lib/jvm/java-21-openjdk",

  "[java]": {
    "editor.defaultFormatter": "redhat.java",
    "editor.tabSize": 4
  },
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "[typescriptreact]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },

  "typescript.tsdk": "frontend/node_modules/typescript/lib",
  "tailwindCSS.includeLanguages": {
    "typescriptreact": "html"
  }
}
EOF
```

Open VS Code in this folder:
```bash
code .
```

When VS Code opens, it will prompt you to install the recommended extensions.
Click **Install All**. These give you:
- Java language support + Spring Boot tools
- TypeScript + ESLint + Prettier
- Tailwind CSS autocomplete
- REST Client (test APIs from inside VS Code)
- SQLTools (query PostgreSQL from inside VS Code)
- GitLens (enhanced Git history)

---

## Step 6 — Set up the PostgreSQL database

```bash
# Connect to PostgreSQL as the default admin user
sudo -u postgres psql
```

Inside the psql prompt, run these commands one by one:

```sql
-- Create the database
CREATE DATABASE wastecollector;

-- Create the dedicated user with a password
CREATE USER wastecollector_user WITH ENCRYPTED PASSWORD 'wc_password_2026';

-- Grant full access to the database
GRANT ALL PRIVILEGES ON DATABASE wastecollector TO wastecollector_user;

-- Grant schema privileges (needed in PostgreSQL 15+)
\c wastecollector
GRANT ALL ON SCHEMA public TO wastecollector_user;
GRANT CREATE ON SCHEMA public TO wastecollector_user;

-- Verify
\l
-- Should show wastecollector in the list

\q
```

Test the connection with your new user:

```bash
psql -U wastecollector_user -d wastecollector -h localhost -W
# Enter password: wc_password_2026
# Should connect successfully
\q
```

---

## Step 7 — Build the Spring Boot backend from scratch

```bash
cd backend
```

### 7.1 — Create the Maven folder structure

```bash
# Create every package folder
mkdir -p src/main/java/com/wastecollector/api/config
mkdir -p src/main/java/com/wastecollector/api/controller
mkdir -p src/main/java/com/wastecollector/api/service
mkdir -p src/main/java/com/wastecollector/api/repository
mkdir -p src/main/java/com/wastecollector/api/model/entity
mkdir -p src/main/java/com/wastecollector/api/model/enums
mkdir -p src/main/java/com/wastecollector/api/dto/request
mkdir -p src/main/java/com/wastecollector/api/dto/response
mkdir -p src/main/java/com/wastecollector/api/security
mkdir -p src/main/java/com/wastecollector/api/exception
mkdir -p src/main/java/com/wastecollector/api/mapper
mkdir -p src/main/java/com/wastecollector/api/scheduler
mkdir -p src/main/resources/db/migration
mkdir -p src/test/java/com/wastecollector/api/service
mkdir -p src/test/java/com/wastecollector/api/controller
mkdir -p src/test/resources

# Verify the structure
find src -type d | sort
```

Expected output:
```
src
src/main
src/main/java
src/main/java/com
src/main/java/com/wastecollector
src/main/java/com/wastecollector/api
src/main/java/com/wastecollector/api/config
src/main/java/com/wastecollector/api/controller
src/main/java/com/wastecollector/api/dto
src/main/java/com/wastecollector/api/dto/request
src/main/java/com/wastecollector/api/dto/response
src/main/java/com/wastecollector/api/exception
src/main/java/com/wastecollector/api/mapper
src/main/java/com/wastecollector/api/model
src/main/java/com/wastecollector/api/model/entity
src/main/java/com/wastecollector/api/model/enums
src/main/java/com/wastecollector/api/repository
src/main/java/com/wastecollector/api/scheduler
src/main/java/com/wastecollector/api/security
src/main/java/com/wastecollector/api/service
src/main/resources
src/main/resources/db
src/main/resources/db/migration
src/test
src/test/java
src/test/java/com/wastecollector/api
src/test/java/com/wastecollector/api/controller
src/test/java/com/wastecollector/api/service
src/test/resources
```

---

### 7.2 — Create pom.xml

```bash
cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!--
        PARENT: Inherit Spring Boot defaults.
        This manages versions for all Spring libraries automatically.
        Spring Boot 3.2.x is the LTS-aligned version for Java 21.
    -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <!-- PROJECT IDENTITY -->
    <groupId>com.wastecollector</groupId>
    <artifactId>api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>
    <name>waste-collector-api</name>
    <description>Waste Collector Ethiopia — Backend API</description>

    <properties>
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <dependencies>

        <!-- ═══════════════════════════════════════════════════════
             SPRING BOOT STARTERS
             Each starter is a curated bundle of related libraries.
             You add one dependency, you get a complete feature set.
        ════════════════════════════════════════════════════════ -->

        <!--
            WEB: Spring MVC + embedded Tomcat + Jackson (JSON)
            This is what makes your app a web server.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!--
            SECURITY: Spring Security + BCrypt password encoder
            Locks every endpoint by default.
            You then open up what should be public.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!--
            DATA JPA: Hibernate ORM + Spring Data + transactions
            Maps Java classes to database tables.
            Generates SQL from method names automatically.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!--
            VALIDATION: @NotBlank @NotNull @Email @Size etc.
            Add @Valid to a controller method and Spring validates
            the request body automatically before it reaches your code.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- ═══════════════════════════════════════════════════════
             DATABASE
        ════════════════════════════════════════════════════════ -->

        <!--
            POSTGRESQL DRIVER: the connector between Java and PostgreSQL.
            scope=runtime because you never import PostgreSQL classes
            directly — JPA abstracts the database for you.
        -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!--
            FLYWAY: database migration tool.
            Runs SQL files (V1, V2, V3...) in order on startup.
            Tracks what has already run — never runs the same file twice.
            This is how you safely evolve your schema over time.
        -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- ═══════════════════════════════════════════════════════
             JWT AUTHENTICATION
             Three jars work together as one unit.
        ════════════════════════════════════════════════════════ -->

        <!-- API: the interfaces — you import these in your code -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <!-- IMPL: the implementation — loaded at runtime, never imported directly -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <!-- JACKSON: JSON serialization for JWT — runtime only -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>

        <!-- ═══════════════════════════════════════════════════════
             API DOCUMENTATION
        ════════════════════════════════════════════════════════ -->

        <!--
            SWAGGER / OPENAPI: reads your controllers and generates
            interactive API documentation automatically at /swagger-ui.html
        -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>

        <!-- ═══════════════════════════════════════════════════════
             DEVELOPER EXPERIENCE
        ════════════════════════════════════════════════════════ -->

        <!--
            DEVTOOLS: hot reload on file change.
            Only active in development — excluded from production jar.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!--
            LOMBOK: generates boilerplate at compile time.
            @Data → getters + setters + equals + hashCode + toString
            @Builder → fluent builder pattern
            @Slf4j → injects a logger field
            @RequiredArgsConstructor → constructor for final fields
        -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- ═══════════════════════════════════════════════════════
             TESTING
        ════════════════════════════════════════════════════════ -->

        <!--
            TEST STARTER: JUnit 5 + Mockito + AssertJ + Spring Test.
            scope=test — never included in production jar.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!--
            SECURITY TEST: @WithMockUser, security mock helpers.
            For testing secured endpoints without real authentication.
        -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>

    </dependencies>

    <build>
        <plugins>

            <!--
                SPRING BOOT MAVEN PLUGIN:
                1. Builds the executable "fat jar" with all dependencies inside
                2. Enables: mvn spring-boot:run
                Lombok is excluded from the final jar (only needed at compile time)
            -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>

            <!--
                MAVEN COMPILER PLUGIN:
                Compiles your Java source code.
                annotationProcessorPaths registers Lombok so it
                generates code during compilation.
            -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>

        </plugins>
    </build>

</project>
EOF
```

---

### 7.3 — Create application.yml

```bash
cat > src/main/resources/application.yml << 'EOF'
spring:
  application:
    name: waste-collector-api

  # ── Database ──────────────────────────────────────────────────────
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/wastecollector}
    username: ${DB_USERNAME:wastecollector_user}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      # Connection pool settings
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000    # 30 seconds
      idle-timeout: 600000         # 10 minutes
      max-lifetime: 1800000        # 30 minutes

  # ── JPA / Hibernate ───────────────────────────────────────────────
  jpa:
    hibernate:
      # validate: checks entities match DB schema — does NOT create tables
      # Flyway creates tables. Hibernate only validates them.
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          time_zone: UTC
    open-in-view: false

  # ── Flyway ────────────────────────────────────────────────────────
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true

  # ── Jackson (JSON serializer) ─────────────────────────────────────
  jackson:
    # Send dates as ISO strings, not timestamps
    serialization:
      write-dates-as-timestamps: false
    # Ignore unknown JSON fields (safe for API evolution)
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null

# ── Server ────────────────────────────────────────────────────────────
server:
  port: ${PORT:8080}
  error:
    include-message: never
    include-stacktrace: never
    include-binding-errors: never

# ── JWT ───────────────────────────────────────────────────────────────
jwt:
  secret: ${JWT_SECRET}
  access-token-expiry-ms: ${JWT_ACCESS_EXPIRY:3600000}
  refresh-token-expiry-days: ${JWT_REFRESH_EXPIRY:30}

# ── Swagger ───────────────────────────────────────────────────────────
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha

# ── Logging ───────────────────────────────────────────────────────────
logging:
  level:
    com.wastecollector: DEBUG
    org.springframework.security: DEBUG    # See auth decisions during dev
    org.springframework: WARN
    org.hibernate.SQL: WARN
  pattern:
    console: "%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
EOF
```

---

### 7.4 — Create the .env file

```bash
cat > .env << 'EOF'
DATABASE_URL=jdbc:postgresql://localhost:5432/wastecollector
DB_USERNAME=wastecollector_user
DB_PASSWORD=wc_password_2026
JWT_SECRET=waste-collector-ethiopia-super-secret-jwt-key-2026-must-be-64-chars-min
JWT_ACCESS_EXPIRY=3600000
JWT_REFRESH_EXPIRY=30
EOF

# Create the safe-to-commit example file
cat > .env.example << 'EOF'
DATABASE_URL=jdbc:postgresql://localhost:5432/wastecollector
DB_USERNAME=wastecollector_user
DB_PASSWORD=
JWT_SECRET=
JWT_ACCESS_EXPIRY=3600000
JWT_REFRESH_EXPIRY=30
EOF
```

### 7.5 — Create the env loader script

Rather than manually exporting vars every time, this script loads your .env:

```bash
cat > load-env.sh << 'EOF'
#!/bin/bash
# Loads backend/.env into the current shell session
# Usage: source backend/load-env.sh

set -a   # automatically export all variables
source "$(dirname "$0")/.env"
set +a   # stop auto-exporting

echo "✓ Environment variables loaded from .env"
echo "  DATABASE_URL: $DATABASE_URL"
echo "  DB_USERNAME:  $DB_USERNAME"
echo "  JWT_SECRET:   [set, ${#JWT_SECRET} chars]"
EOF

chmod +x load-env.sh
```

---

### 7.6 — Create the main application class

```bash
cat > src/main/java/com/wastecollector/api/WasteCollectorApiApplication.java << 'EOF'
package com.wastecollector.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Waste Collector Ethiopia API.
 *
 * @SpringBootApplication combines three annotations:
 *
 *   @Configuration
 *     → This class can define Spring beans
 *
 *   @EnableAutoConfiguration
 *     → Spring Boot auto-configures beans based on the classpath.
 *       Sees PostgreSQL driver on classpath → sets up DataSource.
 *       Sees Spring Security → locks all endpoints by default.
 *       Sees Flyway → runs migrations on startup.
 *
 *   @ComponentScan
 *     → Scans com.wastecollector.api and all sub-packages for:
 *       @Component, @Service, @Repository, @Controller, @RestController
 *       Registers them as Spring beans (managed objects).
 *
 * @EnableScheduling
 *   → Activates the @Scheduled annotation in scheduler classes.
 *     Without this, your cron jobs are defined but never run.
 */
@SpringBootApplication
@EnableScheduling
public class WasteCollectorApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WasteCollectorApiApplication.class, args);
    }
}
EOF
```

---

### 7.7 — Create all enum files

These are the type-safe constants your entire system uses.
Create them now — they are referenced by entities, DTOs, and services.

```bash
# Role.java
cat > src/main/java/com/wastecollector/api/model/enums/Role.java << 'EOF'
package com.wastecollector.api.model.enums;

/**
 * User roles in the system.
 * Stored as strings in the database (not integers) for readability.
 * Used by Spring Security for access control decisions.
 */
public enum Role {
    CITIZEN,
    COLLECTOR,
    ADMIN
}
EOF

# RequestStatus.java
cat > src/main/java/com/wastecollector/api/model/enums/RequestStatus.java << 'EOF'
package com.wastecollector.api.model.enums;

/**
 * All valid states of a pickup request.
 *
 * Valid transitions (enforced in PickupRequestService):
 *   PENDING     → ASSIGNED      (admin assigns a collector)
 *   PENDING     → CANCELLED     (citizen cancels)
 *   ASSIGNED    → IN_PROGRESS   (collector starts the job)
 *   ASSIGNED    → PENDING       (admin reassigns — collector removed)
 *   IN_PROGRESS → COMPLETED     (collector finishes successfully)
 *   IN_PROGRESS → FAILED        (collector cannot complete)
 *   FAILED      → PENDING       (admin decides to retry)
 *
 * Any other transition must throw BusinessException.
 */
public enum RequestStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    FAILED
}
EOF

# Availability.java
cat > src/main/java/com/wastecollector/api/model/enums/Availability.java << 'EOF'
package com.wastecollector.api.model.enums;

/**
 * Collector availability status.
 *
 *   AVAILABLE   → can be assigned new requests
 *   UNAVAILABLE → manually set by admin (day off, sick leave)
 *   ON_DUTY     → currently has an assigned or in-progress request
 */
public enum Availability {
    AVAILABLE,
    UNAVAILABLE,
    ON_DUTY
}
EOF

# NotificationType.java
cat > src/main/java/com/wastecollector/api/model/enums/NotificationType.java << 'EOF'
package com.wastecollector.api.model.enums;

/**
 * All notification types in the system.
 *
 * Citizen receives:
 *   REQUEST_SUBMITTED, REQUEST_ASSIGNED, REQUEST_IN_PROGRESS,
 *   REQUEST_COMPLETED, REQUEST_FAILED, REQUEST_CANCELLED
 *
 * Collector receives:
 *   NEW_ASSIGNMENT, ASSIGNMENT_REMOVED, REASSIGNED
 *
 * Admin receives:
 *   NEW_REQUEST, REQUEST_FAILED, REQUEST_CANCELLED
 */
public enum NotificationType {
    // Citizen notifications
    REQUEST_SUBMITTED,
    REQUEST_ASSIGNED,
    REQUEST_IN_PROGRESS,
    REQUEST_COMPLETED,
    REQUEST_FAILED,
    REQUEST_CANCELLED,

    // Collector notifications
    NEW_ASSIGNMENT,
    ASSIGNMENT_REMOVED,
    REASSIGNED,

    // Admin notifications
    NEW_REQUEST
}
EOF
```

---

### 7.8 — Create the first migration file

```bash
cat > src/main/resources/db/migration/V1__create_users_table.sql << 'EOF'
-- V1__create_users_table.sql
--
-- Flyway naming rules:
--   V{number}__{description}.sql
--   Double underscore between version and description
--   Flyway tracks runs in flyway_schema_history table
--   Once a migration runs, NEVER edit it — create a new one instead
--
-- This migration creates the users table only.
-- All other tables come in subsequent migrations (V2, V3, ...).
-- This order matters: tables with foreign keys come AFTER the tables they reference.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL UNIQUE,
    email         VARCHAR(150) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL
                  CHECK (role IN ('CITIZEN', 'COLLECTOR', 'ADMIN')),
    sub_city      VARCHAR(100),
    kebele        VARCHAR(50),
    address       TEXT,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Indexes: add for every column you will filter or sort by often
CREATE INDEX idx_users_phone  ON users(phone_number);
CREATE INDEX idx_users_role   ON users(role);
CREATE INDEX idx_users_active ON users(is_active);

-- Helpful comment in the database itself
COMMENT ON TABLE users IS
  'All system users: citizens, collectors, and admins. Role column determines permissions.';
COMMENT ON COLUMN users.password_hash IS
  'BCrypt hash of the password. Never store plaintext.';
COMMENT ON COLUMN users.is_active IS
  'False = account deactivated. User cannot log in.';
EOF
```

---

### 7.9 — Create the custom exception classes

These are needed early because services will throw them.

```bash
# BusinessException.java — for violated business rules
cat > src/main/java/com/wastecollector/api/exception/BusinessException.java << 'EOF'
package com.wastecollector.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a business rule is violated.
 *
 * Examples:
 *   - Citizen already has a pending request (BR-01)
 *   - Request cannot be cancelled because it is already assigned
 *   - Collector is not available for assignment
 *
 * Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
EOF

# ResourceNotFoundException.java — for missing records
cat > src/main/java/com/wastecollector/api/exception/ResourceNotFoundException.java << 'EOF'
package com.wastecollector.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource does not exist in the database.
 *
 * Examples:
 *   - Request ID does not exist
 *   - Collector ID does not exist
 *   - User not found during login
 *
 * Maps to HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resourceName, field, value));
    }
}
EOF

# UnauthorizedActionException.java — for permission violations
cat > src/main/java/com/wastecollector/api/exception/UnauthorizedActionException.java << 'EOF'
package com.wastecollector.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user attempts an action they are not permitted to perform.
 *
 * Examples:
 *   - Citizen tries to cancel another citizen's request
 *   - Collector tries to complete a request not assigned to them
 *
 * Maps to HTTP 403 Forbidden.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
EOF

# GlobalExceptionHandler.java — catches all exceptions, returns clean JSON
cat > src/main/java/com/wastecollector/api/exception/GlobalExceptionHandler.java << 'EOF'
package com.wastecollector.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Central exception handler for the entire API.
 *
 * Every exception thrown anywhere in the application flows here.
 * This class converts exceptions into consistent, clean JSON responses.
 *
 * Without this: Spring returns its default HTML error page — useless for an API.
 * With this: every error is a consistent JSON object that React can handle.
 *
 * Response shape:
 * {
 *   "status": 404,
 *   "message": "PickupRequest not found with id: 'abc'",
 *   "timestamp": "2026-05-10T10:30:00Z"
 * }
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── Custom exceptions ─────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.warn("Business rule violated: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedActionException ex) {
        log.warn("Unauthorized action: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(403, ex.getMessage()));
    }

    // ── Validation exceptions ─────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });

        log.warn("Validation failed: {}", errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ValidationErrorResponse(400, "Validation failed", errors));
    }

    // ── Security exceptions ───────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(401, "Invalid phone number or password"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(403, "You do not have permission to perform this action"));
    }

    // ── Catch-all ─────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500, "An unexpected error occurred"));
    }

    // ── Response record types ─────────────────────────────────────

    public record ErrorResponse(
        int status,
        String message,
        OffsetDateTime timestamp
    ) {
        public ErrorResponse(int status, String message) {
            this(status, message, OffsetDateTime.now());
        }
    }

    public record ValidationErrorResponse(
        int status,
        String message,
        Map<String, String> errors,
        OffsetDateTime timestamp
    ) {
        public ValidationErrorResponse(int status, String message, Map<String, String> errors) {
            this(status, message, errors, OffsetDateTime.now());
        }
    }
}
EOF
```

---

### 7.10 — Create a placeholder test

Maven requires at least one test class or the build fails.

```bash
cat > src/test/java/com/wastecollector/api/WasteCollectorApiApplicationTests.java << 'EOF'
package com.wastecollector.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the application context loads successfully.
 * If Spring cannot wire all beans together, this test fails.
 * This is the first test every Spring Boot project should have.
 */
@SpringBootTest
@ActiveProfiles("test")
class WasteCollectorApiApplicationTests {

    @Test
    void contextLoads() {
        // If this method runs without throwing an exception,
        // the entire Spring context loaded successfully.
        // That means all beans are wired, DB connected, config valid.
    }
}
EOF
```

Create a test application config:

```bash
cat > src/test/resources/application-test.yml << 'EOF'
# Test configuration — overrides application.yml during tests
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wastecollector_test
    username: wastecollector_user
    password: wc_password_2026
  flyway:
    enabled: true
  jpa:
    show-sql: true

jwt:
  secret: test-secret-key-for-testing-only-must-be-at-least-64-characters-long
  access-token-expiry-ms: 3600000
  refresh-token-expiry-days: 30

logging:
  level:
    com.wastecollector: DEBUG
EOF
```

---

### 7.11 — First Maven build

```bash
# Make sure Java 21 is active
source ../use-java21.sh

# Load environment variables
source load-env.sh

# Download all dependencies and compile
mvn clean compile
```

Expected: `BUILD SUCCESS`

Now run the application:

```bash
mvn spring-boot:run
```

Expected output:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \

INFO  Starting WasteCollectorApiApplication
INFO  Flyway Community Edition by Redgate
INFO  Database: jdbc:postgresql://localhost:5432/wastecollector
INFO  Successfully validated 1 migration
INFO  Migrating schema "public" to version "1 - create users table"
INFO  Successfully applied 1 migration
INFO  Started WasteCollectorApiApplication in 4.2 seconds
```

**Verify in your browser:**
- http://localhost:8080/swagger-ui.html → Swagger UI loads (no endpoints yet — correct)

**Verify in your database:**
```bash
psql -U wastecollector_user -d wastecollector -h localhost -W
```
```sql
\dt
-- Shows: flyway_schema_history, users
SELECT * FROM flyway_schema_history;
-- Shows V1 migration as successful
\q
```

---

## Step 8 — Build the React + TypeScript frontend

```bash
# Go to the frontend folder
cd ../frontend

# Create the Vite + React + TypeScript project
npm create vite@latest . -- --template react-ts
# When asked "current directory is not empty" → select: Ignore files and continue

# Install base dependencies
npm install
```

### 8.1 — Install all project dependencies

```bash
# Runtime dependencies
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

# Development dependencies
npm install -D \
  tailwindcss \
  postcss \
  autoprefixer \
  @tailwindcss/forms \
  @types/leaflet \
  @types/node \
  eslint \
  @typescript-eslint/eslint-plugin \
  @typescript-eslint/parser \
  eslint-plugin-react-hooks \
  eslint-plugin-react-refresh \
  prettier \
  eslint-config-prettier

# Initialize Tailwind
npx tailwindcss init -p
```

### 8.2 — Create the complete folder structure

```bash
# Create every folder
mkdir -p src/types
mkdir -p src/api
mkdir -p src/hooks
mkdir -p src/context
mkdir -p src/pages/auth
mkdir -p src/pages/citizen
mkdir -p src/pages/collector
mkdir -p src/pages/admin
mkdir -p src/pages/shared
mkdir -p src/components/layout
mkdir -p src/components/ui
mkdir -p src/components/requests
mkdir -p src/components/map
mkdir -p src/components/notifications
mkdir -p src/routes
mkdir -p src/utils
mkdir -p src/constants

# Verify
find src -type d | sort
```

### 8.3 — Configure TypeScript strictly

```bash
cat > tsconfig.json << 'EOF'
{
  "compilerOptions": {
    "target": "ES2020",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "moduleResolution": "bundler",
    "jsx": "react-jsx",

    /* Strictness — always enable all of these */
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "noImplicitReturns": true,

    /* Module resolution */
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,

    /* Path aliases — import from @/ instead of ../../ */
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
EOF
```

### 8.4 — Configure Vite with path aliases

```bash
cat > vite.config.ts << 'EOF'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      // Allows: import Button from '@/components/ui/Button'
      // Instead of: import Button from '../../components/ui/Button'
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    // Proxy API calls to Spring Boot during development
    // This avoids CORS issues in development
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
EOF
```

### 8.5 — Configure Tailwind

```bash
cat > tailwind.config.js << 'EOF'
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // Ethiopian green brand palette
        brand: {
          50:  '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
          800: '#166534',
          900: '#14532d',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [require('@tailwindcss/forms')],
}
EOF
```

### 8.6 — Configure Prettier

```bash
cat > .prettierrc << 'EOF'
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100,
  "bracketSpacing": true,
  "arrowParens": "always"
}
EOF

cat > .prettierignore << 'EOF'
dist/
node_modules/
*.md
EOF
```

### 8.7 — Update vite-env.d.ts with typed env vars

```bash
cat > src/vite-env.d.ts << 'EOF'
/// <reference types="vite/client" />

// Type your environment variables here
// If you add a new VITE_ variable, add it here too
// TypeScript will catch typos at compile time
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
EOF
```

### 8.8 — Create the .env file

```bash
cat > .env << 'EOF'
VITE_API_BASE_URL=http://localhost:8080
EOF
```

### 8.9 — Create all TypeScript type files

```bash
# auth.types.ts
cat > src/types/auth.types.ts << 'EOF'
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
  user:      AuthUser | null;
  isLoading: boolean;
  login:     (userData: AuthUser, accessToken: string, refreshToken: string) => void;
  logout:    () => Promise<void>;
}
EOF

# request.types.ts
cat > src/types/request.types.ts << 'EOF'
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
  oldStatus: RequestStatus | null;
  newStatus: RequestStatus;
  changedBy: string;
  note:      string | null;
  changedAt: string;
}
EOF

# api.types.ts
cat > src/types/api.types.ts << 'EOF'
// Generic paginated API response — matches Spring Boot's Page<T>
export interface PagedResponse<T> {
  content:       T[];
  totalElements: number;
  totalPages:    number;
  currentPage:   number;
  size:          number;
}

// Standard error response from GlobalExceptionHandler
export interface ApiError {
  status:    number;
  message:   string;
  timestamp: string;
}

// Validation error response (has per-field errors map)
export interface ValidationError extends ApiError {
  errors: Record<string, string>;
}
EOF

# notification.types.ts
cat > src/types/notification.types.ts << 'EOF'
export type NotificationType =
  | 'REQUEST_SUBMITTED'
  | 'REQUEST_ASSIGNED'
  | 'REQUEST_IN_PROGRESS'
  | 'REQUEST_COMPLETED'
  | 'REQUEST_FAILED'
  | 'REQUEST_CANCELLED'
  | 'NEW_ASSIGNMENT'
  | 'ASSIGNMENT_REMOVED'
  | 'REASSIGNED'
  | 'NEW_REQUEST';

export interface NotificationResponse {
  id:        string;
  type:      NotificationType;
  title:     string;
  message:   string;
  isRead:    boolean;
  requestId: string | null;
  createdAt: string;
}
EOF

# admin.types.ts
cat > src/types/admin.types.ts << 'EOF'
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

export interface CollectorResponse {
  id:               string;
  fullName:         string;
  phoneNumber:      string;
  subCity:          string | null;
  availability:     'AVAILABLE' | 'UNAVAILABLE' | 'ON_DUTY';
  assignedSubCity:  string | null;
  vehicleType:      string | null;
  isActive:         boolean;
}
EOF
```

### 8.10 — Create the token storage utility

```bash
cat > src/utils/tokenStorage.ts << 'EOF'
/**
 * Token storage strategy:
 *
 * Access token  → memory only (most secure, cleared on tab close/refresh)
 * Refresh token → sessionStorage (survives page refresh, cleared on tab close)
 *
 * Why not localStorage?
 * localStorage persists forever and is vulnerable to XSS attacks.
 * Any injected script can read it. Memory is safer for short-lived tokens.
 *
 * Production recommendation: use httpOnly cookies for refresh tokens.
 * That requires backend changes (Set-Cookie header) and is a v2 improvement.
 */

let inMemoryAccessToken: string | null = null;

export const setToken = (token: string): void => {
  inMemoryAccessToken = token;
};

export const getToken = (): string | null => inMemoryAccessToken;

export const setRefreshToken = (token: string): void => {
  sessionStorage.setItem('wc_refresh', token);
};

export const getRefreshToken = (): string | null => {
  return sessionStorage.getItem('wc_refresh');
};

export const clearTokens = (): void => {
  inMemoryAccessToken = null;
  sessionStorage.removeItem('wc_refresh');
};
EOF
```

### 8.11 — Create the axios instance

```bash
cat > src/api/axiosInstance.ts << 'EOF'
import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosError } from 'axios';
import { getToken, getRefreshToken, setToken, clearTokens } from '@/utils/tokenStorage';

/**
 * The central HTTP client for the entire frontend.
 *
 * Every API call goes through this instance. Configure once, benefit everywhere.
 *
 * What it does automatically on every request:
 *   1. Attaches the JWT access token to the Authorization header
 *   2. On 401 (token expired): silently fetches a new token and retries
 *   3. On refresh failure: clears tokens and redirects to login
 *
 * This means every page/component can make API calls without thinking
 * about authentication at all — the interceptor handles it.
 */
const axiosInstance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10_000,
});

// ── Request interceptor ────────────────────────────────────────────────
// Runs before every outgoing request
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// ── Response interceptor ───────────────────────────────────────────────
// Runs after every response (success or failure)
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // If 401 and we have not already tried to refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = getRefreshToken();
        if (!refreshToken) throw new Error('No refresh token');

        // Call refresh endpoint directly (not through axiosInstance — avoids loop)
        const { data } = await axios.post<{ accessToken: string }>(
          `${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh`,
          { refreshToken }
        );

        // Store the new access token and retry the original request
        setToken(data.accessToken);
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return axiosInstance(originalRequest);

      } catch {
        // Refresh failed — session is dead, force logout
        clearTokens();
        window.location.href = '/login';
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
EOF
```

### 8.12 — Create the AuthContext

```bash
cat > src/context/AuthContext.tsx << 'EOF'
import {
  createContext,
  useContext,
  useState,
  useCallback,
  ReactNode,
} from 'react';
import { AuthUser, AuthContextType } from '@/types/auth.types';
import { setToken, setRefreshToken, clearTokens } from '@/utils/tokenStorage';

const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * AuthProvider wraps the entire application.
 * It holds the logged-in user state and provides login/logout functions.
 *
 * Any component can call useAuth() to:
 *   - Read the current user: const { user } = useAuth()
 *   - Check the role:       if (user?.role === 'ADMIN')
 *   - Log out:              const { logout } = useAuth()
 */
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
    setIsLoading(true);
    try {
      // Tell the server to revoke the refresh token
      const { logoutApi } = await import('@/api/authApi');
      await logoutApi();
    } catch {
      // Even if the API call fails, clear local state
      // The refresh token will expire naturally on the server
    } finally {
      clearTokens();
      setUser(null);
      setIsLoading(false);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * useAuth — the hook every component uses to access auth state.
 *
 * Usage:
 *   const { user, login, logout } = useAuth();
 *
 * Throws if used outside AuthProvider — catches missing wrapper early.
 */
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside <AuthProvider>');
  }
  return context;
}
EOF
```

### 8.13 — Create route guards

```bash
# ProtectedRoute — requires authentication
cat > src/routes/ProtectedRoute.tsx << 'EOF'
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

/**
 * Wraps routes that require the user to be logged in.
 * If not authenticated → redirects to /login.
 * If authenticated → renders the child route (<Outlet />).
 *
 * Usage in App.tsx:
 *   <Route element={<ProtectedRoute />}>
 *     <Route path="/dashboard" element={<Dashboard />} />
 *   </Route>
 */
export default function ProtectedRoute() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-600" />
      </div>
    );
  }

  return user ? <Outlet /> : <Navigate to="/login" replace />;
}
EOF

# RoleGuard — requires a specific role
cat > src/routes/RoleGuard.tsx << 'EOF'
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Role } from '@/types/auth.types';

interface RoleGuardProps {
  allowedRoles: Role[];
}

/**
 * Wraps routes that require a specific user role.
 * If wrong role → redirects to /unauthorized.
 *
 * Usage in App.tsx:
 *   <Route element={<RoleGuard allowedRoles={['ADMIN']} />}>
 *     <Route path="/admin" element={<AdminDashboard />} />
 *   </Route>
 */
export default function RoleGuard({ allowedRoles }: RoleGuardProps) {
  const { user } = useAuth();

  if (!user || !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
EOF
```

### 8.14 — Create the constants files

```bash
cat > src/constants/queryKeys.ts << 'EOF'
/**
 * Centralised React Query cache keys.
 *
 * Why centralise?
 * React Query caches data by key. If you use string literals everywhere,
 * a typo gives you two separate caches for the same data.
 * Centralising here makes cache invalidation safe and predictable.
 *
 * Usage:
 *   useQuery({ queryKey: QUERY_KEYS.MY_REQUESTS, queryFn: ... })
 *   queryClient.invalidateQueries({ queryKey: QUERY_KEYS.MY_REQUESTS })
 */
export const QUERY_KEYS = {
  MY_REQUESTS:       ['my-requests'] as const,
  MY_REQUEST_DETAIL: (id: string) => ['my-requests', id] as const,

  COLLECTOR_TASKS:   ['collector-tasks'] as const,
  COLLECTOR_HISTORY: ['collector-history'] as const,

  ALL_REQUESTS:      ['admin-requests'] as const,
  ALL_COLLECTORS:    ['admin-collectors'] as const,
  ALL_CITIZENS:      ['admin-citizens'] as const,
  DASHBOARD_STATS:   ['admin-dashboard'] as const,

  NOTIFICATIONS:     ['notifications'] as const,
  UNREAD_COUNT:      ['notifications', 'unread-count'] as const,
} as const;
EOF

cat > src/constants/statusConfig.ts << 'EOF'
import { RequestStatus } from '@/types/request.types';

interface StatusConfigEntry {
  label: string;
  color: string;   // Tailwind CSS badge classes
  dot:   string;   // Tailwind CSS indicator dot class
}

/**
 * Maps each RequestStatus to its UI representation.
 *
 * Why Record<RequestStatus, ...>?
 * TypeScript enforces that EVERY status has an entry here.
 * If you add a new status to the type and forget to add it here,
 * TypeScript shows an error immediately — before you even run the app.
 */
export const STATUS_CONFIG: Record<RequestStatus, StatusConfigEntry> = {
  PENDING: {
    label: 'Pending',
    color: 'bg-yellow-100 text-yellow-800 border border-yellow-200',
    dot:   'bg-yellow-500',
  },
  ASSIGNED: {
    label: 'Assigned',
    color: 'bg-blue-100 text-blue-800 border border-blue-200',
    dot:   'bg-blue-500',
  },
  IN_PROGRESS: {
    label: 'In Progress',
    color: 'bg-orange-100 text-orange-800 border border-orange-200',
    dot:   'bg-orange-500',
  },
  COMPLETED: {
    label: 'Completed',
    color: 'bg-green-100 text-green-800 border border-green-200',
    dot:   'bg-green-500',
  },
  CANCELLED: {
    label: 'Cancelled',
    color: 'bg-gray-100 text-gray-500 border border-gray-200',
    dot:   'bg-gray-400',
  },
  FAILED: {
    label: 'Failed',
    color: 'bg-red-100 text-red-800 border border-red-200',
    dot:   'bg-red-500',
  },
};
EOF

cat > src/constants/routes.ts << 'EOF'
/**
 * All route paths in one place.
 * Use these constants instead of string literals in <Link> and navigate().
 * A typo here fails at compile time. A typo in a string literal fails at runtime.
 */
export const ROUTES = {
  // Public
  LOGIN:    '/login',
  REGISTER: '/register',

  // Citizen
  CITIZEN_DASHBOARD:      '/dashboard',
  CITIZEN_NEW_REQUEST:    '/requests/new',
  CITIZEN_REQUESTS:       '/requests',
  CITIZEN_REQUEST_DETAIL: (id: string) => `/requests/${id}`,

  // Collector
  COLLECTOR_DASHBOARD:        '/collector',
  COLLECTOR_ASSIGNMENT_DETAIL: (id: string) => `/collector/assignments/${id}`,
  COLLECTOR_HISTORY:           '/collector/history',

  // Admin
  ADMIN_DASHBOARD:   '/admin',
  ADMIN_REQUESTS:    '/admin/requests',
  ADMIN_COLLECTORS:  '/admin/collectors',
  ADMIN_CITIZENS:    '/admin/citizens',

  // Shared
  UNAUTHORIZED: '/unauthorized',
} as const;
EOF
```

### 8.15 — Create the utility functions

```bash
cat > src/utils/cn.ts << 'EOF'
import { clsx, type ClassValue } from 'clsx';

/**
 * Utility for conditional Tailwind class merging.
 *
 * Usage:
 *   cn('base-class', isActive && 'active-class', 'another-class')
 *   cn('px-4', size === 'lg' && 'py-3', size === 'sm' && 'py-1')
 */
export function cn(...inputs: ClassValue[]): string {
  return clsx(inputs);
}
EOF

cat > src/utils/formatDate.ts << 'EOF'
/**
 * Date formatting utilities.
 * Centralise all date formatting here so you change it in one place.
 */

export const formatDate = (isoString: string): string => {
  return new Date(isoString).toLocaleDateString('en-ET', {
    year:  'numeric',
    month: 'long',
    day:   'numeric',
  });
};

export const formatDateTime = (isoString: string): string => {
  return new Date(isoString).toLocaleString('en-ET', {
    year:   'numeric',
    month:  'short',
    day:    'numeric',
    hour:   '2-digit',
    minute: '2-digit',
  });
};

export const formatRelativeTime = (isoString: string): string => {
  const now  = new Date();
  const date = new Date(isoString);
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60_000);

  if (diffMins < 1)   return 'just now';
  if (diffMins < 60)  return `${diffMins}m ago`;
  if (diffMins < 1440) return `${Math.floor(diffMins / 60)}h ago`;
  return `${Math.floor(diffMins / 1440)}d ago`;
};
EOF
```

### 8.16 — Create placeholder page components

Every page file needs to exist (even empty) so the imports in App.tsx compile:

```bash
# Auth pages
echo "export default function LoginPage() { return <div>Login Page</div>; }" > src/pages/auth/LoginPage.tsx
echo "export default function RegisterPage() { return <div>Register Page</div>; }" > src/pages/auth/RegisterPage.tsx

# Citizen pages
echo "export default function CitizenDashboard() { return <div>Citizen Dashboard</div>; }" > src/pages/citizen/CitizenDashboard.tsx
echo "export default function SubmitRequestPage() { return <div>Submit Request</div>; }" > src/pages/citizen/SubmitRequestPage.tsx
echo "export default function RequestHistoryPage() { return <div>Request History</div>; }" > src/pages/citizen/RequestHistoryPage.tsx
echo "export default function RequestDetailPage() { return <div>Request Detail</div>; }" > src/pages/citizen/RequestDetailPage.tsx

# Collector pages
echo "export default function CollectorDashboard() { return <div>Collector Dashboard</div>; }" > src/pages/collector/CollectorDashboard.tsx
echo "export default function AssignmentDetailPage() { return <div>Assignment Detail</div>; }" > src/pages/collector/AssignmentDetailPage.tsx
echo "export default function CollectorHistoryPage() { return <div>Collector History</div>; }" > src/pages/collector/CollectorHistoryPage.tsx

# Admin pages
echo "export default function AdminDashboard() { return <div>Admin Dashboard</div>; }" > src/pages/admin/AdminDashboard.tsx
echo "export default function RequestsListPage() { return <div>Requests List</div>; }" > src/pages/admin/RequestsListPage.tsx
echo "export default function CollectorsListPage() { return <div>Collectors List</div>; }" > src/pages/admin/CollectorsListPage.tsx
echo "export default function CitizensListPage() { return <div>Citizens List</div>; }" > src/pages/admin/CitizensListPage.tsx

# Shared pages
echo "export default function NotFoundPage() { return <div>404 Not Found</div>; }" > src/pages/shared/NotFoundPage.tsx
echo "export default function UnauthorizedPage() { return <div>403 Unauthorized</div>; }" > src/pages/shared/UnauthorizedPage.tsx
```

### 8.17 — Create App.tsx with full routing

```bash
cat > src/App.tsx << 'EOF'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '@/context/AuthContext';
import ProtectedRoute from '@/routes/ProtectedRoute';
import RoleGuard from '@/routes/RoleGuard';
import { ROUTES } from '@/constants/routes';

import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import CitizenDashboard from '@/pages/citizen/CitizenDashboard';
import SubmitRequestPage from '@/pages/citizen/SubmitRequestPage';
import RequestHistoryPage from '@/pages/citizen/RequestHistoryPage';
import RequestDetailPage from '@/pages/citizen/RequestDetailPage';
import CollectorDashboard from '@/pages/collector/CollectorDashboard';
import AssignmentDetailPage from '@/pages/collector/AssignmentDetailPage';
import CollectorHistoryPage from '@/pages/collector/CollectorHistoryPage';
import AdminDashboard from '@/pages/admin/AdminDashboard';
import RequestsListPage from '@/pages/admin/RequestsListPage';
import CollectorsListPage from '@/pages/admin/CollectorsListPage';
import CitizensListPage from '@/pages/admin/CitizensListPage';
import NotFoundPage from '@/pages/shared/NotFoundPage';
import UnauthorizedPage from '@/pages/shared/UnauthorizedPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry:     1,
      staleTime: 60_000,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>

            {/* ── Public routes ──────────────────────────────── */}
            <Route path={ROUTES.LOGIN}        element={<LoginPage />} />
            <Route path={ROUTES.REGISTER}     element={<RegisterPage />} />
            <Route path={ROUTES.UNAUTHORIZED} element={<UnauthorizedPage />} />

            {/* ── Protected routes (must be logged in) ──────── */}
            <Route element={<ProtectedRoute />}>

              {/* Citizen only */}
              <Route element={<RoleGuard allowedRoles={['CITIZEN']} />}>
                <Route path={ROUTES.CITIZEN_DASHBOARD}   element={<CitizenDashboard />} />
                <Route path={ROUTES.CITIZEN_NEW_REQUEST} element={<SubmitRequestPage />} />
                <Route path={ROUTES.CITIZEN_REQUESTS}    element={<RequestHistoryPage />} />
                <Route path="/requests/:id"              element={<RequestDetailPage />} />
              </Route>

              {/* Collector only */}
              <Route element={<RoleGuard allowedRoles={['COLLECTOR']} />}>
                <Route path={ROUTES.COLLECTOR_DASHBOARD} element={<CollectorDashboard />} />
                <Route path="/collector/assignments/:id" element={<AssignmentDetailPage />} />
                <Route path={ROUTES.COLLECTOR_HISTORY}   element={<CollectorHistoryPage />} />
              </Route>

              {/* Admin only */}
              <Route element={<RoleGuard allowedRoles={['ADMIN']} />}>
                <Route path={ROUTES.ADMIN_DASHBOARD}  element={<AdminDashboard />} />
                <Route path={ROUTES.ADMIN_REQUESTS}   element={<RequestsListPage />} />
                <Route path={ROUTES.ADMIN_COLLECTORS} element={<CollectorsListPage />} />
                <Route path={ROUTES.ADMIN_CITIZENS}   element={<CitizensListPage />} />
              </Route>

            </Route>

            {/* Default and catch-all */}
            <Route path="/" element={<Navigate to={ROUTES.LOGIN} replace />} />
            <Route path="*" element={<NotFoundPage />} />

          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
EOF
```

### 8.18 — Update main.tsx and index.css

```bash
cat > src/main.tsx << 'EOF'
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>
);
EOF

cat > src/index.css << 'EOF'
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  body {
    @apply bg-gray-50 text-gray-900 antialiased;
  }
}
EOF
```

### 8.19 — Verify the frontend builds

```bash
# Type check — zero errors expected
npm run build
```

Expected:
```
vite v5.x.x building for production...
✓ 40 modules transformed.
dist/index.html         x.xx kB
dist/assets/index.css   x.xx kB
dist/assets/index.js    xx.xx kB
✓ built in x.xxs
```

Start the dev server:

```bash
npm run dev
```

Expected:
```
VITE v5.x.x  ready in xxx ms
➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

Open http://localhost:5173 — you should see the Login page placeholder.

---

## Step 9 — Final Git commit

```bash
# Go back to project root
cd ..

# Initialize git if not done yet
git init
git remote add origin https://github.com/YOUR_USERNAME/waste-collector-ethiopia.git

# Stage everything
git add .

# Verify what is being committed (.env files should NOT appear)
git status
# You should NOT see: backend/.env or frontend/.env

# First commit
git commit -m "chore: complete project setup — Spring Boot + React TS + PostgreSQL connected"

# Create and push develop branch
git checkout -b develop
git push -u origin develop
```

---

## Step 10 — Verification checklist

Run through every item. Do not move to Feature 1 until all are green.

### Backend ✓
```bash
cd backend
source ../use-java21.sh && source load-env.sh
mvn spring-boot:run
```
- [ ] App starts without errors
- [ ] Flyway prints: "Successfully applied 1 migration"
- [ ] http://localhost:8080/swagger-ui.html loads
- [ ] `psql -U wastecollector_user -d wastecollector -h localhost -W`
      then `\dt` shows: `flyway_schema_history` and `users` tables

### Frontend ✓
```bash
cd frontend
npm run build   # zero TypeScript errors
npm run dev
```
- [ ] `npm run build` succeeds with zero errors
- [ ] http://localhost:5173 loads in browser
- [ ] Browser shows: "Login Page" (placeholder — correct)

### Git ✓
```bash
git status      # clean working tree
git log --oneline
cat .gitignore | grep ".env"   # .env is in gitignore
```
- [ ] No `.env` files tracked by git
- [ ] Clean working tree
- [ ] First commit visible in `git log`

---

## Your project structure right now

```
waste-collector-ethiopia/
  use-java21.sh              ← Source this every new terminal
  .gitignore
  README.md
  .vscode/
    extensions.json          ← Install all recommended extensions
    settings.json            ← Java 21, Prettier, Tailwind config

  backend/
    pom.xml                  ← All dependencies explained inline
    load-env.sh              ← Loads .env into shell
    .env                     ← Your secrets (never committed)
    .env.example             ← Template for other developers
    src/main/java/com/wastecollector/api/
      WasteCollectorApiApplication.java
      model/enums/           ← Role, RequestStatus, Availability, NotificationType
      exception/             ← GlobalExceptionHandler + 3 custom exceptions
      config/                ← Empty (filled in Feature 1: SecurityConfig)
      controller/            ← Empty (filled in Feature 1: AuthController)
      service/               ← Empty (filled in Feature 1: AuthService)
      repository/            ← Empty (filled in Feature 1: UserRepository)
      dto/                   ← Empty (filled in Feature 1: RegisterRequest etc.)
      security/              ← Empty (filled in Feature 1: JWT classes)
      mapper/                ← Empty (filled in Feature 1)
      scheduler/             ← Empty (filled later)
    src/main/resources/
      application.yml
      db/migration/V1__create_users_table.sql

  frontend/
    .env                     ← VITE_API_BASE_URL (never committed)
    vite.config.ts           ← Path aliases + dev proxy to :8080
    tsconfig.json            ← Strict TypeScript
    tailwind.config.js
    .prettierrc
    src/
      types/                 ← auth, request, api, notification, admin types
      api/axiosInstance.ts   ← Token attachment + silent refresh
      context/AuthContext.tsx ← Global auth state
      routes/                ← ProtectedRoute + RoleGuard
      constants/             ← queryKeys, statusConfig, routes
      utils/                 ← tokenStorage, cn, formatDate
      pages/                 ← All placeholder pages (filled in Feature 1)
      App.tsx                ← Full routing for all 3 roles
```

---

## What comes next — Feature 1: Authentication

The foundation is complete. Every future feature follows this same pattern:

```
Backend:
  1. Write the migration SQL (Vx__description.sql)
  2. Write the @Entity class in model/entity/
  3. Write the Repository interface in repository/
  4. Write the Service class in service/ (business logic here)
  5. Write the DTO classes in dto/request/ and dto/response/
  6. Write the Controller in controller/ (@RestController)
  7. Test every endpoint in Postman or VS Code REST Client

Frontend:
  8. Write the API functions in api/featureApi.ts
  9. Write the React Query hooks in hooks/useFeature.ts
  10. Build the page component in pages/
  11. Test the full flow end to end in the browser
```

Feature 1 follows this exact flow for authentication:
register → login → JWT token → protected routes → role-based redirect.

---

*Setup complete — May 2026*
*Java 21 | Maven 3.9.14 | Arch Linux | VS Code*
*React 18 + TypeScript + Vite | Spring Boot 3.2 | PostgreSQL*
