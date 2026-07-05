# Building Spring Boot from scratch — understanding Maven first

> We do NOT use Spring Initializr here.
> We build everything by hand so you understand what every file does.
> Once you finish this, the Initializr becomes a convenience, not a mystery.

---

## Part 1 — What is Maven?

Maven is a **build tool**. Its job is to answer three questions:

1. What libraries does my project depend on? (dependency management)
2. How do I compile, test, and package my code? (build lifecycle)
3. Where do I get those libraries from? (repository management)

Before Maven existed, Java developers downloaded `.jar` files manually,
placed them in folders, and hoped everything was compatible.
Maven solved this by letting you declare what you need in one file (`pom.xml`)
and it handles the rest automatically.

### The central concept: the POM

POM stands for **Project Object Model**.
The `pom.xml` file IS your project from Maven's perspective.
It answers: what is this project, what does it need, and how should it be built.

### Where do the libraries come from?

Maven downloads them from the **Maven Central Repository**
(https://central.sonatype.com) — a massive public server that hosts
virtually every Java library ever published.

The first time you add a dependency, Maven downloads it to a local cache
on your machine (`~/.m2/repository`). Subsequent builds use the cache.

---

## Part 2 — Install the tools

### Install Java 21

```bash
# On Ubuntu/Debian
sudo apt install openjdk-21-jdk

# On macOS (with Homebrew)
brew install openjdk@21

# Verify
java -version
# Should print: openjdk version "21.x.x"
```

### Install Maven

```bash
# On Ubuntu/Debian
sudo apt install maven

# On macOS
brew install maven

# Verify
mvn -version
# Should print: Apache Maven 3.x.x
```

### Verify JAVA_HOME is set

```bash
echo $JAVA_HOME
# Should print the path to your Java installation
# If empty, set it:
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
```

---

## Part 3 — The Maven folder structure

Maven enforces a standard folder layout. This is intentional —
every Maven project on earth uses the same structure,
so any Java developer can navigate any project immediately.

```
backend/
  pom.xml                          ← The project definition file
  src/
    main/
      java/                        ← Your application source code
        com/wastecollector/api/
          WasteCollectorApiApplication.java
      resources/                   ← Config files, SQL migrations, etc.
        application.yml
        db/
          migration/
    test/
      java/                        ← Your test source code
        com/wastecollector/api/
      resources/                   ← Test-specific config
```

**Why `src/main/java` and not just `src`?**
Because `src/test/java` also exists. Maven needs to know which code
is production code and which is test code. Test code is compiled separately
and never included in your final deployable jar.

**Why the long package path `com/wastecollector/api`?**
In Java, the folder structure must exactly match the package name.
A class in `com.wastecollector.api` must live in `com/wastecollector/api/`.
This is a Java language rule, not a Maven rule.

**Why `com.wastecollector`?**
Package names are conventionally written in reverse domain order.
If your company owns `wastecollector.com`, your packages start with
`com.wastecollector`. This guarantees global uniqueness.
For personal projects, `com.yourname` or `io.github.yourname` is common.

---

## Part 4 — Understanding pom.xml from scratch

Create the backend directory and the pom.xml manually:

```bash
cd waste-collector-ethiopia/backend
touch pom.xml
```

Now build it line by line. Open `pom.xml` and add each section:

### Section 1 — The XML declaration and schema

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
```

**What this does:**
- `<?xml ...>` — standard XML file header
- `<project>` — the root element of every pom.xml
- `xmlns` — declares which XML vocabulary this uses (Maven's POM format)
- `xsi:schemaLocation` — points to Maven's schema so IDEs can validate your XML
- `<modelVersion>4.0.0</modelVersion>` — always 4.0.0. This is the POM format version,
  not your project version. It has been 4.0.0 for 20 years.

---

### Section 2 — The parent (inheriting Spring Boot defaults)

```xml
    <!-- ─────────────────────────────────────────────────────────────
         PARENT
         We inherit from Spring Boot's parent POM.
         This gives us:
           - Pre-configured versions for hundreds of common libraries
           - Default Maven plugin configuration
           - Sensible build defaults
         Without this, we would have to specify every version manually.
    ──────────────────────────────────────────────────────────────── -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>
```

**What this does:**
The parent POM is a separate pom.xml maintained by the Spring team.
By inheriting from it, your project automatically gets correct, tested,
compatible versions for all Spring libraries.

For example: you add `spring-boot-starter-web` and do NOT specify a version.
Maven asks: "what version?" — looks at the parent — finds the answer.
This prevents the most common beginner mistake: incompatible library versions.

`<relativePath/>` — tells Maven "the parent is not a local file, find it online."

---

### Section 3 — Project coordinates (your project's identity)

```xml
    <!-- ─────────────────────────────────────────────────────────────
         PROJECT COORDINATES
         Every Maven artifact is identified by three things:
           groupId    — who made it (your organisation/domain)
           artifactId — what it is (the project name)
           version    — which version it is
         Together: groupId:artifactId:version is a unique identifier.
         Example: com.wastecollector:api:0.0.1-SNAPSHOT
    ──────────────────────────────────────────────────────────────── -->
    <groupId>com.wastecollector</groupId>
    <artifactId>api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>waste-collector-api</name>
    <description>Waste Collector Ethiopia — Backend API</description>
```

**What SNAPSHOT means:**
`0.0.1-SNAPSHOT` means "version 0.0.1, still in development."
SNAPSHOT versions are mutable — the same version number can have
different content at different times. When you release, you remove SNAPSHOT.

**What `<packaging>jar</packaging>` means:**
Maven will build this project into a single `.jar` file (Java Archive).
Spring Boot's jar is self-contained — it includes the embedded Tomcat server,
so you run it with `java -jar yourapp.jar` and it just works.

---

### Section 4 — Properties (variables you reuse)

```xml
    <!-- ─────────────────────────────────────────────────────────────
         PROPERTIES
         Variables that can be referenced throughout this pom.xml
         using ${property.name} syntax.
         The most important one is java.version — it tells Maven
         which Java version to compile for.
    ──────────────────────────────────────────────────────────────── -->
    <properties>
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>
```

**Why define Java version here?**
Without this, Maven defaults to Java 8 compatibility — ancient.
This property is read by the compiler plugin (inherited from the parent)
and tells it to compile for Java 21.

---

### Section 5 — Dependencies (the heart of pom.xml)

```xml
    <!-- ─────────────────────────────────────────────────────────────
         DEPENDENCIES
         Every library your project needs is listed here.
         Maven downloads them from Maven Central automatically.

         Each dependency has:
           groupId    — who published the library
           artifactId — the library name
           version    — which version (often omitted when parent manages it)
           scope      — when is this dependency needed?
                        compile  (default) — needed to compile AND run
                        runtime  — needed to run, not to compile
                        test     — needed only for running tests
                        provided — needed to compile, provided by the server at runtime
    ──────────────────────────────────────────────────────────────── -->
    <dependencies>

        <!-- ── SPRING BOOT CORE ─────────────────────────────────── -->

        <!--
            spring-boot-starter-web
            Includes: Spring MVC, embedded Tomcat, Jackson (JSON serializer)
            This is what makes your class a web server.
            Without this, your application starts and immediately stops.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!--
            spring-boot-starter-security
            Includes: Spring Security, password encoding (BCrypt)
            This locks down every endpoint by default.
            You then configure which endpoints are public.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!--
            spring-boot-starter-data-jpa
            Includes: Hibernate ORM, Spring Data JPA, transaction management
            This lets you write Java classes that map to database tables.
            JPA = Java Persistence API (the standard)
            Hibernate = the implementation of that standard
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!--
            spring-boot-starter-validation
            Includes: Hibernate Validator, Bean Validation API
            This enables annotations like @NotBlank, @NotNull, @Email
            on your DTO classes. Spring validates incoming request bodies
            automatically when you add @Valid to your controller method.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- ── DATABASE ─────────────────────────────────────────── -->

        <!--
            postgresql
            The JDBC driver — the connector between Java and PostgreSQL.
            scope=runtime because you never import PostgreSQL classes
            directly in your code (you use JPA abstractions instead).
            At runtime, JPA needs the driver to actually talk to the database.
        -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!--
            flyway-core
            Database migration tool. Runs SQL files in order (V1, V2, V3...)
            every time the application starts. Tracks which migrations have
            already run so it never runs the same one twice.
            This is how you safely evolve your database schema over time.
        -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <!--
            flyway-database-postgresql
            Flyway's PostgreSQL-specific support module.
            Required in addition to flyway-core when using PostgreSQL.
        -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- ── SECURITY / JWT ───────────────────────────────────── -->

        <!--
            jjwt-api
            The JWT library API — the interfaces and annotations.
            You import classes from this in your code.
        -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>

        <!--
            jjwt-impl
            The actual implementation of the JWT API.
            scope=runtime because you never import impl classes directly.
            You code to the API (jjwt-api), the impl is loaded automatically.
            This is the "program to interfaces, not implementations" principle.
        -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>

        <!--
            jjwt-jackson
            JWT serialization/deserialization using Jackson (JSON library).
            scope=runtime — loaded automatically, not imported directly.
        -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>

        <!-- ── API DOCUMENTATION ────────────────────────────────── -->

        <!--
            springdoc-openapi-starter-webmvc-ui
            Reads your @RestController classes and automatically generates
            Swagger UI documentation at /swagger-ui.html.
            You get interactive API docs for free.
        -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>

        <!-- ── DEVELOPER TOOLS ──────────────────────────────────── -->

        <!--
            spring-boot-devtools
            Hot reload — restarts the application automatically when
            you change a class. Only active in development, never in production.
            scope=runtime so it is not included in your production jar.
            optional=true means projects that depend on YOUR project
            do not inherit this dependency.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!--
            lombok
            Generates boilerplate code at compile time using annotations:
              @Data          → generates getters, setters, equals, hashCode, toString
              @Builder       → generates a builder pattern
              @NoArgsConstructor → generates no-argument constructor
              @AllArgsConstructor → generates constructor with all fields
              @Slf4j         → injects a logger field automatically

            optional=true because downstream projects do not need Lombok.
            Without Lombok you write all of this by hand.
        -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- ── TESTING ──────────────────────────────────────────── -->

        <!--
            spring-boot-starter-test
            Includes: JUnit 5, Mockito, AssertJ, Spring Test
            scope=test means this is ONLY available when running tests.
            It is never included in your production jar.
            This is the most important test dependency — it gives you
            everything you need to write unit and integration tests.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!--
            spring-security-test
            Adds security-specific test utilities:
              @WithMockUser   → simulate a logged-in user in tests
              SecurityMockMvcRequestPostProcessors → set auth in MockMvc calls
        -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>

    </dependencies>
```

---

### Section 6 — Build and plugins

```xml
    <!-- ─────────────────────────────────────────────────────────────
         BUILD
         Configures how Maven compiles and packages your project.
         Plugins are tools that run during the build lifecycle.
    ──────────────────────────────────────────────────────────────── -->
    <build>
        <plugins>

            <!--
                spring-boot-maven-plugin
                This plugin does two critical things:
                1. Repackages your jar into an executable "fat jar"
                   (includes all dependencies + embedded Tomcat inside)
                2. Enables ./mvnw spring-boot:run (run app without building jar first)

                The <configuration> block tells Lombok to be excluded
                from the final jar (it is only needed at compile time).
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
                maven-compiler-plugin
                Controls how Java source code is compiled.
                We set source and target to 21 explicitly here in addition
                to the <java.version> property — belts and suspenders approach.
                <annotationProcessorPaths> registers Lombok as an annotation
                processor so it runs during compilation and generates code.
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
```

---

### The complete pom.xml (all sections together)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

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
        <!-- Spring Boot core -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

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

        <!-- API Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>

        <!-- Developer tools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
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
```

---

## Part 5 — The Maven build lifecycle

This is the most important concept to understand about Maven.
Maven builds happen in phases, in order. Running a later phase
automatically runs all earlier phases first.

```
validate      → check pom.xml is correct
     ↓
compile       → compile src/main/java → target/classes/
     ↓
test-compile  → compile src/test/java → target/test-classes/
     ↓
test          → run all tests in src/test/java
     ↓
package       → bundle compiled code into a .jar file → target/api-0.0.1-SNAPSHOT.jar
     ↓
verify        → run integration tests, check the package is valid
     ↓
install       → copy the jar to your local ~/.m2 cache
     ↓
deploy        → upload the jar to a remote repository (for teams)
```

**Commands you will use constantly:**

```bash
# Compile only — check for syntax/type errors without running tests
mvn compile

# Run all tests
mvn test

# Build the jar file (runs compile + test first)
mvn package

# Build the jar, skip tests (use sparingly — tests exist for a reason)
mvn package -DskipTests

# Delete the target/ folder (clean build — use when things are weird)
mvn clean

# Clean then build
mvn clean package

# Run the Spring Boot application directly (most common during development)
mvn spring-boot:run

# Download all dependencies without building (useful first step)
mvn dependency:resolve
```

---

## Part 6 — Create the folder structure manually

```bash
# From inside backend/
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
mkdir -p src/test/java/com/wastecollector/api/controller1
mkdir -p src/test/resources
```

---

## Part 7 — Create the entry point

Every Spring Boot application needs exactly one entry point class.
This is the class that starts everything.

`src/main/java/com/wastecollector/api/WasteCollectorApiApplication.java`:

```java
package com.wastecollector.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Waste Collector Ethiopia API.
 *
 * @SpringBootApplication is a shortcut for three annotations:
 *   @Configuration       → this class can define Spring beans
 *   @EnableAutoConfiguration → Spring Boot automatically configures
 *                              beans based on what is on the classpath
 *                              (e.g. it sees PostgreSQL driver → sets up DataSource)
 *   @ComponentScan       → scans this package and all sub-packages
 *                          for @Component, @Service, @Repository, @Controller
 *
 * @EnableScheduling → activates the @Scheduled annotation in scheduler classes
 */
@SpringBootApplication
@EnableScheduling
public class WasteCollectorApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WasteCollectorApiApplication.class, args);
    }
}
```

**What `SpringApplication.run()` does:**
1. Creates the Spring ApplicationContext (the container that manages all beans)
2. Triggers auto-configuration based on the classpath
3. Starts the embedded Tomcat server on port 8080
4. Registers all `@Component`, `@Service`, `@Repository`, `@Controller` classes
5. Runs Flyway migrations against the database
6. The application is now ready to accept HTTP requests

---

## Part 8 — Create application.yml

```bash
touch src/main/resources/application.yml
```

```yaml
# ─────────────────────────────────────────────────────────────────────────
# application.yml
#
# This is your application's configuration file.
# YAML format uses indentation to represent hierarchy.
# ${VAR_NAME:default} means: read from environment variable VAR_NAME,
# use 'default' if the variable is not set.
# ─────────────────────────────────────────────────────────────────────────

spring:
  application:
    name: waste-collector-api     # Used in logs and monitoring

  # ── Database connection ──────────────────────────────────────────────
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/wastecollector}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

  # ── JPA / Hibernate ─────────────────────────────────────────────────
  jpa:
    hibernate:
      # validate: Hibernate checks that entities match the DB schema
      #           but does NOT create or alter tables.
      # Flyway creates/alters tables. Hibernate only validates.
      # NEVER use 'create', 'create-drop', or 'update' in production.
      ddl-auto: validate
    show-sql: false               # Set true when debugging SQL queries
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true          # Pretty-print SQL in logs when show-sql=true
    open-in-view: false           # IMPORTANT: prevents performance issues
                                  # with lazy-loaded entities in web layer

  # ── Flyway ───────────────────────────────────────────────────────────
  flyway:
    enabled: true
    locations: classpath:db/migration   # Where to find migration SQL files
    baseline-on-migrate: true           # Safe to use on existing databases

# ── Server ───────────────────────────────────────────────────────────────
server:
  port: ${PORT:8080}
  error:
    include-message: never        # Never expose exception messages in responses
    include-stacktrace: never     # Never expose stack traces in responses

# ── JWT ──────────────────────────────────────────────────────────────────
jwt:
  secret: ${JWT_SECRET}           # No default — MUST be set in environment
  access-token-expiry-ms: ${JWT_ACCESS_EXPIRY:3600000}    # 1 hour
  refresh-token-expiry-days: ${JWT_REFRESH_EXPIRY:30}      # 30 days

# ── Swagger / OpenAPI ────────────────────────────────────────────────────
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

# ── Logging ──────────────────────────────────────────────────────────────
logging:
  level:
    com.wastecollector: DEBUG     # Your code — see everything
    org.springframework: WARN    # Framework — only warnings and errors
    org.hibernate.SQL: WARN      # SQL — only when debugging
```

---

## Part 9 — Create the .env file

```bash
touch .env
```

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/wastecollector
DB_USERNAME=postgres
DB_PASSWORD=your_local_password
JWT_SECRET=super-secret-key-that-is-at-least-64-characters-long-for-security
JWT_ACCESS_EXPIRY=3600000
JWT_REFRESH_EXPIRY=30
```

**How Spring Boot reads .env:**
Spring Boot does not read `.env` files natively.
You load them into your shell before running the app:

```bash
# Option 1: export manually
export DATABASE_URL=jdbc:postgresql://localhost:5432/wastecollector
export DB_USERNAME=postgres
# ... etc

# Option 2: use a tool like dotenv
# Install: npm install -g dotenv-cli
dotenv -- mvn spring-boot:run

# Option 3: pass directly to Maven
DATABASE_URL=jdbc:postgresql://... DB_USERNAME=postgres mvn spring-boot:run
```

For IntelliJ IDEA: Run/Debug Configurations → Environment Variables → paste your vars.
For VS Code: use the "Spring Boot Dashboard" extension → environment settings.

---

## Part 10 — First migration file

```bash
touch src/main/resources/db/migration/V1__create_users_table.sql
```

```sql
-- V1__create_users_table.sql
--
-- Flyway naming convention: V{version}__{description}.sql
-- Version: integer or decimal (V1, V2, V1.1, V1.2)
-- Description: words separated by underscores
-- Double underscore separates version from description
--
-- Flyway tracks which migrations have run in a table called
-- flyway_schema_history. It will NEVER run the same version twice.
-- If you need to change something after V1 has run,
-- you create V2, not edit V1.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- Enables gen_random_uuid()

CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL UNIQUE,
    email         VARCHAR(150) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    sub_city      VARCHAR(100),
    kebele        VARCHAR(50),
    address       TEXT,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_phone  ON users(phone_number);
CREATE INDEX idx_users_role   ON users(role);
CREATE INDEX idx_users_active ON users(is_active);
```

---

## Part 11 — Verify everything works

### Step 1: Download dependencies

```bash
cd backend
mvn dependency:resolve
```

You will see Maven downloading jars on first run.
Subsequent runs use the local cache — fast.

### Step 2: Compile

```bash
mvn compile
```

Expected: `BUILD SUCCESS`
If you see errors here, they are Java syntax errors.

### Step 3: Create the database

```bash
psql -U postgres -c "CREATE DATABASE wastecollector;"
psql -U postgres -c "CREATE USER wastecollector_user WITH ENCRYPTED PASSWORD 'your_password';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE wastecollector TO wastecollector_user;"
```

### Step 4: Run the application

```bash
# Load your env vars first, then run
export DATABASE_URL=jdbc:postgresql://localhost:5432/wastecollector
export DB_USERNAME=wastecollector_user
export DB_PASSWORD=your_password
export JWT_SECRET=your-64-character-secret-key-here

mvn spring-boot:run
```

Expected output:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...

INFO  - Starting WasteCollectorApiApplication
INFO  - Flyway Community Edition ... by Redgate
INFO  - Database: jdbc:postgresql://localhost:5432/wastecollector
INFO  - Successfully validated 1 migration (execution time 00:00.014s)
INFO  - Current version of schema "public": << Empty Schema >>
INFO  - Migrating schema "public" to version "1 - create users table"
INFO  - Successfully applied 1 migration
INFO  - Started WasteCollectorApiApplication in 4.123 seconds
```

### Step 5: Verify Swagger UI

Open: http://localhost:8080/swagger-ui.html

You will see the Swagger UI with no endpoints yet —
that is correct because you have not written any controllers.
The important thing is that it loads without errors.

### Step 6: Verify the database

```bash
psql -U wastecollector_user -d wastecollector -c "\dt"
```

Expected output:
```
               List of relations
 Schema |          Name           | Type  |       Owner
--------+-------------------------+-------+-------------------
 public | flyway_schema_history   | table | wastecollector_user
 public | users                   | table | wastecollector_user
```

Both tables exist. Flyway created `flyway_schema_history` to track migrations,
and your V1 migration created the `users` table.

---

## Part 12 — Maven reference card

Keep this. You will use these commands constantly:

```bash
# ── Daily development ──────────────────────────────────────────────
mvn spring-boot:run          # Start the application
mvn compile                  # Check for compilation errors
mvn test                     # Run all tests
mvn clean                    # Delete target/ folder

# ── Building ───────────────────────────────────────────────────────
mvn clean package            # Clean build — compile + test + build jar
mvn package -DskipTests      # Build jar, skip tests (use sparingly)

# ── Dependency management ──────────────────────────────────────────
mvn dependency:resolve       # Download all dependencies
mvn dependency:tree          # Show the full dependency tree
mvn dependency:analyze       # Find unused or undeclared dependencies
mvn versions:display-dependency-updates  # Show available updates

# ── Troubleshooting ────────────────────────────────────────────────
mvn clean install            # Full rebuild including local cache install
mvn -X spring-boot:run       # Run with debug logging (very verbose)
```

---

## What you now understand that Initializr users do not

| Topic | What you know |
|-------|--------------|
| `pom.xml` structure | Every section and why it exists |
| `<parent>` | How version management is inherited from Spring Boot |
| Dependency coordinates | groupId + artifactId + version + scope |
| Dependency scopes | When each scope is appropriate and why |
| Build lifecycle | compile → test → package → install → deploy |
| Maven plugins | What spring-boot-maven-plugin and compiler-plugin do |
| Folder structure | Why src/main/java and src/test/java exist separately |
| Flyway naming | V1__description.sql convention and why |
| application.yml | Every property and what it controls |

When you use Initializr for future projects, you will recognise every line
it generates. When a Maven error occurs, you will know exactly where to look.
That is the difference between using a tool and understanding it.

---

*Document version: 1.0*
*Created: May 2026*
*Project: Ethiopia Waste Collector System*
*Next: feature-01-authentication-backend.md*