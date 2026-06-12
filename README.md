# Imanuel Jakarta Church Platform

A full-featured church website platform built with Java 21, Spring Boot 3.2, Thymeleaf, and PostgreSQL using a microservices architecture. Deployed at [gmimimanueljakarta.or.id](https://gmimimanueljakarta.or.id).

---

## Features

### Public Website
- **Home page** — hero, active livestream embed, upcoming events, latest sermons, announcements
- **Service times** — dynamic schedule from database, church contact info wired to site settings
- **Interactive calendar** — FullCalendar 6 view of all published events with month/week/list modes
- **Events** — upcoming and past events, detail pages with registration; list and calendar toggle
- **Sermons** — paginated archive with YouTube embed, speaker and series metadata
- **News & blog** — published articles with slug-based URLs, newsletter email links to full articles
- **Livestream** — active YouTube livestream embed; when offline, shows service schedule from DB
- **Gallery** — photo gallery with album filtering
- **Contact form** — form submission with admin inbox; church details live from site settings
- **Prayer requests** — public submission form; confidential flag; admin queue with status tracking
- **Newsletter** — email subscription/unsubscription with confirmation emails linking to production domain
- **Giving page** — bank account and donation information from site settings
- **New Here / FAQ / Ministries / Leadership** — static informational pages

### Member Area
- **Profile** — view and edit name, phone, profile image, birthday; change password with OTP email verification
- **Password self-service** — 6-digit OTP sent to registered email, single-use, 5-minute expiry

### Admin Dashboard
- **User management** — list, create, edit, activate/deactivate, soft-delete; assign roles; reset password by email
- **Birthday list** — filterable by month; shows all members with a birthday set
- **Role & permission management** — create/edit roles with granular permission checkboxes grouped by category
- **Events** — full CRUD with draft → publish → cancel lifecycle; featured toggle
- **Sermons** — add with YouTube URL, speaker, series, date
- **Livestreams** — manage multiple; one-click activate/deactivate (only one active at a time)
- **Announcements** — date-bounded with priority; appear as a banner on homepage
- **News articles** — create, edit, publish, delete; newsletter send per article
- **Service times** — add/edit/reorder worship times; automatically appear in footer, service page, and livestream page
- **Gallery** — upload items, group by albums
- **CMS pages** — rich-content pages with publish/unpublish lifecycle
- **Site settings** — key-value editor for church name, tagline, address, phone, email, social links, bank/giving details; changes propagate to all public templates immediately
- **Prayer requests** — inbox with status management; confidential entries restricted by role
- **Contact inbox** — contact form submissions with status tracking
- **Newsletter subscribers** — view, export subscriber list
- **Audit logs** — searchable, filterable by actor, action, entity, service, date range

---

## Architecture

```
ImanuelJakartaWeb/
├── church-common              # Shared library: DTOs, exceptions, JWT, utils
├── church-config-server       # Spring Cloud Config Server — centralised config  :8888
├── church-api-gateway         # Spring Cloud Gateway — single public entry point :8080
├── church-auth-service        # Authentication & authorization                   :8081
├── church-user-service        # User CRUD, roles, profile, birthday              :8082
├── church-cms-service         # CMS pages, news, announcements, settings         :8083
├── church-event-service       # Event management                                 :8084
├── church-media-service       # Sermons, livestreams, gallery                    :8085
├── church-interaction-service # Prayer requests, contact, newsletter, volunteer  :8086
├── church-audit-service       # Cross-service audit log recording                :8087
├── church-gateway-service     # Thymeleaf frontend + admin dashboard (internal)  :8089
├── database/                  # PostgreSQL migration SQL scripts (V1–V6)
└── pom.xml                    # Maven parent POM
```

**Traffic flow:**
```
User → church-api-gateway (:8080)
           ├── /api/**  → backend services (:8081–:8087)
           └── /**      → church-gateway-service (:8089, Thymeleaf frontend)
                              └── all backend calls go back through the gateway
```

Admin functionality lives inside `church-gateway-service` at `/admin/**`. Admin views are Thymeleaf pages that call backend REST APIs through the API gateway.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Frontend | Spring MVC + Thymeleaf 3 + Bootstrap 5.3.2 |
| Calendar | FullCalendar 6.1.15 (CDN) |
| Icons | FontAwesome 6.5.0 (CDN) |
| Security | Spring Security 6 + JWT (jjwt 0.12.x) |
| Database | PostgreSQL 15+ |
| ORM | Spring Data JPA + Hibernate |
| Mapping | MapStruct 1.5 |
| Build | Maven 3.9+ (multi-module) |
| Container | Podman + Podman Compose |
| CI/CD | GitHub Actions (manual `workflow_dispatch`) |

---

## Service Ports

| Service | Port | Visibility |
|---|---|---|
| API Gateway (single entry point) | 8080 | Public |
| Auth Service | 8081 | Internal |
| User Service | 8082 | Internal |
| CMS Service | 8083 | Internal |
| Event Service | 8084 | Internal |
| Media Service | 8085 | Internal |
| Interaction Service | 8086 | Internal |
| Audit Service | 8087 | Internal |
| Config Server | 8888 | Internal |
| Frontend / Admin UI (Thymeleaf) | 8089 | Internal |

---

## Public Website Pages

| Path | Page |
|---|---|
| `/` | Home — hero, livestream, events, sermons, announcements |
| `/services` | Service times, church address, phone, email (from site settings) |
| `/calendar` | Interactive event calendar (FullCalendar 6, month/week/list views) |
| `/events` | Upcoming & past events with list/calendar toggle |
| `/events/{slug}` | Event detail |
| `/sermons` | Sermon archive with YouTube embed |
| `/sermons/{id}` | Sermon detail |
| `/livestream` | Active YouTube livestream; offline state shows service schedule |
| `/news` | News & blog articles |
| `/news/{slug}` | Article detail |
| `/gallery` | Photo gallery with album filter |
| `/about` | About us — history, vision, statement of faith |
| `/ministries` | Ministry departments |
| `/leadership` | Pastoral & leadership team |
| `/new-here` | New visitor guide |
| `/faq` | Frequently asked questions |
| `/giving` | Giving & donation information |
| `/contact` | Contact form & church details (from site settings) |
| `/prayer-request` | Prayer request form |
| `/profile` | Authenticated user profile — edit info, birthday, change password with OTP |

---

## Admin Pages

All admin routes require `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`. Content routes also accept `ROLE_EDITOR`.

| Path | Description |
|---|---|
| `/admin` | Dashboard — stats, recent activity |
| `/admin/users` | List, activate/deactivate, reset password, delete users |
| `/admin/users/new` | Create user |
| `/admin/users/{id}/edit` | Edit user (name, email, phone, birthday, status, roles) |
| `/admin/users/birthdays` | Member birthday list, filterable by month |
| `/admin/roles` | Role list — permissions grouped by category |
| `/admin/roles/new` | Create role with permission checkboxes |
| `/admin/roles/{id}/edit` | Edit role description and permissions |
| `/admin/permissions` | Permission catalog viewer (all permissions by category) |
| `/admin/events` | Event list with publish / feature toggle |
| `/admin/events/create` | Create event |
| `/admin/sermons` | Sermon list |
| `/admin/sermons/create` | Add sermon with YouTube URL |
| `/admin/livestreams` | Manage livestreams, set active |
| `/admin/livestreams/create` | Add livestream with embed URL preview |
| `/admin/announcements` | Date-bounded announcements with priority |
| `/admin/cms` | CMS page management (publish/unpublish) |
| `/admin/news` | News article management |
| `/admin/gallery` | Gallery items and albums |
| `/admin/service-times` | Manage worship service times and schedule |
| `/admin/prayer-requests` | Prayer request queue (confidential flag respected) |
| `/admin/contacts` | Contact message inbox |
| `/admin/newsletter` | Newsletter subscriber list |
| `/admin/audit-logs` | Audit log viewer — filter by actor, action, entity, service, date range |
| `/admin/settings` | Site settings key-value editor |

---

## User Roles

| Role | Access |
|---|---|
| `ROLE_SUPER_ADMIN` | Full system access — all permissions |
| `ROLE_ADMIN` | All permissions — manage content, users, roles, settings |
| `ROLE_EDITOR` | CMS CRUD, event create/edit, media view |
| `ROLE_PASTOR` | Prayer requests, testimonies, contact messages, volunteer view |
| `ROLE_MEDIA_MANAGER` | All media (sermons, livestreams, gallery), event view |
| `ROLE_EVENT_MANAGER` | All events, media view |
| `ROLE_MEMBER` | Authenticated member — profile, prayer requests (default role) |

Roles are fully manageable via `/admin/roles`. Each role has a set of granular permissions drawn from 8 categories: **USERS**, **CONTENT**, **EVENTS**, **MEDIA**, **PASTORAL**, **COMMUNITY**, **SETTINGS**, **AUDIT**.

---

## Site Settings

Site settings are stored as key-value pairs in the `site_settings` table and managed at `/admin/settings`. Changes take effect immediately — no restart required.

| Key | Used in |
|---|---|
| `church_name` | Page titles, footer, home hero, services page |
| `church_tagline` | Footer, home hero subtitle |
| `church_address` | Footer, services page, contact page |
| `church_phone` | Footer, services page, contact page |
| `church_email` | Footer, services page, contact page (mailto link) |
| `church_logo_url` | Logo image |
| `facebook_url` | Footer social icons, contact page |
| `instagram_url` | Footer social icons, contact page |
| `church_youtube_channel` | Footer social icons, contact page, livestream YouTube button |
| `default_livestream_url` | Fallback embed when no active livestream is set |
| `about_us_text` | About page content |
| `vision_statement` | Vision section |
| `mission_statement` | Mission section |
| `giving_bank_name` | Giving / donation page |
| `giving_account_name` | Giving / donation page |
| `giving_account_number` | Giving / donation page |
| `giving_branch` | Giving / donation page |

Long-text fields (`about_us_text`, `vision_statement`, `mission_statement`) render as textareas in the admin settings editor.

---

## Database Migrations

All migrations live in `database/` and are safe to re-run (`IF NOT EXISTS` / `ON CONFLICT DO NOTHING`).

| File | Description |
|---|---|
| `V1__initial_schema.sql` | 17 core tables, seed roles (MEMBER → SUPER_ADMIN), admin user, sample site settings |
| `V2__add_permissions.sql` | `permissions` + `role_permissions` tables, 28 permissions, 3 new roles (PASTOR, MEDIA_MANAGER, EVENT_MANAGER), role-permission assignments |
| `V3__extended_permissions.sql` | Granular permissions for every capability (USER_MANAGE_ROLES, LIVE_MANAGE, GALLERY_*, NEWSLETTER_*, etc.) |
| `V4__permission_categories.sql` | `permission_categories` lookup table, seeds all 8 category names |
| `V5__service_times.sql` | `service_times` table with day_of_week, start/end time, location, description, active flag, sort_order; SERVICE_TIME_* permissions |
| `V6__add_birthday_to_users.sql` | `birthday DATE` column on `users`; `USER_BIRTHDAY_VIEW` permission granted to ADMIN/SUPER_ADMIN |

**Fresh deployment:** migration files are bind-mounted into the postgres container and run automatically at init.

**Live database:** use the `run-migrations` GitHub Actions workflow action.

---

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
GET    /api/users                           List users (paginated, searchable)
GET    /api/users/{id}                      Get user by ID
GET    /api/users/username/{name}           Get user by username
GET    /api/users/birthdays?month={1-12}    Members with birthday in given month (ADMIN)
POST   /api/users                           Create user (ADMIN)
PUT    /api/users/{id}                      Update user (name, phone, birthday, profileImageUrl)
PUT    /api/users/{id}/status               Change status ACTIVE / INACTIVE / LOCKED (ADMIN)
PUT    /api/users/{id}/roles                Assign roles (ADMIN)
DELETE /api/users/{id}                      Soft delete (ADMIN)

PUT    /api/users/{id}/password             Change password (requires current password)
POST   /api/users/{id}/reset-password       Admin resets password — emails temp password (ADMIN)
POST   /api/users/{id}/request-password-otp Send 6-digit OTP to user's email
PUT    /api/users/{id}/change-password-otp  Change password verified by OTP

GET    /api/roles                           List all roles with permissions
GET    /api/roles/{id}                      Get role by ID
POST   /api/roles                           Create role (ADMIN)
PUT    /api/roles/{id}                      Update role (ADMIN)
DELETE /api/roles/{id}                      Delete role (ADMIN)

GET    /api/permissions                     List all permissions (ADMIN)
```

### CMS Service — port 8083

```
GET    /api/cms/pages                          List pages (admin)
GET    /api/cms/pages/published                Published pages only
GET    /api/cms/pages/{id}                     Get page by ID
GET    /api/cms/pages/slug/{slug}/published    Published page by slug (public)
POST   /api/cms/pages                          Create page (EDITOR+)
PUT    /api/cms/pages/{id}                     Update page
PUT    /api/cms/pages/{id}/publish             Publish
PUT    /api/cms/pages/{id}/unpublish           Unpublish
DELETE /api/cms/pages/{id}                     Delete (ADMIN)

GET    /api/announcements/active               Active announcements (public)

GET    /api/news                               Published articles (public, paginated)
GET    /api/news/slug/{slug}                   Article by slug (public)

GET    /api/service-times/active               Active service times (public)

GET    /api/settings/public                    All settings as key-value map (public)
GET    /api/settings/{key}                     Single setting by key (public)
PUT    /api/settings/{key}                     Update setting (ADMIN)
```

### Event Service — port 8084

```
GET  /api/events              All events (paginated)
GET  /api/events/upcoming     Upcoming events (public)
GET  /api/events/featured     Featured events (public)
GET  /api/events/past         Past events
GET  /api/events/slug/{slug}  Event by slug (public)
POST /api/events              Create event (EDITOR+)
PUT  /api/events/{id}         Update event
PUT  /api/events/{id}/publish Publish event
PUT  /api/events/{id}/cancel  Cancel event (ADMIN)
PUT  /api/events/{id}/featured Toggle featured flag
DELETE /api/events/{id}       Delete event (ADMIN)
```

### Media Service — port 8085

```
GET    /api/sermons             Sermon list (public, paginated)
GET    /api/sermons/latest      Latest sermons (public)
GET    /api/sermons/{id}        Sermon detail
POST   /api/sermons             Create sermon (EDITOR+)
PUT    /api/sermons/{id}        Update sermon
DELETE /api/sermons/{id}        Delete sermon (ADMIN)

GET    /api/livestreams/active          Active livestream (public)
POST   /api/livestreams                 Create livestream
PUT    /api/livestreams/{id}/activate   Set as active (deactivates all others)
DELETE /api/livestreams/{id}            Delete (ADMIN)

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

POST /api/newsletter/subscribe                        Subscribe (public)
GET  /api/newsletter/confirm-unsubscribe?token={tok}  Confirm unsubscription via email link
POST /api/newsletter/unsubscribe                      Unsubscribe (authenticated)

POST /api/testimonies   Submit testimony (public)
POST /api/volunteer     Apply to volunteer (public)
```

### Audit Service — port 8087

Internal service. All 6 backend services post write operations here automatically.

```
POST /api/audit     Record an audit log entry
GET  /api/audit     Query audit logs (paginated, filterable)
  ?actor=           Filter by username
  ?action=          Filter by action (e.g. CREATE_EVENT, DELETE_USER)
  ?entityType=      Filter by entity type (e.g. Event, User, Sermon)
  ?serviceName=     Filter by service (e.g. church-event-service)
  ?from=            Start date-time (ISO-8601: 2026-06-01T00:00:00)
  ?to=              End date-time (ISO-8601: 2026-06-08T23:59:59)
  ?page=            Page number (0-based, default 0)
  ?size=            Page size (default 50)
```

Results are always sorted by `createdAt DESC`.

---

## Configuration

Each service reads its own `application.yml`. Shared configuration lives in `config-repo/application.yml`; per-service overrides in `config-repo/{service-name}.yml`.

Key settings to configure per environment:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/imanuelweb_db
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate   # never change DDL in prod — use migration scripts

jwt:
  secret: <256-bit-base64-secret>
  expiration: 86400000     # 24 hours in ms

# church-interaction-service
app:
  public-url: https://gmimimanueljakarta.or.id   # domain used in newsletter email links
```

### Email (SMTP)

`church-user-service` and `church-interaction-service` both send email. Configure via environment variables:

| Variable | Description |
|---|---|
| `MAIL_USERNAME` | Gmail address to send from |
| `MAIL_PASSWORD` | Gmail app password (not your account password) |

Used for: admin password reset emails, OTP codes, and newsletter unsubscribe confirmation links.

---

## Local Development

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 15+

### 1. Database Setup

```bash
psql -U postgres -c "CREATE USER church_app WITH PASSWORD 'church_password';"
psql -U postgres -c "CREATE DATABASE imanuelweb_db OWNER church_app;"

# Apply all migrations in order
psql -U church_app -d imanuelweb_db -f database/V1__initial_schema.sql
psql -U church_app -d imanuelweb_db -f database/V2__add_permissions.sql
psql -U church_app -d imanuelweb_db -f database/V3__extended_permissions.sql
psql -U church_app -d imanuelweb_db -f database/V4__permission_categories.sql
psql -U church_app -d imanuelweb_db -f database/V5__service_times.sql
psql -U church_app -d imanuelweb_db -f database/V6__add_birthday_to_users.sql
```

### 2. Build All Services

```bash
mvn clean install -DskipTests
```

### 3. Start Services (in order)

```bash
# 1. Config server — MUST be first; all services fetch config from it at startup
cd church-config-server && mvn spring-boot:run

# 2. Auth + Audit early so logins and audit writes are captured
cd church-auth-service    && mvn spring-boot:run
cd church-audit-service   && mvn spring-boot:run

# 3. Business services
cd church-user-service        && mvn spring-boot:run
cd church-cms-service         && mvn spring-boot:run
cd church-event-service       && mvn spring-boot:run
cd church-media-service       && mvn spring-boot:run
cd church-interaction-service && mvn spring-boot:run

# 4. Thymeleaf frontend (internal, port 8089)
cd church-gateway-service && mvn spring-boot:run

# 5. API gateway — MUST be last; public entry point on :8080
cd church-api-gateway && mvn spring-boot:run
```

### 4. Access the Application

| URL | Description |
|---|---|
| http://localhost:8080 | Public church website |
| http://localhost:8080/calendar | Interactive event calendar |
| http://localhost:8080/admin | Admin dashboard |
| http://localhost:8080/login | Login page |

### Default Admin Credentials

```
Username: admin
Password: Admin@1234
```

---

## Production Deployment

The platform ships with a `Containerfile` (multi-stage build) and `compose.yml` for Podman Compose. Deployments are managed via the parameterised GitHub Actions workflow at `.github/workflows/deploy.yml`.

### Required GitHub Secrets

| Secret | Description |
|---|---|
| `SSH_KEYS` | Private SSH key for the deploy server |
| `ENV_FILE` | Full contents of the server `.env` file |

### Workflow Actions

| Action | Input | What it does |
|---|---|---|
| `full-deploy` | `branch` | Pull latest code, build all service images sequentially, start the full stack |
| `build-service` | `services`, `branch` | Pull code, rebuild and restart specific service(s) by name |
| `restart-service` | `services` | Restart service(s) without rebuild |
| `restart-all` | — | Restart every container |
| `deploy-config` | `branch` | Rsync `config-repo/` to server + trigger `/actuator/refresh` on config server |
| `deploy-env` | — | Push `.env` content from `ENV_FILE` secret to server |
| `run-migrations` | — | Run all pending SQL migration scripts on the live database via `podman exec` |

### First-Time Server Setup

```bash
# Step 1 — push .env to server
# Trigger: Actions → Deploy — Church Platform → deploy-env

# Step 2 — full deploy (clones repo, builds all images, starts stack)
# Trigger: Actions → Deploy — Church Platform → full-deploy, branch: main
```

### Deploying a Single Service After Code Changes

```bash
# Trigger: Actions → Deploy → build-service
# Services field: church-gateway-service
# Branch: main
```

### Running Database Migrations on a Live Server

**When migrations must be applied before new code runs** (e.g. adding a column that `ddl-auto: validate` will check), follow this order:

```
1. build-service  → church-cms-service church-gateway-service
                    (gets new code onto server without restarting auth/user yet)
2. run-migrations → applies pending V*.sql scripts
3. build-service  → church-auth-service church-user-service
                    (now safe to restart — validate passes with the new column)
```

Never use `full-deploy` for incremental schema changes — it builds and restarts all services before migrations can run.

### Container Directory Layout (on server)

```
/home/ibytes/website/gmimimanueljakarta/
├── .env                     # environment variables (never committed)
├── compose.yml
├── Containerfile
├── config-repo/             # Spring Cloud Config files
├── database/                # SQL migration scripts (V1–V6)
└── logs/                    # bind-mounted service log files
    ├── church-auth-service.log
    ├── church-cms-service.log
    └── ...
```

### Viewing Logs

```bash
# Tail a specific service
tail -f logs/church-gateway-service.log

# Watch all services at once
tail -f logs/church-*.log
```

---

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
├── user            # User CRUD, roles, status, birthday, OTP password change
├── cms             # CMS pages, content blocks, news, announcements, service times, settings
├── event           # Events with lifecycle (draft → published → cancelled)
├── media           # Sermons, livestreams, gallery
├── interaction     # Prayer requests, contact, newsletter, testimonies, volunteer
├── audit           # Audit log recording and querying (port 8087)
├── apigateway      # Spring Cloud Gateway entry point (port 8080)
├── configserver    # Spring Cloud Config Server (port 8888)
└── gateway         # Thymeleaf frontend — public site, calendar, admin panel (port 8089)
```

---

## Security

- Passwords hashed with BCrypt
- JWT signed with HS256, configurable expiration (default 24 h)
- Admin routes protected by Spring Security `@PreAuthorize`
- CSRF tokens on all HTML forms
- Confidential prayer requests visible to `ROLE_ADMIN` / `ROLE_SUPER_ADMIN` only
- Account status (ACTIVE / INACTIVE / LOCKED) checked on every login
- Audit service is internal-only (not exposed via gateway)
- Password change requires OTP email verification
- Admin password reset sends a secure 12-character temporary password by email

---

## Audit Logging

Every write operation across all 6 backend services is recorded to `church-audit-service` as fire-and-forget HTTP calls — if the audit service is down, business operations are never blocked.

Recorded fields: actor (username), actor role, action name, entity type, entity ID, entity name, originating service, IP address, timestamp.

The admin UI at `/admin/audit-logs` provides a searchable, filterable, paginated view with color-coded action badges.

---

## YouTube Livestream

Add a livestream at `/admin/livestreams/create`. The embed URL must be in this format:

```
https://www.youtube.com/embed/VIDEO_ID
```

Only one livestream can be active at a time — activating one automatically deactivates all others. The active stream appears on the homepage and `/livestream`.

---

## Newsletter

Members can subscribe via the footer or profile page. When an admin publishes a news article and clicks "Send Newsletter", all subscribers receive an email with an excerpt and a link to the full article.

The link uses `app.public-url` in `config-repo/church-interaction-service.yml` (set to `https://gmimimanueljakarta.or.id`) — not the internal service URL — so recipients always get the production domain regardless of server configuration.

To unsubscribe, members click the link in any newsletter email. A confirmation email is sent to verify the request, and clicking it completes the removal.

---

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

---

## Development Tips

- Start `church-config-server` first — every service fetches its config from it at startup and will fail if it is unreachable
- All shared config (datasource, JWT, logging) lives in `config-repo/application.yml`; per-service overrides in `config-repo/{service-name}.yml`
- To change config without restarting a service, update `config-repo/` and POST to `/actuator/refresh` on the target service (or use the `deploy-config` workflow)
- Start `church-audit-service` early so write operations are captured from the beginning
- Start `church-api-gateway` last — all other services must be up first
- `church-gateway-service` runs on `:8089` internally; all its backend calls route through the gateway on `:8080`
- Set `spring.jpa.hibernate.ddl-auto: update` in `config-repo/application.yml` on first run, then switch back to `validate`
- Set `spring.thymeleaf.cache: false` in `config-repo/church-gateway-service.yml` for live template reload during development
- Set `logging.level.com.jeimandei: DEBUG` in `config-repo/application.yml` for verbose service logs
- The audit service uses `flyway.table: flyway_schema_history_audit` to avoid checksum conflicts when sharing a database schema
