# Imanuel Jakarta Church Platform

A complete church website platform built with Java 21, Spring Boot 3.2, Thymeleaf, and PostgreSQL using microservices architecture.

## Architecture Overview

```
church-platform/
├── church-common           # Shared library (DTOs, exceptions, JWT, utils)
├── church-auth-service     # Authentication & authorization        :8081
├── church-user-service     # User CRUD & profile management        :8082
├── church-cms-service      # CMS, pages, announcements, news        :8083
├── church-event-service    # Event management                       :8084
├── church-media-service    # Sermons, livestreams, gallery          :8085
├── church-interaction-service # Forms: prayer, contact, newsletter  :8086
├── church-gateway-service  # Main web frontend + admin UI          :8080
└── church-admin-service    # (Reserved — admin merged into gateway)
```

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
- Docker (optional)

## Quick Start

### 1. Database Setup

```bash
cd church-platform/database
chmod +x setup.sh
./setup.sh
```

Or manually:
```sql
-- Run as postgres superuser
CREATE USER church_app WITH PASSWORD 'church_password';
CREATE DATABASE church_db OWNER church_app;
GRANT ALL PRIVILEGES ON DATABASE church_db TO church_app;

-- Then run the schema:
psql -U church_app -d church_db -f V1__initial_schema.sql
```

### 2. Build All Services

```bash
cd church-platform
mvn clean install -DskipTests
```

### 3. Start Services (in order)

```bash
# Terminal 1 - Auth Service
cd church-auth-service
mvn spring-boot:run

# Terminal 2 - User Service  
cd church-user-service
mvn spring-boot:run

# Terminal 3 - CMS Service
cd church-cms-service
mvn spring-boot:run

# Terminal 4 - Event Service
cd church-event-service
mvn spring-boot:run

# Terminal 5 - Media Service
cd church-media-service
mvn spring-boot:run

# Terminal 6 - Interaction Service
cd church-interaction-service
mvn spring-boot:run

# Terminal 7 - Gateway (Main Frontend) - start LAST
cd church-gateway-service
mvn spring-boot:run
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

| Service | Port |
|---|---|
| Gateway (Frontend) | 8080 |
| Auth Service | 8081 |
| User Service | 8082 |
| CMS Service | 8083 |
| Event Service | 8084 |
| Media Service | 8085 |
| Interaction Service | 8086 |

## API Endpoints

### Auth Service (port 8081)
```
POST /api/auth/register    Register new user
POST /api/auth/login       Login (returns JWT)
POST /api/auth/logout      Logout
GET  /api/auth/me          Get current user info
```

### User Service (port 8082)
```
GET    /api/users           List users (admin)
GET    /api/users/{id}      Get user
POST   /api/users           Create user
PUT    /api/users/{id}      Update user
DELETE /api/users/{id}      Soft delete user
PUT    /api/users/{id}/status   Change status
PUT    /api/users/{id}/roles    Assign roles
PUT    /api/users/{id}/password Change password
```

### CMS Service (port 8083)
```
GET    /api/cms/pages              List pages
GET    /api/cms/pages/{id}         Get page
GET    /api/cms/pages/slug/{slug}/published  Get published page
POST   /api/cms/pages              Create page (EDITOR+)
PUT    /api/cms/pages/{id}/publish Publish page
GET    /api/announcements/active   Active announcements (public)
GET    /api/news                   Published articles (public)
GET    /api/settings/{key}         Get site setting (public)
```

### Event Service (port 8084)
```
GET  /api/events/upcoming  Upcoming events (public)
GET  /api/events/featured  Featured events (public)
GET  /api/events/slug/{s}  Event by slug (public)
POST /api/events           Create event (EDITOR+)
PUT  /api/events/{id}/publish
PUT  /api/events/{id}/featured
```

### Media Service (port 8085)
```
GET  /api/sermons          Sermon list (public)
GET  /api/sermons/latest   Latest sermons (public)
GET  /api/livestreams/active  Active livestream (public)
GET  /api/gallery          Gallery items (public)
GET  /api/gallery/albums   Album names (public)
```

### Interaction Service (port 8086)
```
POST /api/prayer-requests  Submit prayer request (public)
POST /api/contact-messages Submit contact form (public)
POST /api/newsletter/subscribe Newsletter signup (public)
POST /api/testimonies      Submit testimony (public)
POST /api/volunteer        Apply to volunteer (public)
```

## Public Website Pages

| Path | Description |
|---|---|
| / | Home page |
| /about | About us |
| /services | Service times |
| /sermons | Sermon archive |
| /sermons/{id} | Sermon detail |
| /events | Upcoming events |
| /events/{slug} | Event detail |
| /livestream | YouTube livestream |
| /ministries | Church ministries |
| /news | News & articles |
| /news/{slug} | Article detail |
| /gallery | Photo gallery |
| /prayer-request | Submit prayer request |
| /contact | Contact us |
| /giving | Giving information |
| /new-here | New visitor guide |
| /faq | FAQ |
| /leadership | Leadership team |

## Admin Pages

| Path | Description | Roles |
|---|---|---|
| /admin | Dashboard | ADMIN, EDITOR |
| /admin/users | User management | ADMIN |
| /admin/events | Event management | ADMIN, EDITOR |
| /admin/sermons | Sermon management | ADMIN, EDITOR |
| /admin/livestreams | Livestream management | ADMIN |
| /admin/announcements | Announcements | ADMIN, EDITOR |
| /admin/news | News articles | ADMIN, EDITOR |
| /admin/gallery | Gallery | ADMIN |
| /admin/prayer-requests | Prayer requests | ADMIN, EDITOR |
| /admin/contacts | Contact messages | ADMIN |
| /admin/settings | Site settings | ADMIN |
| /admin/cms | CMS pages | ADMIN, EDITOR |

## User Roles

| Role | Description |
|---|---|
| ROLE_SUPER_ADMIN | Full system access |
| ROLE_ADMIN | Manage CMS, events, media, users |
| ROLE_EDITOR | Manage content (CMS, sermons, events) |
| ROLE_MEMBER | Authenticated church member |
| ROLE_GUEST | Public visitor |

## Package Structure

```
com.jeimandei.imanuelbytes.*
├── common          # Shared library
│   ├── dto         # ApiResponse, PageResponse
│   ├── entity      # BaseEntity
│   ├── exception   # ChurchPlatformException, ResourceNotFoundException
│   ├── security    # JwtService, JwtAuthenticationFilter
│   ├── constant    # AppConstants
│   └── util        # SlugUtils, DateUtils
├── auth            # Auth service
├── user            # User service
├── cms             # CMS service
├── event           # Event service
├── media           # Media service
├── interaction     # Interaction service
└── gateway         # Gateway / frontend
```

## Configuration

Each service has its own `application.yml`. Key settings:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/church_db
    username: church_app
    password: church_password

jwt:
  secret: <your-256-bit-secret>
  expiration: 86400000  # 24 hours
```

## YouTube Livestream

Admins can add livestreams at `/admin/livestreams`. The YouTube embed URL must follow this format:
```
https://www.youtube.com/embed/VIDEO_ID
```

Only one livestream can be active at a time. The active livestream is displayed on the homepage and `/livestream` page.

## Security Notes

- Passwords are hashed with BCrypt
- JWT tokens expire in 24 hours (configurable)
- Admin routes require ROLE_ADMIN or ROLE_SUPER_ADMIN
- Confidential prayer requests are only visible to admin/editor roles
- CSRF protection is enabled on all web forms

## Database Schema

See `database/V1__initial_schema.sql` for the complete PostgreSQL schema with:
- 17 tables
- Indexes on all commonly-queried columns
- Seed data for roles, admin user, site settings, and sample content

## Development Notes

- Change `ddl-auto: validate` to `ddl-auto: update` during initial setup
- Set `spring.thymeleaf.cache: false` for template hot-reload
- Set `logging.level.com.jeimandei: DEBUG` for verbose logging
