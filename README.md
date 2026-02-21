# PlayerApp — Backend

Spring Boot 3.5 REST API for the PlayerApp soccer/football networking platform.

## Tech Stack

- **Java 17** — Language
- **Spring Boot 3.5** — Web, Security, Data JPA, Validation
- **PostgreSQL 16** — Database (Docker)
- **Spring Security + JWT** — HS256 tokens, 24h expiry
- **MapStruct** — Entity/DTO mapping
- **Lombok** — Boilerplate reduction
- **SpringDoc OpenAPI** — Swagger UI documentation
- **Spring AI MCP** — AI-callable tool integration
- **Maven** — Build tool with Spring Java Format plugin

## Prerequisites

- **Java 17+**
- **Maven 3.8+** (or use the included `./mvnw` wrapper)
- **Docker & Docker Compose** (for PostgreSQL)

## Getting Started

```bash
# 1. Start PostgreSQL database
docker compose up -d

# 2. Run the application (http://localhost:8080)
./mvnw spring-boot:run

# 3. Build + run tests
./mvnw clean install

# 4. Run tests only
./mvnw test

# 5. Format code (Spring Java Format)
./mvnw spring-javaformat:apply
```

## Database

PostgreSQL runs via Docker Compose:

| Service    | Port  | Credentials               |
|------------|-------|---------------------------|
| PostgreSQL | 5432  | `salah` / `salah123`      |
| pgAdmin    | 5050  | `admin@playerapp.com` / `admin123` |

- **Database**: `playersdb`
- **DDL**: Auto-managed by Hibernate (`ddl-auto: update`)

## Project Structure

```
src/main/java/com/salah/mcpplayersservice/
├── controllers/
│   ├── AuthController.java          # Signup & login
│   ├── PlayerController.java        # Player profile CRUD + available players
│   ├── TeamController.java          # Team listing + logo upload/serving
│   ├── PublicationController.java   # Publication CRUD
│   └── MediaController.java         # Media file upload/download
├── models/
│   ├── User.java                    # User entity (implements UserDetails)
│   ├── Player.java                  # Player profile entity
│   ├── Team.java                    # Team entity with logoUrl
│   ├── Publication.java             # Team publication entity
│   ├── Media.java                   # Player media (gallery)
│   └── Role.java                    # PLAYER, TEAM_MANAGER enum
├── dto/
│   ├── request/                     # LoginRequest, SignupRequest, etc.
│   └── response/                    # PlayerResponseDto, TeamResponseDto, etc.
├── repository/                      # Spring Data JPA repositories
├── services/
│   ├── AuthService.java             # Signup (player/manager), login + JWT
│   ├── TeamService.java             # Team CRUD + MCP tools
│   └── PlayerService.java           # Player operations + MCP tools
├── mappers/                         # MapStruct mappers
├── security/
│   ├── SecurityConfig.java          # Spring Security filter chain, CORS
│   ├── JwtAuthenticationFilter.java # JWT request filter
│   ├── JwtUtil.java                 # Token generation/validation
│   └── UserDetailsServiceImpl.java  # User loading from DB
├── exceptions/                      # Custom exceptions + global handler
└── config/                          # App configuration, data seeders
```

## API Endpoints

### Authentication (Public)

| Method | Endpoint            | Description                        |
|--------|---------------------|------------------------------------|
| POST   | `/api/auth/signup`  | Register as Player or Team Manager |
| POST   | `/api/auth/login`   | Login, returns JWT + role          |

### Players (Authenticated)

| Method | Endpoint                | Description                         |
|--------|-------------------------|-------------------------------------|
| GET    | `/api/players/me`       | Get current user's player profile   |
| PUT    | `/api/players/me`       | Update current player profile       |
| GET    | `/api/players/available`| List players without a team         |

### Teams

| Method | Endpoint                  | Access        | Description                  |
|--------|---------------------------|---------------|------------------------------|
| GET    | `/api/teams`              | Authenticated | List all teams (dropdown)    |
| PUT    | `/api/teams/me/logo`      | Authenticated | Upload team logo (multipart) |
| GET    | `/api/teams/{id}/logo`    | Public        | Serve team logo image        |

### Publications (Authenticated)

| Method | Endpoint                    | Description                     |
|--------|-----------------------------|---------------------------------|
| GET    | `/api/publications`         | Get all publications            |
| GET    | `/api/publications/me`      | Get current team's publications |
| POST   | `/api/publications`         | Create a publication            |
| DELETE | `/api/publications/{id}`    | Delete a publication            |

### Media (Authenticated)

| Method | Endpoint                    | Description              |
|--------|-----------------------------|--------------------------|
| POST   | `/api/media`                | Upload media file        |
| GET    | `/api/media/me`             | Get current user's media |
| GET    | `/api/media/{id}/file`      | Serve media file (public)|
| DELETE | `/api/media/{id}`           | Delete media             |

## Entity Relationships

```
User ──(1:1)── Player ──(N:1)── Team
                  │                 │
                  │ (1:N)           │ (1:N)
                  ▼                 ▼
                Media          Publication
```

- **User** ↔ **Player**: One-to-one (every user has a player record)
- **Player** → **Team**: Many-to-one (optional for players, mandatory for managers)
- **Team** → **Publication**: One-to-many
- **Player** → **Media**: One-to-many

## Security

- JWT (HS256) with 24-hour expiry
- Stateless session management
- Public endpoints: `/api/auth/**`, `/api/media/*/file`, `/api/teams/*/logo`, Swagger UI
- All other endpoints require valid Bearer token
- CORS enabled for `http://localhost:4200`

## Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI spec**: http://localhost:8080/v3/api-docs

## File Uploads

Uploaded files are stored in the `uploads/` directory:

```
uploads/
├── logos/       # Team logo images
└── media/       # Player gallery files
```

Max file size: 300MB (configurable in `application.yml`).

## Code Style

- **Spring Java Format** — Enforced automatically in the Maven `validate` phase
- **MapStruct + Lombok** — Annotation processors configured in Maven compiler plugin
