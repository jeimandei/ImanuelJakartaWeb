---
inclusion: fileMatch
fileMatchPattern: "church-gateway-service/src/main/resources/templates/**/*.html"
---

# Thymeleaf templates (church-gateway-service)

<!-- Migrated 2026-09-24 from the Claude Code sessions (settings wiring, SpEL fixes PR #103/#105, Google Maps
     PR #107/#109, navbar PR #110) and from reading the templates, fragments, custom.css, custom.js,
     GlobalModelAttributes, RequestUriAdvice and CsrfEagerInitInterceptor on main @ 22c734c. -->

## Globals available in every template

| Name | Type | Source | On failure |
|---|---|---|---|
| `siteSettings` | `Map<String,String>` | `GlobalModelAttributes` → `CmsClientService.getPublicSettings()` → `GET /api/settings/public` (no auth) | empty map, so every lookup needs a fallback |
| `footerServiceTimes` | `List<ServiceTimeDto>` (active, sorted) | `GlobalModelAttributes` → `CmsClientService.getActiveServiceTimes()` → `GET /api/service-times/active` | empty list |
| `requestURI` | `String` | `RequestUriAdvice` (`request.getRequestURI()`) | used for navbar active state, for example `th:classappend="${requestURI == '/' ? 'active' : ''}"` |
| `successMessage` / `errorMessage` / `infoMessage` | flash attributes | set by controllers via `RedirectAttributes.addFlashAttribute` | rendered by `fragments/alerts :: alerts` (auto-dismiss after 5 s) |
| `_csrf` | CSRF token | Spring Security; `CsrfEagerInitInterceptor` forces the token early so large pages don't fail with "Cannot create a session after the response has been committed". Don't remove that interceptor | — |

No controller changes are needed to use `siteSettings` or `footerServiceTimes`.

## Page skeletons

Public / auth page:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/head :: head('Page Title')}"></head>
<body>

<nav th:replace="~{fragments/navbar :: navbar}"></nav>
<div th:replace="~{fragments/alerts :: alerts}"></div>

<!-- Page Header -->
<section style="background: linear-gradient(135deg, var(--primary) 0%, var(--primary-light) 100%); color:white; padding:80px 0;">
    <div class="container text-center">
        <i class="fas fa-church fa-3x mb-3" style="opacity:0.8;"></i>
        <h1 class="display-5 fw-bold mb-2">Title</h1>
        <p class="lead mb-0" style="opacity:0.85;">Subtitle</p>
    </div>
</section>

<section class="py-5">
    <div class="container">
        <div class="section-header mb-5">
            <h2>Section</h2>
            <div class="divider"></div>
            <p>Section subtitle</p>
        </div>
        …
    </div>
</section>

<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script th:src="@{/js/custom.js}"></script>
</body>
</html>
```
Form-style pages (contact, auth) use `<div class="form-page-header">` instead of the gradient section. `head('Page Title')` renders `<title>` as `Page Title | <church_name>`, with church_name falling back to `Imanuel Jakarta Church`.

Admin page: `xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6"`, `head('<Thing> - Admin')`, no public navbar/footer. The layout is `<div class="d-flex">`, then `<nav class="admin-sidebar d-none d-md-flex flex-column py-3">` (header `<div class="brand"><i class="fas fa-church me-2"></i>Church Admin</div>`), then `<div class="admin-content flex-grow-1">` containing an `admin-topbar` (title with icon, `<span sec:authentication="name">`, logout POST form) and the content (with the `alerts` fragment), then the same two scripts.

### Admin sidebar: duplicated in all 30 admin templates
The sidebar is **copied inline** in every file under `templates/admin/`; it is not a fragment. Canonical order (17 links, FontAwesome icon in parentheses):
Dashboard `/admin` (`fa-tachometer-alt`) · Users `/admin/users` (`fa-users`) · Roles `/admin/roles` (`fa-user-shield`) · Permissions `/admin/permissions` (`fa-key`) · CMS Pages `/admin/cms` (`fa-file-alt`) · Events `/admin/events` (`fa-calendar-alt`) · Sermons `/admin/sermons` (`fa-microphone`) · Livestreams `/admin/livestreams` (`fa-broadcast-tower`) · Announcements `/admin/announcements` (`fa-bullhorn`) · Service Times `/admin/service-times` (`fa-clock`) · News `/admin/news` (`fa-newspaper`) · Gallery `/admin/gallery` (`fa-images`) · Prayer Requests `/admin/prayer-requests` (`fa-hands`) · Contacts `/admin/contacts` (`fa-envelope`) · Newsletter `/admin/newsletter` (`fa-mail-bulk`) · Audit Logs `/admin/audit-logs` (`fa-clipboard-list`) · Settings `/admin/settings` (`fa-cog`).
- Existing drift: a Birthdays link `/admin/users/birthdays` appears **only** in `dashboard.html`, `users/list.html`, `users/edit.html` and `users/birthdays.html` (18 links); the other 26 files have 17.
- Adding an admin page means adding its `<li class="nav-item"><a class="nav-link" th:href="@{/admin/<x>}"><i class="fas fa-…"></i>Label</a></li>` to **all 30** files, and adding `active` only in the new page's own copy. `custom.js` also highlights the active link by path prefix.

## SpEL / Thymeleaf rules (each one broke production once)

1. **Apostrophes in string literals:** double them. Write `'Building God''s Kingdom'`, never `\'` (that caused `EL1046E: Cannot find terminating ' for string`, PR #103).
2. **No utility objects inside lambdas:** `#temporals`, `#strings`, `#lists`, `#arrays` etc. are **not available** inside `.stream().map(x -> …)`. That caused `EL1042E: Problem parsing right operand` (PR #105). Iterate instead:
   ```html
   <th:block th:each="st, iterStat : ${footerServiceTimes}"><span th:text="${#temporals.format(st.startTime, 'HH:mm')}">08:00</span><span th:unless="${iterStat.last}"> &amp; </span></th:block>
   ```
3. **Settings always need a fallback:** `th:text="${siteSettings['church_name'] ?: 'Imanuel Jakarta Church'}"`. Wrap the Elvis expression in parentheses when concatenating: `th:text="'Welcome to ' + (${siteSettings['church_name'] ?: 'Imanuel Jakarta Church'})"`.
4. **Only reference DTO fields that exist**. A missing property fails at render time (PR #81 removed a non-existent `author` field).
5. **Guard lists** with `th:if="${list != null and !list.isEmpty()}"` and provide a `th:unless` empty state (for example "Service schedule coming soon").

## Formatting patterns

- Time: `${#temporals.format(st.startTime, 'HH:mm')}` + ` WIB`; a range is `start + ' – ' + end + ' WIB'` when `endTime != null`.
- Day: `${#strings.capitalize(#strings.toLowerCase(st.dayOfWeek))}` (`SUNDAY` → `Sunday`).
- Links from settings: `th:href="${siteSettings['facebook_url'] ?: '#'}"`; mailto: `th:href="'mailto:' + (${siteSettings['church_email'] ?: 'info@gmimimanueljakarta.or.id'})"`.
- YouTube channel button fallback: `'https://youtube.com/@imanueljakarta'`.
- App URLs use `@{/path}` (`th:href="@{/contact}"`, `th:src="@{/js/custom.js}"`).

## Forms

- Every POST form includes `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>` and uses `th:action="@{/…}" method="post"`.
- Bound forms use `th:object="${form}"`, `th:field="*{name}"`, `th:classappend="${#fields.hasErrors('name') ? 'is-invalid' : ''}"`, and `<div class="invalid-feedback" th:if="${#fields.hasErrors('name')}" th:errors="*{name}">`.
- Delete buttons live in a POST form with `data-confirm="Are you sure…?"`; `custom.js` shows a `confirm()` dialog (default text "Are you sure you want to delete this item?").
- Admin controllers redirect after POST (PRG) and set `successMessage` / `errorMessage` flash attributes.

## Site settings in templates

- Wired keys: `church_name`, `church_tagline`, `church_address`, `church_phone`, `church_email`, `facebook_url`, `instagram_url`, `church_youtube_channel`, `church_map_embed_url`, `church_map_link_url`. Seeded but **not yet rendered** on public pages: `church_logo_url`, `default_livestream_url`, `about_us_text`, `vision_statement`, `mission_statement`, `giving_bank_name`, `giving_account_name`, `giving_account_number`, `giving_branch`.
- Admin editor (`admin/settings/index.html`): shows the description as the label with the key in small monospace. The long-text keys (`about_us_text`, `vision_statement`, `mission_statement`) render as `<textarea rows="4">`, controlled by a `th:if` / `th:unless` pair that lists the keys. Update **both** conditions when adding a long-text key.

## Google Maps embed pattern (`/contact` 250 px, `/services` card)

```html
<div th:if="${siteSettings['church_map_embed_url'] != null and !siteSettings['church_map_embed_url'].isEmpty()}" …>
    <iframe th:src="${siteSettings['church_map_embed_url']}" width="100%" height="100%" style="border:0; display:block;"
            allowfullscreen loading="lazy" referrerpolicy="no-referrer-when-downgrade"></iframe>
</div>
<div th:unless="${siteSettings['church_map_embed_url'] != null and !siteSettings['church_map_embed_url'].isEmpty()}" …>
    <!-- fallback: icon + "Map not configured" + address -->
</div>
<a th:href="${siteSettings['church_map_link_url'] ?: 'https://maps.google.com'}" target="_blank" …>Open in Google Maps</a>
```
On `/services` ("Find Us"), the row uses `align-items-stretch`. The address card is `card p-4 h-100`, and the map card is `card h-100 overflow-hidden`: a header (`px-4 pt-4 pb-2`, "Interactive Map"), a `flex-grow-1` iframe container with `min-height:260px`, and a `border-top` footer holding the "Open in Google Maps" button. Match neighbouring cards like this for any new component.

## Navbar rules (`fragments/navbar.html`)

- Keep top-level items few. Group related pages in dropdowns (`Services ▾`, `Events ▾` with Upcoming Events + Calendar).
- **Never** combine `nav-link` and `btn` on one element. A button in the navbar is `<li class="nav-item d-flex align-items-center ms-1"><a class="btn btn-sm btn-gold px-3" …>Join Us</a></li>`.
- Auth-dependent items use `sec:authorize="!isAuthenticated()"` / `isAuthenticated()` / `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')`; logout is a POST form with CSRF.

## Design system (`static/css/custom.css`, 162 lines)

- Tokens: `--primary: #1a3a6b`, `--primary-dark: #0f2248`, `--primary-light: #2a5ab0`, `--accent: #d4af37` (gold), `--accent-dark: #b8961e`, `--light-bg: #f8f9fa`, `--text-muted: #6c757d`. FullCalendar is themed through `--fc-*` variables mapped to these.
- Component classes: `btn-gold`, `btn-outline-gold`, `section-header` (+ `.divider`), `service-time-card` (`.service-type`, `.service-time`), `ministry-icon`, `form-card`, `form-page-header`, `event-date-badge`, `livestream-container`, `no-livestream`, `stat-card`, `sermon-card`, `gallery-img`, `hero-section`, `announcement-banner`, `badge-status-active|inactive|locked`, `admin-sidebar`, `admin-sidebar-header`, `admin-content`, `admin-topbar`.
- Headings/icons commonly use inline `style="color:var(--primary);"` and accent icons `style="color:var(--accent);"`. Follow that local idiom.
- `static/js/custom.js`: auto-dismisses `.alert.auto-dismiss` after 5 s; `form[data-confirm]` delete confirmation; admin sidebar active link by path prefix (exact match for `/admin`); mobile admin sidebar toggle `#adminSidebarToggle`.
- Icons: FontAwesome 6.5 (`fas …`, `fab fa-youtube|fa-facebook-f|fa-instagram|fa-tiktok`).

## Known hardcoded content to fix when touching these files

- `public/new-here.html`: hardcoded "**GKJ** Imanuel Jakarta" (wrong name), address "Jl. Imanuel No. 1…", phone, and static service times (SUN 08:00, SUN 10:30, WED 19:00). They should come from `siteSettings` / `footerServiceTimes`.
- `fragments/footer.html`: the email fallback is `info@imanueljakarta.org`, while contact/services use `info@gmimimanueljakarta.or.id`. The copyright line "© 2024 GMIM Imanuel Jakarta…" is hardcoded, and the TikTok link is `href="#"` with no setting.
