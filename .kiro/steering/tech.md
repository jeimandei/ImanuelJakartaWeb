---
inclusion: always
---

# Tech: stack, versions, configuration, ports

<!-- Migrated 2026-09-24 from README.md (Tech Stack, Service Ports, Configuration, Email, Development Tips),
     pom.xml + module poms, maven-settings.xml, Containerfile, compose.yml, config-repo/*.yml,
     src/main/resources/application.yml + logback-spring.xml in every module, fragments/head.html,
     and .env.example (variable NAMES only). Build + test were executed on 2026-09-24 to verify. -->

## Stack and exact versions

| Layer | Technology / version | Where it is defined |
|---|---|---|
| Language | Java **21** (`java.version`, `maven.compiler.source/target` = 21). Verified here with OpenJDK 21.0.10 | `pom.xml` |
| Framework | Spring Boot **3.2.5** (`spring-boot-starter-parent`) | `pom.xml` |
| Cloud | Spring Cloud **2023.0.3** BOM: Spring Cloud Gateway (`church-api-gateway`), Spring Cloud Config Server, `native` profile (`church-config-server`), `spring-cloud-starter-config` in every client | `pom.xml`, module poms |
| Frontend | Spring MVC + Thymeleaf 3 (`spring-boot-starter-thymeleaf`) + `thymeleaf-extras-springsecurity6` + Bootstrap **5.3.2** | `church-gateway-service/pom.xml`, `templates/fragments/head.html` |
| Calendar | FullCalendar **6.1.15** (CDN, calendar page only) | `templates/public/calendar.html` |
| Icons | FontAwesome **6.5.0** (CDN) | `templates/fragments/head.html` |
| Security | Spring Security 6 + JWT via jjwt **0.12.5** (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`), HS256 | `pom.xml` |
| Database | PostgreSQL **15** (`docker.io/library/postgres:15-alpine`; README says 15+). JDBC driver `org.postgresql:postgresql` **42.7.3** | `compose.yml`, `pom.xml` |
| ORM | Spring Data JPA + Hibernate (`PostgreSQLDialect`), `ddl-auto: validate` | `config-repo/application.yml` |
| Mapping | MapStruct **1.5.5.Final** (annotation processor wired in parent `maven-compiler-plugin`) | `pom.xml` |
| DB migrations | Manual SQL scripts in `database/` for the shared schema. Flyway **10.11.0** (`flyway-core`, `flyway-database-postgresql`) **only** in `church-audit-service` | `database/`, `church-audit-service` |
| Mail | `spring-boot-starter-mail` (user + interaction services), Gmail SMTP `smtp.gmail.com:587`, STARTTLS required | `config-repo/church-user-service.yml`, `config-repo/church-interaction-service.yml` |
| Build | Maven **3.9+** multi-module (verified here with Maven 3.9.11) | `pom.xml` |
| Tests | JUnit 5 + Mockito (`spring-boot-starter-test`); `spring-security-test` in auth, event, interaction and user | module poms |
| Container | Podman + `podman compose`; multi-stage `Containerfile` | `Containerfile`, `compose.yml` |
| CI/CD | GitHub Actions, **manual `workflow_dispatch` only** (`.github/workflows/deploy.yml`). There is **no** build/test CI on push or PR | `.github/workflows/deploy.yml` |
| Not used | **No Lombok.** No Checkstyle / Spotless / PMD / SpotBugs / formatter / linter plugins, no `.editorconfig`, no npm or Node toolchain | — |

## Build system details

- Parent POM `com.jeimandei:church-platform:1.0.0-SNAPSHOT` (packaging `pom`, name "Church Platform - Parent") inherits `org.springframework.boot:spring-boot-starter-parent:3.2.5`.
- Modules in reactor order: `church-common`, `church-auth-service`, `church-user-service`, `church-cms-service`, `church-event-service`, `church-media-service`, `church-interaction-service`, `church-gateway-service`, `church-audit-service`, `church-api-gateway`, `church-config-server`.
- Properties: `java.version=21`, `maven.compiler.source=21`, `maven.compiler.target=21`, `project.build.sourceEncoding=UTF-8`, `spring-boot.version=3.2.5`, `jjwt.version=0.12.5`, `mapstruct.version=1.5.5.Final`, `postgresql.version=42.7.3`, `flyway.version=10.11.0`, `spring-cloud.version=2023.0.3`.
- `dependencyManagement`: `spring-cloud-dependencies` BOM (import), `church-common:${project.version}`, jjwt api/impl(runtime)/jackson(runtime), mapstruct + mapstruct-processor (provided), postgresql, flyway-core, flyway-database-postgresql.
- `pluginManagement`: `spring-boot-maven-plugin`; `maven-compiler-plugin` with `<source>21</source><target>21</target>` and `annotationProcessorPaths` → `mapstruct-processor` `${mapstruct.version}`.
- `maven-settings.xml`: mirror `google-central` ("Google Maven Central CDN") → `https://maven-central.storage-download.googleapis.com/maven2/`, `mirrorOf central`. The Containerfile always passes `-s maven-settings.xml`.

### Module dependencies (from each `pom.xml`)

| Module | Key dependencies |
|---|---|
| `church-common` (library jar, no boot plugin) | web, validation, data-jpa, security, jjwt, mapstruct |
| `church-config-server` | `spring-cloud-config-server`, actuator |
| `church-api-gateway` | `spring-cloud-starter-gateway`, actuator, config client |
| `church-auth-service` | web, actuator, security, data-jpa, validation, church-common, postgresql (runtime), jjwt, config client, spring-security-test |
| `church-user-service` | web, actuator, security, data-jpa, validation, church-common, postgresql (runtime), jjwt, mapstruct, **mail**, config client, spring-security-test |
| `church-cms-service` | web, actuator, security, data-jpa, validation, postgresql, jjwt, mapstruct, church-common, config client |
| `church-event-service` | web, actuator, security, data-jpa, validation, church-common, postgresql (runtime), jjwt, mapstruct, config client, spring-security-test |
| `church-media-service` | web, actuator, security, data-jpa, validation, church-common, postgresql, jjwt, mapstruct, config client |
| `church-interaction-service` | web, actuator, security, data-jpa, validation, **mail**, postgresql (runtime), jjwt, mapstruct, church-common, config client, spring-security-test |
| `church-audit-service` | web, actuator, data-jpa, validation, security, church-common, postgresql, **flyway-core + flyway-database-postgresql**, config client |
| `church-gateway-service` | web, **thymeleaf**, **thymeleaf-extras-springsecurity6**, security, validation, actuator, church-common, jjwt, config client |

## Architecture and ports

```
User → church-api-gateway (:8080)            ← the ONLY port published to the host ("8080:8080")
           ├── /api/**  → backend services (:8081–:8087)
           └── /**      → church-gateway-service (:8089, Thymeleaf frontend + /admin)
                              └── all backend calls go back through the gateway (:8080)
```

| Service | Port | Visibility | Role |
|---|---|---|---|
| `church-api-gateway` | 8080 | Public | Spring Cloud Gateway, single entry point |
| `church-auth-service` | 8081 | Internal | Registration, login, JWT issuance |
| `church-user-service` | 8082 | Internal | Users, roles, permissions, permission categories, profile, birthday, OTP password |
| `church-cms-service` | 8083 | Internal | CMS pages + content blocks, news, announcements, service times, site settings |
| `church-event-service` | 8084 | Internal | Events (draft → published → cancelled) |
| `church-media-service` | 8085 | Internal | Sermons, livestreams, gallery |
| `church-interaction-service` | 8086 | Internal | Prayer requests, contact, newsletter, testimonies, volunteer |
| `church-audit-service` | 8087 | Internal (README) | Cross-service audit log. ⚠ `/api/audit/**` **is** routed by the public gateway; see `security.md` |
| `church-gateway-service` | 8089 | Internal | Thymeleaf public site + admin dashboard |
| `church-config-server` | 8888 | Internal | Spring Cloud Config Server |
| `postgres` (`church-postgres`) | 5432 | Internal (no host port) | PostgreSQL 15, database `imanuelweb_db` |

### API gateway routes (`config-repo/church-api-gateway.yml`)

| Route id | Target (env var, default) | Path predicates |
|---|---|---|
| `auth-service` | `AUTH_SERVICE_URI` (`http://localhost:8081`) | `/api/auth/**` |
| `user-service` | `USER_SERVICE_URI` (`http://localhost:8082`) | `/api/users/**,/api/roles/**,/api/permissions/**,/api/permission-categories/**` |
| `cms-service` | `CMS_SERVICE_URI` (`http://localhost:8083`) | `/api/cms/**,/api/announcements/**,/api/news/**,/api/settings/**,/api/service-times/**` |
| `event-service` | `EVENT_SERVICE_URI` (`http://localhost:8084`) | `/api/events/**` |
| `media-service-sermons` | `MEDIA_SERVICE_URI` (`http://localhost:8085`) | `/api/sermons/**` |
| `media-service-livestreams` | `MEDIA_SERVICE_URI` | `/api/livestreams/**` |
| `media-service-gallery` | `MEDIA_SERVICE_URI` | `/api/gallery/**` |
| `interaction-service` | `INTERACTION_SERVICE_URI` (`http://localhost:8086`) | `/api/prayer-requests/**,/api/contact-messages/**,/api/newsletter/**,/api/testimonies/**,/api/volunteer/**` |
| `audit-service` | `AUDIT_SERVICE_URI` (`http://localhost:8087`) | `/api/audit/**` |
| `frontend` | `FRONTEND_URI` (`http://localhost:8089`) | `/**` (catch-all, must stay last) |

A new `/api/<prefix>` does not work until its prefix is added here (lesson from PR #85).

## Runtime configuration

### Spring Cloud Config (native, file-based)
- `church-config-server` (`src/main/resources/application.yml`): `server.port: 8888`, `spring.profiles.active: native`, `spring.cloud.config.server.native.search-locations: file:${CONFIG_REPO_PATH:../config-repo}`, resolved relative to the config-server working directory, so running from `church-config-server/` finds `../config-repo`. Actuator exposes `health,env,refresh`, and health shows details. Logging: `com.jeimandei: INFO`, `org.springframework.cloud.config: DEBUG`.
- Every other module's `src/main/resources/application.yml` contains only `spring.application.name: <module>` and `spring.config.import: "configserver:http://church-config-server:8888"`. `compose.yml` also sets `SPRING_CONFIG_IMPORT` to that value. The hostname `church-config-server` only resolves on the container network (see commands.md → local run).
- Config server startup: start `church-config-server` first. Every service fetches its config from it at startup and **fails** if it is unreachable.

### Shared config: `config-repo/application.yml` (applies to every client)
- `spring.jackson.serialization.write-dates-as-timestamps: false`
- `spring.datasource.url: ${DB_URL:jdbc:postgresql://localhost:5432/imanuelweb_db}`, `username: ${DB_USERNAME:church_app}`, `password: ${DB_PASSWORD:<dev default, see security.md>}`, `driver-class-name: org.postgresql.Driver`
- `spring.jpa.hibernate.ddl-auto: validate` (never change DDL in prod; use migration scripts), `show-sql: false`, `hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect`, `format_sql: true`
- `jwt.secret: ${JWT_SECRET:<committed fallback, see security.md>}`, `jwt.expiration: ${JWT_EXPIRATION:86400000}` (24 h in ms)
- `audit-service.url: ${AUDIT_SERVICE_URL:http://localhost:8087}`. Backend services post audit events directly to the audit service (internal call).
- `management.endpoints.web.exposure.include: health`; `management.endpoint.health.show-details: never`
- `logging.level.com.jeimandei: DEBUG`, `org.springframework.security: INFO`. This file is also used in production.

### Per-service overrides: `config-repo/<service>.yml`

| File | Settings |
|---|---|
| `church-api-gateway.yml` | `server.port: 8080`; the gateway routes above; actuator health show-details `always`; logging `com.jeimandei: INFO`, `org.springframework.cloud.gateway: INFO` |
| `church-gateway-service.yml` | `server.port: 8089`; `server.forward-headers-strategy: FRAMEWORK`; `server.error.whitelabel.enabled: false`; `spring.thymeleaf.prefix: classpath:/templates/`, `suffix: .html`, **`cache: false`** (also in production); `spring.web.resources.static-locations: classpath:/static/`; multipart `max-file-size`/`max-request-size` `10MB`; `services.{auth,user,cms,event,media,interaction,audit}-service.url: ${API_GATEWAY_URL:http://localhost:8080}`, so all backend calls route through the API gateway; logging `com.jeimandei: DEBUG`, `org.springframework.security: INFO`, `org.springframework.web.client: DEBUG` |
| `church-auth-service.yml` | `server.port: 8081`; `spring.data.jpa.repositories.bootstrap-mode: deferred` |
| `church-user-service.yml` | `server.port: 8082`; mail `smtp.gmail.com:587`, `username: ${MAIL_USERNAME:…}`, `password: ${MAIL_PASSWORD:…}`, `smtp.auth: true`, `starttls.enable: true`, `starttls.required: true`, `mail.debug: false` |
| `church-cms-service.yml` | `server.port: 8083` |
| `church-event-service.yml` | `server.port: 8084`; `bootstrap-mode: deferred` |
| `church-media-service.yml` | `server.port: 8085` |
| `church-interaction-service.yml` | `server.port: 8086`; **`app.public-url: https://gmimimanueljakarta.or.id`** (domain used in newsletter email links); `bootstrap-mode: deferred`; same Gmail SMTP block as user-service |
| `church-audit-service.yml` | `server.port: 8087`; `spring.flyway.enabled: true`, `locations: classpath:db/migration`, **`table: flyway_schema_history_audit`** (avoids checksum conflicts when sharing a database schema), `baseline-on-migrate: true`, `baseline-version: 0`; logging `com.jeimandei: INFO`, `org.springframework: WARN`, `org.hibernate: WARN` |

### Development toggles (README "Development Tips")
- Set `spring.jpa.hibernate.ddl-auto: update` in `config-repo/application.yml` on first run, then switch back to `validate`.
- `spring.thymeleaf.cache: false` in `config-repo/church-gateway-service.yml` gives live template reload. It is already `false` in the committed file.
- `logging.level.com.jeimandei: DEBUG` in `config-repo/application.yml` gives verbose logs. It is already `DEBUG` in the committed file.
- README: "To change config without restarting a service, update `config-repo/` and POST to `/actuator/refresh` on the target service (or use the `deploy-config` workflow)". ⚠ This does not match the current config: clients expose only `health`, and no `@RefreshScope` exists. Restart the affected services after a config change (see commands.md → deploy-config).

## Environment variables (names only; values live in `.env` / the `ENV_FILE` secret)

| Variable | Consumed by (per `compose.yml`) | Purpose |
|---|---|---|
| `DB_URL` | auth, user, cms, event, media, interaction, audit | JDBC URL. Database name is **`imanuelweb_db`** (compose `POSTGRES_DB`, deploy workflow, config default). `.env.example` still shows the stale name `church_db` |
| `DB_USERNAME` | postgres (`POSTGRES_USER`), all DB services, postgres healthcheck | DB user (conventionally `church_app`) |
| `DB_PASSWORD` | postgres (`POSTGRES_PASSWORD`), all DB services | DB password |
| `JWT_SECRET` | auth, user, cms, event, media, interaction, gateway-service | HS256 signing key (256-bit, base64) |
| `JWT_EXPIRATION` | same as `JWT_SECRET` | Token lifetime in ms (`86400000` = 24 h) |
| `AUDIT_SERVICE_URL` | auth, user, cms, event, media, interaction | Audit base URL (container: `http://church-audit-service:8087`) |
| `MAIL_USERNAME` | user, interaction | Gmail address to send from |
| `MAIL_PASSWORD` | user, interaction | Gmail **app password** (not the account password) |
| `API_GATEWAY_URL` | gateway-service | Base URL for all backend calls (container: `http://church-api-gateway:8080`) |
| `AUTH_SERVICE_URI`, `USER_SERVICE_URI`, `CMS_SERVICE_URI`, `EVENT_SERVICE_URI`, `MEDIA_SERVICE_URI`, `INTERACTION_SERVICE_URI`, `AUDIT_SERVICE_URI`, `FRONTEND_URI` | api-gateway | Route targets (container: `http://church-<name>:<port>`) |
| `CONFIG_REPO_PATH` | config-server | Config directory (container: `/config-repo`; local default `../config-repo`) |
| `SPRING_CONFIG_IMPORT` | every client (set inline in `compose.yml`, not in `.env`) | `configserver:http://church-config-server:8888` |
| `PUID` / `PGID` | every container (inline in `compose.yml`) | `1002` / `1002` |
| `POSTGRES_USER` | inside the postgres container; used by `run-migrations` (`psql -U $POSTGRES_USER`) | Mirrors `DB_USERNAME` |

GitHub Actions secrets (names): `SSH_KEYS` (private SSH key for the deploy server), `ENV_FILE` (full contents of the server `.env`).

## Containers

### `Containerfile` (one file builds every service; selected by build arg)
- Stage 1 `docker.io/library/maven:3.9-eclipse-temurin-21-alpine AS build`, `WORKDIR /workspace`. Copies the parent + every module `pom.xml` first for layer caching, then `maven-settings.xml`, then runs `mvn dependency:go-offline -B -s maven-settings.xml` with a BuildKit cache mount `--mount=type=cache,target=/root/.m2,id=maven-m2`. Copies the full source, then `ARG SERVICE_NAME` and `mvn package -pl ${SERVICE_NAME} -am -DskipTests -B -s maven-settings.xml` with the same cache mount.
- Stage 2 `docker.io/library/eclipse-temurin:21-jre-alpine`: `ARG SERVICE_NAME`, `ARG SERVICE_PORT=8080`, `WORKDIR /app`, non-root user/group `spring`, `/app/logs` owned by `spring`, copies `/workspace/${SERVICE_NAME}/target/*.jar` to `app.jar`, `EXPOSE ${SERVICE_PORT}`, `ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]`.

### `compose.yml` (project name `church-platform`)
- Network `public` (bridge). Despite the name, only `church-api-gateway` publishes a host port (`"8080:8080"`). Named volume `postgres-data`.
- Logging anchor: `driver: journald`, `tag: "{{.Name}}"`.
- Service defaults anchor: build `context: .`, `dockerfile: Containerfile`, `pull: true`; network `public`; `restart: always`; `security_opt: [label=disable]`; env `PUID: 1002`, `PGID: 1002`; resource limits `memory: 1g`, `cpus: '2'`.
- `postgres`: image `docker.io/library/postgres:15-alpine`, `container_name: church-postgres`, `POSTGRES_DB: imanuelweb_db`, `POSTGRES_USER: ${DB_USERNAME}`, `POSTGRES_PASSWORD: ${DB_PASSWORD}`; volumes `postgres-data:/var/lib/postgresql/data` plus **read-only bind mounts of `database/V1…V7__*.sql` into `/docker-entrypoint-initdb.d/`** (these run only when the data volume is first initialised); healthcheck `pg_isready -U ${DB_USERNAME} -d imanuelweb_db` (interval 10s, timeout 5s, retries 5, start_period 15s).
- Each Spring service: `container_name` = service name; build args `SERVICE_NAME` + `SERVICE_PORT`; volume `./logs:/app/logs`; healthcheck `wget -qO- http://localhost:<port>/actuator/health | grep -q UP` (interval 10s, timeout 5s, retries 5; start_period 30s for config-server and api-gateway, 40s for backends, 60s for gateway-service). `church-config-server` also mounts `./config-repo:/config-repo:ro`.
- Startup dependencies (all `condition: service_healthy`): postgres → config-server → auth, user, cms, event, media, interaction, audit → api-gateway (waits for all 7 backends) → gateway-service (waits for api-gateway).

## Logging (`src/main/resources/logback-spring.xml` in every module)
- Appenders: console + rolling file `logs/${spring.application.name}.log`, daily roll to `logs/<app>.%d{yyyy-MM-dd}.log.gz`, `maxHistory` 30, `totalSizeCap` 1GB.
- Pattern: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}:%L - %msg%n`.
- Spring profile `dev` → `com.jeimandei` DEBUG, profile `prod` → INFO. Framework noise: `org.springframework` WARN, `org.hibernate` WARN, `org.flywaydb` INFO. Root INFO.
- Variants: `church-api-gateway` adds `org.springframework.cloud.gateway` (DEBUG in dev / INFO in prod); `church-config-server` adds `org.springframework.cloud.config` DEBUG in dev; audit, api-gateway and config-server only differ in formatting and default `appName`.
- On the server, logs are bind-mounted to `/home/ibytes/website/gmimimanueljakarta/logs/church-*.log`.

## Frontend assets
- CDN only (no bundler): Bootstrap CSS `https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css`, Bootstrap JS bundle `https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js` (included at the bottom of every page), FontAwesome `https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css`, FullCalendar `https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js` (calendar page).
- Local static files in `church-gateway-service/src/main/resources/static/`: `css/custom.css` (design tokens + components), `js/custom.js` (behaviours), `favicon.ico`, `favicon-32.png`, `favicon-192.png`, `images/event-placeholder.jpg`, `images/gallery-placeholder.jpg`, `images/hero-pattern.svg`.
- Google Maps embeds use a plain iframe with no API key.
