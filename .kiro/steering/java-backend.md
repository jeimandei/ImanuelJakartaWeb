---
inclusion: fileMatch
fileMatchPattern: "**/*.java"
---

# Java conventions (all modules)

<!-- Migrated 2026-09-24 from reading representative sources on main @ 22c734c: cms ServiceTimeController /
     ServiceTimeServiceImpl / ServiceTime / ServiceTimeMapper / AuditClientService / SecurityConfig / JpaConfig /
     RestTemplateConfig, church-common ApiResponse / exceptions / utils, gateway GlobalModelAttributes /
     SecurityConfig / ServiceUrlConfig / AdminServiceTimeController / SecurityUtils, and NewsArticleServiceImplTest.
     Also the harness rule "write code that reads like the surrounding code". -->

**Match the surrounding code**: its comment density, naming and idioms. No Lombok and no formatter, so write it the way neighbouring files do.

## Application class
- `@SpringBootApplication(scanBasePackages = "com.jeimandei.imanuelbytes")` in every business service and the gateway-service, so `church-common` beans (`GlobalExceptionHandler`, `JwtService`, …) are picked up. Keep this on any new service.
- `config/JpaConfig.java` has `@EnableJpaAuditing`, required for `@CreatedDate` / `@LastModifiedDate`.
- `config/RestTemplateConfig.java`: `RestTemplateBuilder` with a 5 s connect timeout and a 10 s read timeout.

## Backend REST controller (`<svc>/controller/<Name>Controller.java`)
```java
@RestController
@RequestMapping("/api/service-times")
public class ServiceTimeController {

    private static final Logger log = LoggerFactory.getLogger(ServiceTimeController.class);

    private final ServiceTimeService serviceTimeService;

    public ServiceTimeController(ServiceTimeService serviceTimeService) {
        this.serviceTimeService = serviceTimeService;
    }

    @GetMapping("/active")                                       // public: also permitAll() in SecurityConfig
    public ResponseEntity<ApiResponse<List<ServiceTimeDto>>> getActiveServiceTimes() {
        return ResponseEntity.ok(ApiResponse.success(serviceTimeService.getActiveServiceTimes()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
    public ResponseEntity<ApiResponse<ServiceTimeDto>> createServiceTime(
            @Valid @RequestBody CreateServiceTimeRequest request) {
        log.debug("Creating service time '{}'", request.getName());
        ServiceTimeDto created = serviceTimeService.createServiceTime(request);
        log.info("Created service time id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service time created", created));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteServiceTime(@PathVariable Long id) { … return ResponseEntity.ok(ApiResponse.success("Service time deleted", null)); }
}
```
- Path base `/api/<plural-kebab-resource>`. Common sub-routes: `/active`, `/published`, `/upcoming`, `/latest`, `/all` (admin list), `/slug/{slug}`, `/{id}/publish`, `/{id}/activate`, `/{id}/status`, `/{id}/toggle`.
- Return `ResponseEntity<ApiResponse<T>>` with `ApiResponse.success(data)`, `ApiResponse.success("Message", data)` or `ApiResponse.success("Message")`. Errors come from exceptions, not hand-built bodies. One exception exists: `/api/newsletter/check` returns a plain boolean (PR #96).
- Paginated lists return `ApiResponse<PageResponse<T>>` via `PageResponse.from(springPage)`.
- Roles in `@PreAuthorize` are written without the `ROLE_` prefix (`hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')`). Some files put spaces after the commas; match the file.

## Security per backend service (`config/SecurityConfig.java`)
`@Configuration @EnableWebSecurity @EnableMethodSecurity`; CSRF disabled; `SessionCreationPolicy.STATELESS`; `.requestMatchers("/actuator/health").permitAll()`; explicit `permitAll()` for public GET paths (for example `HttpMethod.GET, "/api/service-times/active"`, `"/api/settings/{key}"`); `.anyRequest().authenticated()`; `addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)`. A new public endpoint needs a matcher here. `church-audit-service` is the exception: `anyRequest().permitAll()` (see `security.md` §4).

## Service layer
- Interface `service/<Name>Service.java` + implementation `service/impl/<Name>ServiceImpl.java` annotated `@Service @Transactional`; read methods add `@Transactional(readOnly = true)`.
- Constructor injection of repository, MapStruct mapper and `AuditClientService`, into `private final` fields.
- Not found: `.orElseThrow(() -> new ResourceNotFoundException("ServiceTime", "id", id))` gives "ServiceTime not found with id: 5" and HTTP 404.
- Streams: `.stream().map(mapper::toDto).toList()`.
- **Audit every write** after it succeeds:
  ```java
  try {
      auditClient.log(getCurrentActor(), getCurrentActorRole(), "CREATE_SERVICE_TIME", "ServiceTime",
              String.valueOf(saved.getId()), saved.getName());
  } catch (Exception e) {
      log.warn("Audit log failed for CREATE_SERVICE_TIME {}: {}", saved.getId(), e.getMessage());
  }
  ```
  Actions are `CREATE_<ENTITY>`, `UPDATE_<ENTITY>`, `DELETE_<ENTITY>` (UPPER_SNAKE). `getCurrentActor()` returns the authenticated username, or `"system"` for anonymous. `getCurrentActorRole()` returns the first authority, or `"UNKNOWN"`. These are private helpers copied per service impl.
- Delete implementations now load the entity with `findById(id)` (so the audit entry can carry its name) before deleting. Unit tests must stub `findById`, not `existsById` (9 tests are red because of this drift; see commands.md §4).

## Per-service audit client (`<svc>/audit/AuditClientService.java`)
Fire-and-forget `RestTemplate.postForEntity(auditServiceUrl + "/api/audit", body, Void.class)` with a JSON map of `actor` (default `"system"`), `actorRole` (default `"UNKNOWN"`), `action`, `entityType`, `entityId`, `entityName`, `serviceName`. `@Value("${audit-service.url:http://localhost:8087}")`, and `getServiceName()` returns the literal service name (for example `"church-cms-service"`). All exceptions are caught and logged at `warn`. Audit must never break a business operation.

## Entities (`entity/`)
```java
@Entity
@Table(name = "service_times")
@EntityListeners(AuditingEntityListener.class)
public class ServiceTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false, length = 20)
    private String dayOfWeek;
    …
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ServiceTime() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    …
}
```
- Table names are snake_case plural; columns are snake_case via `@Column(name = …)`; `BIGSERIAL` ↔ `IDENTITY`.
- Types: `LocalDateTime` for timestamps, `LocalTime` for times of day, `LocalDate` for dates (birthday), enums for statuses (for example `ContentStatus`).
- **Every column must exist in the DB** (`ddl-auto: validate`). Add it in a `database/V<N>__*.sql` migration first (see `database-migrations.md`).
- `church-common` also offers `BaseEntity` (createdAt/updatedAt) for reuse.

## DTOs and mappers
- `dto/<Name>Dto` for responses, `dto/Create<Name>Request` / `Update<Name>Request` for input (with Bean Validation annotations), plain getters/setters.
- MapStruct:
  ```java
  @Mapper(componentModel = "spring")
  public interface ServiceTimeMapper {
      ServiceTimeDto toDto(ServiceTime serviceTime);

      @Mapping(target = "id", ignore = true)
      @Mapping(target = "createdAt", ignore = true)
      @Mapping(target = "updatedAt", ignore = true)
      ServiceTime toEntity(CreateServiceTimeRequest request);

      @Mapping(target = "id", ignore = true)
      @Mapping(target = "createdAt", ignore = true)
      @Mapping(target = "updatedAt", ignore = true)
      void updateEntity(@MappingTarget ServiceTime serviceTime, CreateServiceTimeRequest request);
  }
  ```
- The gateway-service has its **own** DTO copies in `gateway/dto/` (for example `gateway.dto.ServiceTimeDto`). When a backend DTO gains a field that templates need, update the gateway DTO too.

## church-common (shared)
- `ApiResponse<T>`: immutable envelope `{success, message, data, timestamp, errors}`, with `@JsonInclude(NON_NULL)` and static factories `success(data)`, `success(message, data)`, `success(message)`, `error(message)`, `error(message, errors)`.
- `PageResponse.from(Page<T>)`, `SlugUtils.toSlug(String)`, `DateUtils.formatDate/formatDateTime/isUpcoming`, `AppConstants` (`JWT_HEADER = "Authorization"`, `JWT_PREFIX = "Bearer "`, role constants `ROLE_SUPER_ADMIN|ADMIN|EDITOR|MEMBER|GUEST`).
- Exceptions: `ChurchPlatformException` (carries an `HttpStatus`) and its subclasses `ResourceNotFoundException` (404; constructors `(message)`, `(entityName, id)`, `(entityName, field, value)`), `ValidationException`, `UnauthorizedException`, `ForbiddenException`. `GlobalExceptionHandler` maps `ChurchPlatformException` → its status, `MethodArgumentNotValidException` → 400 with field errors, `NoResourceFoundException` → 404, and any other `Exception` → 500.
- `JwtService` + `JwtAuthenticationFilter` (HS256, `jwt.secret`, `jwt.expiration`).
- Changing `church-common` affects every module. Rebuild with `-am`, run the whole test suite, and redeploy every service that depends on the change.

## Gateway-service (Thymeleaf frontend) Java
- **Client services** (`gateway/service/<X>ClientService.java`) are the only place that talks to backends. They build URLs with `ServiceUrlConfig` (`serviceUrlConfig.cmsUrl("/api/settings/public")`, `authUrl`, `userUrl`, `eventUrl`, `mediaUrl`, `interactionUrl`, `auditUrl`), all resolving to `API_GATEWAY_URL`. The pattern:
  ```java
  public Map<String, String> getPublicSettings() {
      try {
          String url = serviceUrlConfig.cmsUrl("/api/settings/public");
          ResponseEntity<ApiResponse<Map<String, String>>> response = restTemplate.exchange(
                  url, HttpMethod.GET, null,
                  new ParameterizedTypeReference<ApiResponse<Map<String, String>>>() {});
          ApiResponse<Map<String, String>> body = response.getBody();
          return (body != null && body.getData() != null) ? body.getData() : Collections.emptyMap();
      } catch (RestClientException e) {
          log.error("Failed to fetch public settings: {}", e.getMessage());
          return Collections.emptyMap();
      }
  }
  ```
  Authenticated calls take a `String jwt` parameter and send `Authorization: Bearer <jwt>`. Build query strings with `UriComponentsBuilder` to avoid double encoding (PR #97).
- **JWT for backend calls:** `SecurityUtils.getJwt()` reads it from the logged-in `GatewayUserDetails`. `GatewayAuthenticationProvider` obtains it at form login via `authClientService.login(username, password)`.
- **Global model attributes:** `controller/GlobalModelAttributes` (`@ControllerAdvice`) adds `footerServiceTimes` and `siteSettings` to every model, each in `try/catch` → `log.warn` → empty collection. `config/RequestUriAdvice` adds `requestURI`.
- **Public controllers** (`controller/*PublicController`, `HomeController`, `AuthController`, `InteractionController`): `@Controller`, return `"public/<page>"`, wrap client calls in `try/catch` → `log.error`, and still render with empty data.
- **Admin controllers** (`controller/admin/Admin<X>Controller`):
  ```java
  @Controller
  @RequestMapping("/admin/service-times")
  @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')")
  public class AdminServiceTimeController { … }
  ```
  Routes: `GET {"", "/"}` → `admin/<x>/list`; `GET /new` → `admin/<x>/form` with `isNew=true`; `POST /create`; `GET /{id}/edit` → form with `isNew=false`; `POST /{id}/edit`; `POST /{id}/delete` with method-level `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")`. Every POST: `String jwt = SecurityUtils.getJwt();`, `try { client.call(…, jwt); redirectAttributes.addFlashAttribute("successMessage", "… successfully."); } catch (Exception e) { log.error(…); redirectAttributes.addFlashAttribute("errorMessage", "Failed to …: " + e.getMessage()); }`, then `return "redirect:/admin/<x>";`. Form bodies are often built as `Map<String, Object>` from `HttpServletRequest` parameters.
- Gateway `SecurityConfig`: the public paths allowlist, form login `/login` (success → `/`, failure → `/login?error=true`), logout `/logout` → `/login?logout=true` (invalidate session, delete `JSESSIONID`), access denied `/403`, `maximumSessions(1)`, BCrypt. **Add every new public path to the allowlist.**
- `config/CsrfEagerInitInterceptor` forces CSRF token creation before rendering, to prevent "Cannot create a session after the response has been committed" on large pages. Keep it.

## Unit tests (`src/test/java/...`)
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("NewsArticleServiceImpl")
class NewsArticleServiceImplTest {

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @Mock
    private NewsArticleMapper newsArticleMapper;

    @InjectMocks
    private NewsArticleServiceImpl newsArticleService;

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------
    private NewsArticle buildArticle(Long id, String title, String slug, ContentStatus status) { … }

    // ---------------------------------------------------------------------------
    // createArticle
    // ---------------------------------------------------------------------------
    @Nested
    @DisplayName("createArticle")
    class CreateArticle {
        @Test
        @DisplayName("creates article with slug generated from title when request slug is blank")
        void createsArticle_withSlugFromTitle_whenRequestSlugIsBlank() { … }
    }
}
```
- JUnit 5 + Mockito only: no Spring context, no database. Static imports `org.junit.jupiter.api.Assertions.*` and `org.mockito.Mockito.*`; use `ArgumentCaptor` for saved entities.
- Mockito strict stubs are on (`MockitoExtension`), so unused stubs fail with `UnnecessaryStubbingException`. Stub exactly what the implementation calls.
- The test class is package-private, lives in the service **interface** package (`…cms.service`), and tests `…service.impl.<Name>ServiceImpl`.
- Gateway controller tests exist (`HomeControllerTest`, `InteractionControllerTest`). Keep them in sync with client method signatures.
