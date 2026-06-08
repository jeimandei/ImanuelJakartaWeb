# Imanuel Jakarta Church Platform

A complete church website platform built with Java 21, Spring Boot 3.2, Thymeleaf, and PostgreSQL using a microservices architecture.

## Architecture

```
ImanuelJakartaWeb/
├── church-common              # Shared library: DTOs, exceptions, JWT, utils
├── church-config-server       # Spring Cloud Config Server — centralised config  :8888
├── church-api-gateway         # Spring Cloud Gateway — single public entry point :8080
├── church-auth-service        # Authentication & authorization                   :8081
├── church-user-service        # User CRUD & profile management                   :8082
├── church-cms-service         # CMS pages, news, announcements, settings         :8083
├── church-event-service       # Event management                                 :8084
├── church-media-service       # Sermons, livestreams, gallery                    :8085
├── church-interaction-service # Prayer requests, contact, newsletter             :8086
├── church-audit-service       # Cross-service audit log recording                :8087
├── church-gateway-service     # Thymeleaf frontend + admin dashboard (internal)  :8089
├── database                   # PostgreSQL schema and setup script
└── pom.xml                    # Maven parent POM
```

**Traffic flow:**
```
User → church-api-gateway (:8080)
           ├── /api/**  → backend services (:8081–:8087)
           └── /**      → church-gateway-service (:8089, Thymeleaf frontend)
                              └── all backend calls go through the gateway
```

Admin functionality lives inside `church-gateway-service` at `/admin/**`. Admin views are Thymeleaf pages that call backend REST APIs through the API gateway.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Frontend | Spring MVC + Thymeleaf 3 + Bootstrap 5 |
| Security | Spring Security 6 + JWT (jjwt 0.12.x) |
| Database | PostgreSQL 15+ |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Mapping | MapStruct 1.5 |
| Build | Maven 3.9+ (multi-module) |

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 15+

## Quick Start

### 1. Database Setup

```bash
cd database
chmod +x setup.sh
./setup.sh
```

Or manually:

```sql
-- Run as postgres superuser
CREATE USER church_app WITH PASSWORD 'church_password';
CREATE DATABASE church_db OWNER church_app;
GRANT ALL PRIVILEGES ON DATABASE church_db TO church_app;

-- Then apply the schema:
psql -U church_app -d church_db -f V1__initial_schema.sql
```

### 2. Build All Services

```bash
mvn clean install -DskipTests
```

### 3. Start Services (in order)

```bash
# Terminal 1 — start FIRST: all other services fetch config from here on startup
cd church-config-server && mvn spring-boot:run

# Terminal 2
cd church-auth-service && mvn spring-boot:run

# Terminal 3 — start audit service early so write operations are captured
cd church-audit-service && mvn spring-boot:run

# Terminal 4
cd church-user-service && mvn spring-boot:run

# Terminal 5
cd church-cms-service && mvn spring-boot:run

# Terminal 6
cd church-event-service && mvn spring-boot:run

# Terminal 7
cd church-media-service && mvn spring-boot:run

# Terminal 8
cd church-interaction-service && mvn spring-boot:run

# Terminal 9 — Thymeleaf frontend (internal, port 8089)
cd church-gateway-service && mvn spring-boot:run

# Terminal 10 — start LAST: API gateway is the public entry point on :8080
cd church-api-gateway && mvn spring-boot:run
```

### 4. Access the Application

| URL | Description |
|---|---|
| http://localhost:8080 | Public church website |
| http://localhost:8080/admin | Admin dashboard |
| http://localhost:8080/login | Login page |

### Default Admin Credentials

```
Username: admin
Password: Admin@1234
```

## Service Ports

| Service | Port | Visibility |
|---|---|---|
| API Gateway (single entry point) | 8080 | Public |
| Config Server | 8888 | Internal |
| Auth Service | 8081 | Internal |
| User Service | 8082 | Internal |
| CMS Service | 8083 | Internal |
| Event Service | 8084 | Internal |
| Media Service | 8085 | Internal |
| Interaction Service | 8086 | Internal |
| Audit Service | 8087 | Internal |
| Frontend / Admin UI (Thymeleaf) | 8089 | Internal |

## REST API Reference

### Auth Service — port 8081

```
POST /api/auth/register       Register new user
POST /api/auth/login          Login (returns JWT)
POST /api/auth/logout         Logout
GET  /api/auth/me             Get current user info
```

### User Service — port 8082

```
GET    /api/users                  List users (paginated, searchable)
GET    /api/users/{id}             Get user by ID
GET    /api/users/username/{name}  Get user by username
POST   /api/users                  Create user (ADMIN)
PUT    /api/users/{id}             Update user
PUT    /api/users/{id}/status      Change status (ACTIVE / INACTIVE / LOCKED)
PUT    /api/users/{id}/roles       Assign roles
PUT    /api/users/{id}/password    Change password
DELETE /api/users/{id}             Soft delete (ADMIN)
```

### CMS Service — port 8083

```
GET    /api/cms/pages                          List pages (admin)
GET    /api/cms/pages/published                Published pages only
GET    /api/cms/pages/{id}                     Get page by ID
GET    /api/cms/pages/slug/{slug}/published    Get published page by slug
POST   /api/cms/pages                          Create page (EDITOR+)
PUT    /api/cms/pages/{id}                     Update page
PUT    /api/cms/pages/{id}/publish             Publish page
PUT    /api/cms/pages/{id}/unpublish           Unpublish page
DELETE /api/cms/pages/{id}                     Delete page (ADMIN)

GET    /api/announcements/active               Active announcements (public)
GET    /api/news                               Published articles (public, paginated)
GET    /api/news/slug/{slug}                   Article by slug (public)
GET    /api/settings/{key}                     Site setting by key (public)
```

### Event Service — port 8084

```
GET  /api/events/upcoming      Upcoming events (public)
GET  /api/events/featured      Featured events (public)
GET  /api/events/past          Past events
GET  /api/events/slug/{slug}   Event by slug (public)
POST /api/events               Create event (EDITOR+)
PUT  /api/events/{id}          Update event
PUT  /api/events/{id}/publish  Publish event
PUT  /api/events/{id}/cancel   Cancel event (ADMIN)
PUT  /api/events/{id}/featured Toggle featured flag
DELETE /api/events/{id}        Delete event (ADMIN)
```

### Media Service — port 8085

```
GET    /api/sermons             Sermon list (public, paginated)
GET    /api/sermons/latest      Latest sermons (public)
GET    /api/sermons/{id}        Sermon detail
POST   /api/sermons             Create sermon (EDITOR+)
PUT    /api/sermons/{id}        Update sermon
DELETE /api/sermons/{id}        Delete sermon (ADMIN)

GET    /api/livestreams/active  Active livestream (public)
POST   /api/livestreams         Create livestream
PUT    /api/livestreams/{id}/activate  Set as active (deactivates all others)
DELETE /api/livestreams/{id}    Delete livestream (ADMIN)

GET    /api/gallery             Gallery items (public)
GET    /api/gallery/albums      Album names (public)
POST   /api/gallery             Add gallery item (ADMIN)
DELETE /api/gallery/{id}        Delete gallery item (ADMIN)
```

### Interaction Service — port 8086

```
POST /api/prayer-requests              Submit prayer request (public)
GET  /api/prayer-requests              List all requests (ADMIN/EDITOR)
PUT  /api/prayer-requests/{id}/status  Update status

POST /api/contact-messages             Submit contact form (public)
GET  /api/contact-messages             List all messages (ADMIN)
PUT  /api/contact-messages/{id}/status Update status

POST /api/newsletter/subscribe         Subscribe (public)
POST /api/newsletter/unsubscribe       Unsubscribe (public)

POST /api/testimonies                  Submit testimony (public)
POST /api/volunteer                    Apply to volunteer (public)
```

### Audit Service — port 8087

Internal service. All 6 backend services post write operations here automatically.

```
POST /api/audit          Record an audit log entry
GET  /api/audit          Query audit logs (paginated, filterable)
  ?actor=                Filter by username who performed the action
  ?action=               Filter by action name (e.g. CREATE_EVENT, DELETE_USER)
  ?entityType=           Filter by entity type (e.g. Event, User, Sermon)
  ?serviceName=          Filter by originating service (e.g. church-event-service)
  ?from=                 Filter by start date-time (ISO-8601: 2026-06-01T00:00:00)
  ?to=                   Filter by end date-time (ISO-8601: 2026-06-08T23:59:59)
  ?page=                 Page number (0-based, default 0)
  ?size=                 Page size (default 50)
```

Results are always sorted by `createdAt DESC` regardless of the caller-supplied sort.

## Public Website Pages

| Path | Page |
|---|---|
| `/` | Home — hero, livestream, events, sermons, announcements |
| `/about` | About us — history, vision, statement of faith |
| `/services` | Service times and schedule |
| `/sermons` | Sermon archive with YouTube embed |
| `/sermons/{id}` | Sermon detail |
| `/events` | Upcoming & past events |
| `/events/{slug}` | Event detail |
| `/livestream` | Active YouTube livestream (iframe) |
| `/ministries` | Ministry departments |
| `/news` | News & blog articles |
| `/news/{slug}` | Article detail |
| `/gallery` | Photo gallery with album filter |
| `/prayer-request` | Prayer request form |
| `/contact` | Contact form & church details |
| `/giving` | Giving & donation information |
| `/new-here` | New visitor guide |
| `/faq` | Frequently asked questions |
| `/leadership` | Pastoral & leadership team |

## Admin Pages

All admin routes require `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`. Content routes also accept `ROLE_EDITOR`.

| Path | Description |
|---|---|
| `/admin` | Dashboard — stats, recent activity |
| `/admin/users` | List, activate/deactivate, delete users |
| `/admin/users/new` | Create user |
| `/admin/users/{id}/edit` | Edit user (name, email, phone, status) |
| `/admin/events` | Event list with publish / feature toggle |
| `/admin/events/create` | Create event |
| `/admin/sermons` | Sermon list |
| `/admin/sermons/create` | Add sermon with YouTube URL |
| `/admin/livestreams` | Manage livestreams, set active |
| `/admin/livestreams/create` | Add livestream with embed URL preview |
| `/admin/announcements` | Date-bounded announcements with priority |
| `/admin/news` | News article management |
| `/admin/gallery` | Gallery items and albums |
| `/admin/prayer-requests` | Prayer request queue (confidential flag respected) |
| `/admin/contacts` | Contact message inbox |
| `/admin/audit-logs` | Audit log viewer — filter by actor, action, entity, service, date range |
| `/admin/settings` | Site settings key-value editor |
| `/admin/cms` | CMS page management (publish/unpublish) |

## User Roles

| Role | Access |
|---|---|
| `ROLE_SUPER_ADMIN` | Full system access |
| `ROLE_ADMIN` | Manage all content, users, settings |
| `ROLE_EDITOR` | Manage CMS pages, events, sermons, news, announcements |
| `ROLE_MEMBER` | Authenticated member — profile, prayer requests |
| `ROLE_GUEST` | Public visitor — read-only public pages |

## Database

See `database/V1__initial_schema.sql` for the main PostgreSQL schema:

- **17 tables**: users, roles, user_roles, cms_pages, cms_content_blocks, events, sermons, livestreams, announcements, news_articles, gallery_items, prayer_requests, contact_messages, site_settings, newsletter_subscriptions, testimony_submissions, volunteer_applications
- **1 additional table** (`audit_logs`) created by `church-audit-service` via its own Flyway migration (`flyway_schema_history_audit` history table to avoid conflicts)
- Indexes on all commonly-queried columns
- CHECK constraints for all status enums
- Seed data: default roles, admin user (`admin` / `Admin@1234`), and sample site settings

## Package Structure

```
com.jeimandei.imanuelbytes
├── common
│   ├── dto         # ApiResponse, PageResponse, AuditLogRequest
│   ├── entity      # BaseEntity (createdAt, updatedAt)
│   ├── exception   # ResourceNotFoundException, ValidationException, GlobalExceptionHandler
│   ├── security    # JwtService, JwtAuthenticationFilter
│   ├── constant    # AppConstants
│   └── util        # SlugUtils, DateUtils
├── auth            # Registration, login, JWT issuance
├── user            # User CRUD, roles, status management
├── cms             # CMS pages, content blocks, news, announcements, settings
├── event           # Events with lifecycle (draft → published → cancelled)
├── media           # Sermons, livestreams, gallery
├── interaction     # Prayer requests, contact, newsletter, testimonies, volunteer
├── audit           # Audit log recording and querying (port 8087)
├── apigateway      # Spring Cloud Gateway entry point (port 8080)
├── configserver    # Spring Cloud Config Server (port 8888)
└── gateway         # Thymeleaf frontend — public site, admin panel, auth pages (port 8089)
```

## Configuration

Each service reads its own `src/main/resources/application.yml`. Shared settings to configure per environment:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/church_db
    username: church_app
    password: church_password
  jpa:
    hibernate:
      ddl-auto: validate   # use 'update' on first run

jwt:
  secret: <256-bit-base64-secret>
  expiration: 86400000     # 24 hours in ms

audit-service:
  url: http://localhost:8080   # all backend calls go through the API gateway

# church-api-gateway has no application-level config beyond application.yml routes
```

## Containerized Deployment

The platform ships with a `Containerfile` (single multi-stage build) and `compose.yml` for Podman Compose. Deployments are managed via the parameterised GitHub Actions workflow in `.github/workflows/deploy.yml`.

### Required GitHub Secrets

| Secret | Description |
|---|---|
| `SSH_KEYS` | Private SSH key for the deploy server |
| `SSH_KNOWN_HOSTS` | Output of `ssh-keyscan -p 1709 -H jeimandei.com` |
| `ENV_FILE` | Full contents of the server `.env` file |

### Workflow Actions

| Action | What it does |
|---|---|
| `full-deploy` | Clone (first run) or pull + build all services sequentially + start stack |
| `build-service` | Rebuild and restart specific service(s) by name |
| `restart-service` | Restart service(s) without rebuild |
| `restart-all` | Restart every container |
| `deploy-config` | Rsync `config-repo/` to server + trigger config refresh |
| `deploy-env` | Push `.env` content from `ENV_FILE` secret to server |

### First-Time Server Setup

```bash
# 1. Push your .env to the server via the deploy-env workflow action

# 2. Run full-deploy — it will clone the repo, create ./logs, build and start all containers
#    Trigger from: GitHub → Actions → Deploy — Church Platform → full-deploy, branch: main
```

### Container Directory Layout (on server)

```
/home/ibytes/website/gmimimanueljakarta/
├── .env                          # environment variables (never committed)
├── compose.yml
├── Containerfile
├── config-repo/                  # Spring Cloud Config files
├── database/                     # SQL schema (read-only mount)
└── logs/                         # all service log files (bind-mounted from containers)
    ├── church-config-server.log
    ├── church-auth-service.log
    └── ...
```

### Viewing Logs

```bash
# Tail a specific service
tail -f logs/church-auth-service.log

# Watch all services at once
tail -f logs/church-*.log

# Container stdout (journald)
journalctl -t church-auth-service -f
```

## Logging

Each service ships with a `logback-spring.xml` that writes rolling log files:

- **Pattern**: timestamp, thread, level, logger with class name and **line number**, message
- **Rotation**: daily, compressed to `.log.gz`, 30-day retention, 1 GB total size cap
- **Dev profile** (`spring.profiles.active=dev`): `com.jeimandei` at `DEBUG`
- **Prod profile**: `com.jeimandei` at `INFO`

**Local dev**: log files written to `logs/<service-name>.log` inside each service directory (excluded from git).

**Containerized**: log files written to `/app/logs/<service-name>.log` inside the container, bind-mounted to `./logs/` in the deploy directory on the host.

## Audit Logging

Every write operation across all 6 backend services is recorded to `church-audit-service`. Each service embeds an `AuditClientService` that posts audit events as fire-and-forget HTTP calls — if the audit service is down, business operations are never blocked.

Recorded fields per event: actor (username), actor role, action name, entity type, entity ID, entity name, originating service, IP address, timestamp.

The admin UI at `/admin/audit-logs` provides a searchable, filterable, paginated view of all audit events with color-coded action badges.

## YouTube Livestream

Add a livestream at `/admin/livestreams/create`. The embed URL must be in this exact format:

```
https://www.youtube.com/embed/VIDEO_ID
```

Only one livestream can be active at a time — activating one automatically deactivates all others. The active stream appears on the homepage and `/livestream`.

## Security

- Passwords hashed with BCrypt
- JWT signed with HS256, configurable expiration (default 24 h)
- Admin routes protected by Spring Security `@PreAuthorize`
- CSRF tokens on all HTML forms
- Confidential prayer requests visible to `ROLE_ADMIN` / `ROLE_EDITOR` only
- Account status (ACTIVE / INACTIVE / LOCKED) checked on every login
- Audit service is internal-only (no JWT required, CSRF disabled, not exposed via gateway)

## Running Tests

```bash
mvn test -fae
```

175 unit tests across all 9 modules — JUnit 5 + Mockito, no Spring context or database required.

| Module | Tests |
|---|---|
| church-common | 38 |
| church-auth-service | 10 |
| church-user-service | 14 |
| church-cms-service | 18 |
| church-event-service | 13 |
| church-media-service | 22 |
| church-interaction-service | 12 |
| church-audit-service | 21 |
| church-gateway-service | 27 |

## Development Tips

- Start `church-config-server` first — every service fetches its config from it at startup and will refuse to start if it's unreachable
- All shared config (datasource, JWT, logging) lives in `config-repo/application.yml`; per-service overrides in `config-repo/{service-name}.yml`
- To change config without restarting a service, update `config-repo/` and `POST /actuator/refresh` on the target service
- Start `church-audit-service` early (after config-server) so write operations are captured from the beginning
- Start `church-api-gateway` last — it is the public entry point and all other services must be up first
- `church-gateway-service` (Thymeleaf frontend) runs on `:8089` internally; all its backend calls route through the gateway on `:8080`
- Set `spring.jpa.hibernate.ddl-auto: update` in `config-repo/application.yml` on first run, then switch back to `validate`
- Set `spring.thymeleaf.cache: false` in `config-repo/church-gateway-service.yml` for live template reload during development
- Set `logging.level.com.jeimandei: DEBUG` in `config-repo/application.yml` for verbose service logs
- The audit service uses `flyway.table: flyway_schema_history_audit` to avoid Flyway checksum conflicts when multiple services share the same database
