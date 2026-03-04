# MovieMate Backend — Spring Boot REST API

> A full-featured RESTful backend for a movie ticket booking platform, built with Spring Boot 4, Spring Security (JWT), JPA/Hibernate, and PostgreSQL.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture & Core Concepts](#architecture--core-concepts)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
  - [User Endpoints](#user-endpoints)
  - [Movie Endpoints](#movie-endpoints)
  - [Order Endpoints](#order-endpoints)
- [Security](#security)
- [DTOs (Data Transfer Objects)](#dtos-data-transfer-objects)
- [Exception Handling](#exception-handling)
- [Configuration](#configuration)
- [Getting Started](#getting-started)

---

## Project Overview

**MovieMate Server** is the backend service for a movie ticket booking system. It exposes a versioned REST API (`/api/v1/`) that handles:

- User registration, authentication, and role-based access control
- Movie catalogue management (CRUD, seat updates, scheduling)
- Order lifecycle management (create, track, cancel, approve)

The system supports two roles: `ROLE_USER` (regular customers) and `ROLE_ADMIN` (staff/managers).

---

## Tech Stack

| Layer            | Technology                              |
| ---------------- | --------------------------------------- |
| Language         | Java 21                                 |
| Framework        | Spring Boot 4.0.2                       |
| Build Tool       | Gradle                                  |
| Database         | PostgreSQL (port 5433)                  |
| ORM              | Spring Data JPA / Hibernate             |
| Security         | Spring Security + JWT (jjwt 0.11.5)     |
| Password Hashing | BCrypt                                  |
| Boilerplate      | Lombok                                  |
| Migrations       | Flyway (configured, currently disabled) |
| Monitoring       | Spring Boot Actuator                    |
| Testing          | JUnit 5 / Spring Boot Test              |

---

## Project Structure

```
src/main/java/moviemate/server/
├── ServerApplication.java          # Entry point — bootstraps Spring context
│
├── controller/                     # HTTP layer — maps requests to service calls
│   ├── UserController.java
│   ├── MovieController.java
│   └── OrderController.java
│
├── service/                        # Business logic layer
│   ├── UserService.java
│   ├── MovieService.java
│   └── OrderService.java
│
├── repository/                     # Data access layer (Spring Data JPA)
│   ├── UserRepository.java
│   ├── MovieRepository.java
│   ├── OrderRepository.java
│   └── RoleRepository.java
│
├── model/                          # JPA entities (DB tables)
│   ├── User.java
│   ├── Movie.java
│   ├── Order.java
│   ├── OrderProduct.java
│   └── Role.java
│
├── dto/                            # Data Transfer Objects
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── ChangePasswordRequest.java
│   │   ├── ResetPasswordRequest.java
│   │   ├── CreateOrderRequest.java
│   │   └── UpdateOrderRequest.java
│   └── response/
│       ├── UserResponse.java
│       ├── MovieResponse.java
│       ├── OrderResponse.java
│       ├── LoginResponse.java
│       └── MessageResponse.java
│
├── security/                       # Spring Security configuration
│   ├── SecurityConfig.java         # Filter chain, CORS, session policy
│   ├── JwtAuthenticationFilter.java# Intercepts every request to validate JWT
│   ├── CustomUserDetailsService.java# Loads user from DB for Spring Security
│   └── UserDetailsImpl.java        # Wraps User entity into Spring's UserDetails
│
├── exception/                      # Global error handling
│   ├── GlobalExceptionHandler.java
│   ├── UserNotFoundException.java
│   └── MovieNotFoundException.java
│
└── utils/
    ├── HashUtil.java               # JWT generation, validation, claims extraction
    └── TokenUtil.java              # (Placeholder)
```

---

## Architecture & Core Concepts

### 1. Layered Architecture (MVC + Service)

The application follows a strict layered pattern:

```
HTTP Request
    ↓
Controller       — Parses request, calls service, returns ResponseEntity
    ↓
Service          — All business rules live here (validation, mapping, orchestration)
    ↓
Repository       — Communicates with DB via JPA; no SQL written manually
    ↓
Database (PostgreSQL)
```

Each layer only depends on the layer directly below it. Controllers never access repositories directly.

---

### 2. Spring Boot & Auto-Configuration

`@SpringBootApplication` on `ServerApplication` enables:

- **`@ComponentScan`** — auto-discovers all `@Component`, `@Service`, `@Repository`, `@Controller` beans
- **`@EnableAutoConfiguration`** — wires up Spring MVC, JPA, Security, etc. from classpath
- **`@Configuration`** — allows declaring `@Bean` methods

---

### 3. Spring Data JPA & Hibernate

Repositories extend `JpaRepository<Entity, ID>`, which provides:

- `save()`, `findById()`, `findAll()`, `deleteById()`, `existsById()` — out of the box
- **Derived Query Methods** — Spring generates SQL from method names:
  ```java
  Optional<User> findByEmail(String email);
  List<Order> findByBuyerId(Integer buyerId);
  boolean existsBySlug(String slug);
  void deleteBySlug(String slug);   // requires @Transactional
  ```

`ddl-auto: update` means Hibernate automatically creates/alters tables to match entity definitions on startup.

---

### 4. JPA Entity Relationships

```
User  ←──(ManyToMany)──→  Role          (via join table user_roles)
User  ←──(ManyToOne)───   Movie          (createdBy, updatedBy)
User  ←──(ManyToOne)───   Order          (buyer, approvedBy)
Order ←──(OneToMany)───→  OrderProduct   (cascade = ALL, orphanRemoval)
Movie ←──(ManyToOne)───   OrderProduct
```

`CascadeType.ALL` + `orphanRemoval = true` on `Order.products` means when an order is saved, all its `OrderProduct` rows are automatically persisted/deleted with it.

---

### 5. Spring Security — JWT Stateless Authentication

The application is **fully stateless** — no HTTP sessions. Authentication follows this flow:

```
1. POST /api/v1/users/login
       ↓
2. PasswordEncoder.matches() verifies BCrypt hash
       ↓
3. HashUtil.generateToken() creates a signed JWT
       ↓
4. Client stores JWT and sends it as:
   Authorization: Bearer <token>
       ↓
5. JwtAuthenticationFilter intercepts every request:
   a. Extracts token from Authorization header
   b. Parses email (subject) from JWT claims
   c. Loads UserDetailsImpl from DB
   d. Validates token (email match + not expired)
   e. Sets UsernamePasswordAuthenticationToken in SecurityContextHolder
       ↓
6. Spring Security proceeds; @PreAuthorize checks roles
```

**Key classes:**

| Class                      | Role                                                                                              |
| -------------------------- | ------------------------------------------------------------------------------------------------- |
| `SecurityConfig`           | Defines filter chain, session policy (STATELESS), public vs protected routes                      |
| `JwtAuthenticationFilter`  | `OncePerRequestFilter` — runs once per request before the auth filter                             |
| `HashUtil`                 | JJWT wrapper — signs with HMAC-SHA256, reads/validates claims                                     |
| `CustomUserDetailsService` | Implements `UserDetailsService.loadUserByUsername()` used by Spring                               |
| `UserDetailsImpl`          | Implements `UserDetails`; exposes `getId()` for use in controllers via `@AuthenticationPrincipal` |

---

### 6. Role-Based Access Control (RBAC)

Roles stored in a DB table (`roles`) with values `ROLE_USER` and `ROLE_ADMIN`.

Access rules are applied in two places:

**a) `SecurityConfig` — URL-level rules:**

```java
.requestMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll()
.requestMatchers(HttpMethod.GET, "/api/v1/movies/**").permitAll()
.requestMatchers("/api/v1/users/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
```

**b) `@PreAuthorize` on individual methods:**

```java
@PreAuthorize("hasRole('ADMIN')")       // Admin only
@PreAuthorize("isAuthenticated()")      // Any logged-in user
```

`@EnableMethodSecurity` on `SecurityConfig` activates the `@PreAuthorize` support.

---

### 7. JWT — JSON Web Token

`HashUtil` uses `jjwt` to produce tokens with:

- **Subject**: user email
- **Claims**: `userId`, `roles`
- **Algorithm**: HMAC-SHA256 (symmetric key from `application.yml`)
- **Expiry**: 1 hour (3 600 000 ms)

Tokens are validated by checking the signature (key match) and expiration date — no DB lookup needed.

---

### 8. DTO Pattern

Raw entity classes (`User`, `Movie`, etc.) are never serialized directly into API responses. Instead:

- **Request DTOs** (`LoginRequest`, `CreateOrderRequest`, …) carry only the fields the client should supply
- **Response DTOs** (`UserResponse`, `MovieResponse`, …) expose only the fields safe to return (passwords never leave the server)
- Service layer maps entities → response DTOs using private `toXxxResponse()` helper methods + Lombok `@Builder`

---

### 9. Lombok

Reduces boilerplate annotations:

| Annotation                                   | Generated code                                     |
| -------------------------------------------- | -------------------------------------------------- |
| `@Data`                                      | getters, setters, `equals`, `hashCode`, `toString` |
| `@Builder`                                   | builder pattern for object construction            |
| `@NoArgsConstructor` / `@AllArgsConstructor` | constructors                                       |
| `@RequiredArgsConstructor`                   | constructor for `final` fields (used for DI)       |
| `@Slf4j`                                     | injects `log` field (SLF4J logger)                 |

---

### 10. Global Exception Handling

`@ControllerAdvice` + `@ExceptionHandler` in `GlobalExceptionHandler` catches exceptions thrown anywhere in the service/controller layer and converts them to clean JSON responses:

```json
{ "message": "User not found with id: 42" }
```

Custom exceptions (`UserNotFoundException`, `MovieNotFoundException`) extend `RuntimeException` to carry meaningful messages.

---

### 11. Jackson — JSON Naming Strategy

`spring.jackson.property-naming-strategy: SNAKE_CASE` in `application.yml` means all JSON fields are automatically serialized/deserialized in `snake_case` (e.g., `isActive` ↔ `is_active`).

---

## Database Schema

```
┌──────────────┐        ┌──────────────┐
│    users     │        │    roles     │
│──────────────│        │──────────────│
│ id (PK)      │        │ id (PK)      │
│ name         │        │ name         │
│ email        │        └──────────────┘
│ password     │               ▲
│ image        │  ┌────────────┘
│ is_active    │  │  user_roles (join table)
│ is_email_    │  │  ┌──────────────────┐
│   verified   │  │  │ user_id (FK)     │
│ otp          │  │  │ role_id (FK)     │
│ created_at   │  │  └──────────────────┘
│ updated_at   │
└──────────────┘
       ▲
       │ buyer_id / created_by / updated_by / approved_by
       │
┌──────────────┐        ┌──────────────────┐
│   orders     │        │  order_products  │
│──────────────│        │──────────────────│
│ id (UUID PK) │◄───────│ order_id (FK)    │
│ buyer_id(FK) │        │ movie_id (FK)    │
│ name         │        │ quantity         │
│ email        │        │ price            │
│ total        │        │ amount           │
│ type (enum)  │        └──────────────────┘
│ status(enum) │               │
│ approved_by  │               ▼
│ created_at   │        ┌──────────────┐
│ updated_at   │        │    movies    │
└──────────────┘        │──────────────│
                        │ id (PK)      │
                        │ title        │
                        │ slug (unique)│
                        │ duration     │
                        │ synopsis     │
                        │ poster       │
                        │ rating       │
                        │ seats        │
                        │ price        │
                        │ release_date │
                        │ end_date     │
                        │ created_by   │
                        │ updated_by   │
                        │ created_at   │
                        │ updated_at   │
                        └──────────────┘
```

---

## API Reference

Base URL: `http://localhost:8000/api/v1`

### User Endpoints

| Method   | URL                      | Auth          | Description                  |
| -------- | ------------------------ | ------------- | ---------------------------- |
| `POST`   | `/users/register`        | Public        | Register a new user          |
| `POST`   | `/users/login`           | Public        | Login — returns JWT token    |
| `GET`    | `/users/profile`         | Authenticated | Get own profile              |
| `PUT`    | `/users/{id}/profile`    | Authenticated | Update own profile           |
| `PATCH`  | `/users/change-password` | Authenticated | Change own password          |
| `GET`    | `/users/all`             | Admin         | List all users               |
| `GET`    | `/users/{id}`            | Admin         | Get user by ID               |
| `DELETE` | `/users/{id}`            | Admin         | Delete user                  |
| `PATCH`  | `/users/reset-password`  | Admin         | Reset a user's password      |
| `PATCH`  | `/users/{id}/block`      | Admin         | Block/deactivate a user      |
| `PUT`    | `/users/{id}`            | Admin         | Full profile update by admin |

---

### Movie Endpoints

| Method   | URL                           | Auth   | Description         |
| -------- | ----------------------------- | ------ | ------------------- |
| `POST`   | `/movies/`                    | Admin  | Create a movie      |
| `GET`    | `/movies/`                    | Public | List all movies     |
| `GET`    | `/movies/{slug}`              | Public | Get movie by slug   |
| `PUT`    | `/movies/{slug}`              | Admin  | Full movie update   |
| `PATCH`  | `/movies/{slug}/seats`        | Admin  | Update seat count   |
| `PATCH`  | `/movies/{slug}/release-date` | Admin  | Update release date |
| `DELETE` | `/movies/{slug}`              | Admin  | Delete a movie      |

---

### Order Endpoints

| Method  | URL                   | Auth          | Description            |
| ------- | --------------------- | ------------- | ---------------------- |
| `POST`  | `/orders/`            | Authenticated | Create a new order     |
| `GET`   | `/orders/my-order`    | Authenticated | Get caller's orders    |
| `GET`   | `/orders/{id}`        | Authenticated | Get order by ID        |
| `PATCH` | `/orders/{id}/cancel` | Authenticated | Cancel a pending order |
| `PUT`   | `/orders/{id}`        | Authenticated | Update order (type)    |
| `GET`   | `/orders/`            | Admin         | List all orders        |
| `PATCH` | `/orders/{id}/status` | Admin         | Update order status    |

---

### Sample Request & Response

**Login `POST /api/v1/users/login`**

Request:

```json
{
  "email": "john@example.com",
  "password": "secret123"
}
```

Response `200 OK`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "User logged in successfully"
}
```

**Create Order `POST /api/v1/orders/`**

Request (with `Authorization: Bearer <token>`):

```json
{
  "buyerId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "type": "Online",
  "products": [{ "movieId": 3, "quantity": 2 }]
}
```

Response `201 Created`:

```json
{
  "id": "550e8400-e29b-41d4-a716",
  "buyer_id": 1,
  "buyer_name": "John Doe",
  "total": 25.00,
  "status": "pending",
  "products": [...]
}
```

---

## Security

### Public Routes (no token required)

- `POST /api/v1/users/register`
- `POST /api/v1/users/login`
- `GET /api/v1/movies/**`

### Sending the JWT

All protected endpoints require:

```
Authorization: Bearer <your_jwt_token>
```

### Order Status Flow

```
PENDING  →  COMPLETED
         →  FAILED
         →  CANCELLED  (by user — only from PENDING)
```

---

## DTOs (Data Transfer Objects)

| DTO                        | Direction | Purpose                      |
| -------------------------- | --------- | ---------------------------- |
| `LoginRequest`             | Request   | Email + password for login   |
| `RegisterRequest`          | Request   | Name, email, password        |
| `ChangePasswordRequest`    | Request   | Old + new password           |
| `ResetPasswordRequest`     | Request   | Admin resets user password   |
| `CreateOrderRequest`       | Request   | Buyer ID, product list, type |
| `UpdateOrderRequest`       | Request   | Change order type            |
| `UpdateOrderStatusRequest` | Request   | Admin changes status         |
| `UserResponse`             | Response  | User data without password   |
| `MovieResponse`            | Response  | Movie details                |
| `OrderResponse`            | Response  | Full order with line items   |
| `LoginResponse`            | Response  | JWT token + message          |
| `MessageResponse`          | Response  | Simple `{ message: "..." }`  |

---

## Exception Handling

| Exception                | HTTP Status | When thrown                                                 |
| ------------------------ | ----------- | ----------------------------------------------------------- |
| `UserNotFoundException`  | 404         | User not found by ID or email                               |
| `MovieNotFoundException` | 404         | Movie not found by slug                                     |
| `RuntimeException`       | 500         | Generic failures (invalid password, inactive account, etc.) |

All handled centrally in `GlobalExceptionHandler` — controllers stay clean.

---

## Configuration

`src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/moviemate
    username: postgres
    password: saral123
  jpa:
    hibernate:
      ddl-auto: update # auto-create/alter tables
    show-sql: true # logs all SQL queries

server:
  port: 8000

jwt:
  secret: G5k!2@9wQz$Tn6VbLpRmXe4HfYc3Kj8U
  expiration: 3600000 # 1 hour in milliseconds
```

> **Important:** Move the JWT secret and DB credentials to environment variables before deploying to production.

---

## Getting Started

### Prerequisites

- Java 21+
- PostgreSQL running on port `5433`
- Database named `moviemate` created

### 1. Clone and configure

```bash
git clone <repo-url>
cd server
```

Edit `src/main/resources/application.yml` with your DB credentials.

### 2. Seed the roles table

The application expects at least `ROLE_USER` and `ROLE_ADMIN` rows in the `roles` table before registering users:

```sql
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
```

### 3. Build and Run

```bash
./gradlew bootRun
```

Or build a JAR:

```bash
./gradlew bootJar
java -jar build/libs/server-0.0.1-SNAPSHOT.jar
```

The server starts at **http://localhost:8000**.

### 4. Run tests

```bash
./gradlew test
```

---

## Key Design Decisions

| Decision                     | Rationale                                                    |
| ---------------------------- | ------------------------------------------------------------ |
| Stateless JWT (no sessions)  | Scalable; works across multiple server instances             |
| BCrypt for passwords         | Industry-standard adaptive hashing; resistant to brute-force |
| UUID for Order IDs           | Avoids predictable/sequential IDs for orders                 |
| Slug for Movie lookups       | Human-readable, URL-safe identifiers instead of numeric IDs  |
| DTO mapping in Service layer | Keeps API contract independent from DB schema                |
| `@ControllerAdvice`          | Single place to control all error responses                  |
| `SNAKE_CASE` JSON strategy   | Consistent with most frontend/API conventions                |
