---
inclusion: always
---

# Structure: where things live

<!-- Migrated 2026-09-24 from README.md (Architecture, Package Structure, Container Directory Layout)
     and a scan of `git ls-files` on main @ 22c734c (449 tracked files before this migration). -->

## Repository root

```
ImanuelJakartaWeb/
├── AGENTS.md                      # Primary agent instructions (Kiro + other AGENTS.md-aware tools)
├── .kiro/steering/                # Kiro steering docs (this folder)
├── .github/workflows/deploy.yml   # The ONLY workflow: manual deploy via workflow_dispatch (no PR CI)
├── .env.example                   # Env var NAMES + placeholder values; copy to .env (gitignored)
├── .gitignore
├── Containerfile                  # Multi-stage image build for ANY service (build arg SERVICE_NAME)
├── compose.yml                    # Podman Compose stack (postgres + 10 Spring services)
├── maven-settings.xml             # Maven mirror (Google Maven Central CDN), used by Containerfile
├── pom.xml                        # Maven parent POM (packaging pom, 11 modules)
├── LICENSE                        # MIT, (c) 2026 Jeremy Matthew M
├── README.md                      # Human docs: features, stack, API, local dev, deployment
├── config-repo/                   # Spring Cloud Config files (served by church-config-server)
│   ├── application.yml            #   shared by all services (datasource, JWT, audit URL, logging)
│   └── church-<service>.yml       #   per-service overrides (ports, routes, mail, flyway…), 10 files
├── database/                      # Manual PostgreSQL migrations for the shared schema
│   ├── V1__initial_schema.sql … V7__add_map_settings.sql
│   └── setup.sh                   # Legacy local bootstrap (creates church_db, runs V1 only; stale)
├── church-common/                 # Shared library jar (DTOs, exceptions, JWT, utils)
├── church-config-server/          # Spring Cloud Config Server                        :8888
├── church-api-gateway/            # Spring Cloud Gateway, single public entry point  :8080
├── church-auth-service/           # Authentication & authorization                    :8081
├── church-user-service/           # User CRUD, roles, permissions, profile, birthday  :8082
├── church-cms-service/            # CMS pages/blocks, news, announcements, service times, settings :8083
├── church-event-service/          # Event management                                  :8084
├── church-media-service/          # Sermons, livestreams, gallery                     :8085
├── church-interaction-service/    # Prayer requests, contact, newsletter, testimonies, volunteer :8086
├── church-audit-service/          # Cross-service audit log recording (own Flyway)    :8087
└── church-gateway-service/        # Thymeleaf frontend + admin dashboard (internal)   :8089
```

Ignored by git (`.gitignore`): `target/`, `.env`, `logs/`, `*.log`, `.idea/`, `*.iml`, `.vscode/`, `*.classpath`, `*.project`, `.settings/`, `*.class`, `*.ctxt`, `.mtj.tmp/`, `*.jar`, `*.war`, `*.nar`, `*.ear`, `*.zip`, `*.tar.gz`, `*.rar`, `hs_err_pid*`, `replay_pid*`. `.kiro/` is **not** ignored; steering is meant to be committed.

## Standard module layout

```
church-<name>/
├── pom.xml
└── src/
    ├── main/java/com/jeimandei/imanuelbytes/<pkg>/
    │   ├── <Name>Application.java        # e.g. ChurchCmsApplication
    │   ├── audit/AuditClientService.java  # fire-and-forget audit client (6 backend services)
    │   ├── config/                        # SecurityConfig, RestTemplateConfig, JpaConfig, …
    │   ├── controller/                    # @RestController, base path /api/<resource>
    │   ├── dto/                           # <Name>Dto, Create<Name>Request, Update<Name>Request
    │   ├── entity/                        # JPA entities + enums (e.g. ContentStatus)
    │   ├── mapper/                        # MapStruct <Name>Mapper (componentModel = "spring")
    │   ├── repository/                    # Spring Data JPA repositories
    │   └── service/ + service/impl/       # <Name>Service interface + <Name>ServiceImpl
    ├── main/resources/
    │   ├── application.yml                # ONLY spring.application.name + spring.config.import
    │   └── logback-spring.xml             # logs/<app>.log, daily rolling
    └── test/java/com/jeimandei/imanuelbytes/<pkg>/service/…Test.java   # JUnit 5 + Mockito
```

## Java packages (root `com.jeimandei.imanuelbytes`) and file counts

| Module | Package | Sub-packages (files) |
|---|---|---|
| church-common | `common` | `constant` (1: AppConstants), `dto` (3: ApiResponse, PageResponse, AuditLogRequest), `entity` (1: BaseEntity with createdAt/updatedAt), `exception` (6: ChurchPlatformException, ForbiddenException, GlobalExceptionHandler, ResourceNotFoundException, UnauthorizedException, ValidationException), `security` (2: JwtService, JwtAuthenticationFilter), `util` (2: SlugUtils, DateUtils) |
| church-auth-service | `auth` | audit (1), config (3), controller (1), dto (4), entity (3), repository (2), service (2), service/impl (1) |
| church-user-service | `user` | audit (1), config (4), controller (4: User, Role, Permission, PermissionCategory), dto (16), entity (5), mapper (1), otp (2), repository (4), service (3), service/impl (3) |
| church-cms-service | `cms` | audit (1), config (3), controller (6: Announcement, CmsPage, ContentBlock, NewsArticle, ServiceTime, SiteSetting), dto (13), entity (7), mapper (4), repository (6), service (6), service/impl (6) |
| church-event-service | `event` | audit (1), config (3), controller (1), dto (4), entity (2), mapper (1), repository (1), service (1), service/impl (1) |
| church-media-service | `media` | audit (1), config (3), controller (3: Gallery, Livestream, Sermon), dto (9), entity (3), mapper (3), repository (3), service (3), service/impl (3) |
| church-interaction-service | `interaction` | audit (1), config (3), controller (5: ContactMessage, Newsletter, PrayerRequest, Testimony, Volunteer), dto (12), entity (6), repository (5), service (5), service/impl (5), token (1) |
| church-audit-service | `audit` | config (2), controller (1), dto (2), entity (1), repository (1), service (1), service/impl (1); Flyway SQL in `src/main/resources/db/migration/V1__audit_schema.sql` |
| church-gateway-service | `gateway` | config (6: CsrfEagerInitInterceptor, RequestUriAdvice, RestTemplateConfig, SecurityConfig, ServiceUrlConfig, WebMvcConfig), controller (10 public incl. GlobalModelAttributes), controller/admin (17 `Admin*Controller`), dto (25), security (2: GatewayAuthenticationProvider, GatewayUserDetails), service (10 `*ClientService`), util (1: SecurityUtils) |
| church-api-gateway | `apigateway` | Application class only (routes live in `config-repo/church-api-gateway.yml`) |
| church-config-server | `configserver` | Application class only |

README's package summary: `common` (dto, entity, exception, security, constant, util) · `auth` (registration, login, JWT issuance) · `user` (user CRUD, roles, status, birthday, OTP password change) · `cms` (CMS pages, content blocks, news, announcements, service times, settings) · `event` (events with lifecycle draft → published → cancelled) · `media` (sermons, livestreams, gallery) · `interaction` (prayer requests, contact, newsletter, testimonies, volunteer) · `audit` (audit log recording and querying, port 8087) · `apigateway` (Spring Cloud Gateway entry point, port 8080) · `configserver` (Spring Cloud Config Server, port 8888) · `gateway` (Thymeleaf frontend: public site, calendar, admin panel, port 8089).

## Frontend (`church-gateway-service/src/main/resources/`)

```
templates/
├── error.html
├── fragments/                 # head.html (head(pageTitle)), navbar.html, footer.html, alerts.html
├── auth/                      # login, register, forgot-password, profile, 403
├── public/                    # 19 pages: home, about, services, calendar, events, event-detail, sermons,
│                              #   sermon-detail, livestream, news, news-detail, gallery, ministries,
│                              #   leadership, new-here, faq, giving, contact, prayer-request
└── admin/                     # 30 pages, EACH embedding its own copy of the admin sidebar:
    ├── dashboard.html
    ├── announcements/ cms/ events/ gallery/ livestreams/ news/ permissions/ roles/ sermons/ service-times/
    │                          #   → list.html + form.html
    ├── audit-logs/index.html  settings/index.html
    ├── contacts/list.html  newsletter/list.html  prayer-requests/list.html
    └── users/                 # list, create, edit, birthdays
static/
├── css/custom.css             # design tokens (:root vars) + component classes
├── js/custom.js               # auto-dismiss alerts, data-confirm deletes, sidebar active/toggle
├── favicon.ico, favicon-32.png, favicon-192.png
└── images/                    # event-placeholder.jpg, gallery-placeholder.jpg, hero-pattern.svg
```

## Server layout (production, from README + deploy.yml)

```
/home/ibytes/website/gmimimanueljakarta/     # DEPLOY_DIR, a git checkout of this repo
├── .env                     # environment variables (never committed; pushed by deploy-env, chmod 600)
├── compose.yml
├── Containerfile
├── config-repo/             # Spring Cloud Config files (rsynced by deploy-config)
├── database/                # SQL migration scripts (V1–V7)
└── logs/                    # bind-mounted service log files (church-auth-service.log, church-cms-service.log, …)
```

## Where to put new things

| You are adding… | Put it in… | Also required (see the checklists in `conventions.md`) |
|---|---|---|
| A backend REST endpoint | `church-<svc>/…/controller/<Name>Controller.java` (+ service interface/impl, dto, mapper, repository) | New path prefix → `config-repo/church-api-gateway.yml`; public GET → that service's `config/SecurityConfig.java` `permitAll()` |
| A gateway→backend call | `church-gateway-service/…/service/<X>ClientService.java` via `ServiceUrlConfig` | Deploy the backend before (or together with) the gateway |
| A public page | `church-gateway-service/…/controller/<X>PublicController.java` or an existing controller + `templates/public/<page>.html` | Add the path to gateway `SecurityConfig` `permitAll()`; navbar/footer link |
| An admin page | `church-gateway-service/…/controller/admin/Admin<X>Controller.java` + `templates/admin/<x>/list.html` + `form.html` | Sidebar link in **all 30** admin templates |
| A schema change / seed | `database/V<N>__<snake_case>.sql` (next N; latest is V7) | Bind-mount in `compose.yml` + step in `deploy.yml` run-migrations + README |
| An audit-service schema change | `church-audit-service/src/main/resources/db/migration/V<N>__*.sql` (Flyway, own history table) | none of the `database/` wiring |
| A site setting | seed via a new `database/V<N>__*.sql`; read via `siteSettings['key']` | Textarea list in `admin/settings/index.html` if long text; README settings table |
| Shared DTO / exception / util | `church-common/…/common/<dto|exception|util>/` | Rebuild dependents (`-am`) |
| Config value | `config-repo/<service>.yml` (or `application.yml` if shared) | `deploy-config` + restart affected services |
| An env var | `compose.yml` service `environment:` + `.env.example` (name + placeholder) + `ENV_FILE` secret | `deploy-env` then rebuild/restart |
