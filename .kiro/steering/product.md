---
inclusion: always
---

# Product: GMIM Imanuel Jakarta Church Platform

<!-- Migrated 2026-09-24 from README.md (intro, Features, Public Website Pages, Admin Pages, User Roles,
     Site Settings, YouTube Livestream, Newsletter, Audit Logging, Security) plus facts verified in
     source code and rules the maintainer stated during Claude Code sessions. -->

## What it is

A full-featured church website platform for **GMIM Imanuel Jakarta** (GMIM = Gereja Masehi Injili di Minahasa), a church congregation in Jakarta, Indonesia. It is built with Java 21, Spring Boot 3.2, Thymeleaf and PostgreSQL as a set of microservices.

- Production site: https://gmimimanueljakarta.or.id
- Repository: https://github.com/jeimandei/ImanuelJakartaWeb (maintainer: Jeremy Matthew M, GitHub `jeimandei`)
- License: MIT, Copyright (c) 2026 Jeremy Matthew M

## Naming and locale rules

- The church is **GMIM Imanuel Jakarta**. Never write "GKJ Imanuel Jakarta". That was a past bug, and `templates/public/new-here.html` still contains it (see Known issues in `AGENTS.md`).
- The displayed name comes from the `church_name` site setting, never from hardcoded text. The seed default is `Imanuel Jakarta Church`; production currently shows "GMIM Imanuel Jakarta Church".
- The official email domain is `gmimimanueljakarta.or.id` (for example `info@gmimimanueljakarta.or.id`).
- Show times in 24-hour `HH:mm` with the suffix ` WIB` (Western Indonesia Time), for example `08:00 WIB`.
- Days of week are stored in UPPERCASE (`SUNDAY`) and displayed capitalized (`Sunday`).
- The New Here page describes worship in both Indonesian and English/bilingual services.

## Who it is for

| Audience | What they do |
|---|---|
| Public visitors | Read service times, events, calendar, sermons and news; watch the livestream; send contact messages and prayer requests; subscribe to the newsletter; see giving information |
| Members (`ROLE_MEMBER`, the default role on registration) | Profile (name, phone, profile image, birthday), change password with an OTP email, newsletter subscription toggle on the profile page |
| Staff (`ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, `ROLE_EDITOR`, plus `ROLE_PASTOR`, `ROLE_MEDIA_MANAGER`, `ROLE_EVENT_MANAGER`) | Admin dashboard at `/admin/**`. As coded, the gateway lets only `ADMIN`, `SUPER_ADMIN` and `EDITOR` open `/admin/**` |

## Features (README, verbatim)

### Public Website
- **Home page**: hero, active livestream embed, upcoming events, latest sermons, announcements
- **Service times**: dynamic schedule from database, church contact info wired to site settings
- **Interactive calendar**: FullCalendar 6 view of all published events with month/week/list modes
- **Events**: upcoming and past events, detail pages with registration; list and calendar toggle
- **Sermons**: paginated archive with YouTube embed, speaker and series metadata
- **News & blog**: published articles with slug-based URLs, newsletter email links to full articles
- **Livestream**: active YouTube livestream embed; when offline, shows service schedule from DB
- **Gallery**: photo gallery with album filtering
- **Contact form**: form submission with admin inbox; church details live from site settings
- **Prayer requests**: public submission form; confidential flag; admin queue with status tracking
- **Newsletter**: email subscription/unsubscription with confirmation emails linking to production domain
- **Giving page**: bank account and donation information from site settings
- **New Here / FAQ / Ministries / Leadership**: static informational pages

### Member Area
- **Profile**: view and edit name, phone, profile image, birthday; change password with OTP email verification
- **Password self-service**: 6-digit OTP sent to registered email, single-use, 5-minute expiry

### Admin Dashboard
- **User management**: list, create, edit, activate/deactivate, soft-delete; assign roles; reset password by email
- **Birthday list**: filterable by month; shows all members with a birthday set
- **Role & permission management**: create/edit roles with granular permission checkboxes grouped by category
- **Events**: full CRUD with draft → publish → cancel lifecycle; featured toggle
- **Sermons**: add with YouTube URL, speaker, series, date
- **Livestreams**: manage multiple; one-click activate/deactivate (only one active at a time)
- **Announcements**: date-bounded with priority; appear as a banner on homepage
- **News articles**: create, edit, publish, delete; newsletter send per article
- **Service times**: add/edit/reorder worship times; automatically appear in footer, service page, and livestream page
- **Gallery**: upload items, group by albums
- **CMS pages**: rich-content pages with publish/unpublish lifecycle
- **Site settings**: key-value editor for church name, tagline, address, phone, email, social links, bank/giving details; changes propagate to all public templates immediately
- **Prayer requests**: inbox with status management; confidential entries restricted by role
- **Contact inbox**: contact form submissions with status tracking
- **Newsletter subscribers**: view, export subscriber list
- **Audit logs**: searchable, filterable by actor, action, entity, service, date range

## Public website pages

| Path | Page |
|---|---|
| `/` | Home: hero, livestream, events, sermons, announcements |
| `/services` | Service times, church address, phone, email (from site settings), and an embedded Google Map in the "Find Us" section |
| `/calendar` | Interactive event calendar (FullCalendar 6, month/week/list views); JSON feed at `/calendar/feed` |
| `/events` | Upcoming & past events with list/calendar toggle |
| `/events/{slug}` | Event detail |
| `/sermons` | Sermon archive with YouTube embed |
| `/sermons/{id}` | Sermon detail |
| `/livestream` | Active YouTube livestream; offline state shows service schedule from DB service times |
| `/news` | News & blog articles |
| `/news/{slug}` | Article detail |
| `/gallery` | Photo gallery with album filter |
| `/about` | About us: history, vision, statement of faith |
| `/ministries` | Ministry departments |
| `/leadership` | Pastoral & leadership team |
| `/new-here` | New visitor guide |
| `/faq` | Frequently asked questions |
| `/giving` | Giving & donation information |
| `/contact` | Contact form & church details (from site settings) and an embedded Google Map |
| `/prayer-request` | Prayer request form |
| `/login`, `/register`, `/forgot-password`, `/forgot-password/reset` | Authentication pages |
| `/profile` | Authenticated user profile: edit info, birthday, change password with OTP, newsletter toggle |

## Public navbar (`templates/fragments/navbar.html`)

Keep the navbar compact. It overflowed once, so Events and Calendar were merged into one dropdown (PR #110).

- Home · About · **Services ▾** (Service Times `/services`, Sermons `/sermons`, Livestream `/livestream`) · **Events ▾** (Upcoming Events `/events`, Calendar `/calendar`) · Ministries · News · Gallery · Contact · Give
- Right side, anonymous: Login, **Join Us** (`btn btn-sm btn-gold` inside `<li class="nav-item d-flex align-items-center ms-1">`)
- Right side, authenticated: a user dropdown with My Profile, Admin Panel (shown to `ADMIN`/`SUPER_ADMIN`/`EDITOR` only) and Logout (a POST form with CSRF)

## Admin pages

README says all admin routes require `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`, and content routes also accept `ROLE_EDITOR`. As coded, the gateway `SecurityConfig` allows `/admin/**` for `ADMIN`, `SUPER_ADMIN` and `EDITOR`, admin controllers add class-level `@PreAuthorize`, and deletes are limited to `ADMIN`/`SUPER_ADMIN`.

| Path | Description |
|---|---|
| `/admin` | Dashboard: stats, recent activity |
| `/admin/users` | List, activate/deactivate, reset password, delete users |
| `/admin/users/new` | Create user |
| `/admin/users/{id}/edit` | Edit user (name, email, phone, birthday, status, roles) |
| `/admin/users/birthdays` | Member birthday list, filterable by month |
| `/admin/roles` | Role list: permissions grouped by category |
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
| `/admin/audit-logs` | Audit log viewer: filter by actor, action, entity, service, date range |
| `/admin/settings` | Site settings key-value editor |

## User roles

| Role | Access |
|---|---|
| `ROLE_SUPER_ADMIN` | Full system access: all permissions |
| `ROLE_ADMIN` | All permissions: manage content, users, roles, settings |
| `ROLE_EDITOR` | CMS CRUD, event create/edit, media view |
| `ROLE_PASTOR` | Prayer requests, testimonies, contact messages, volunteer view |
| `ROLE_MEDIA_MANAGER` | All media (sermons, livestreams, gallery), event view |
| `ROLE_EVENT_MANAGER` | All events, media view |
| `ROLE_MEMBER` | Authenticated member: profile, prayer requests (default role) |

Roles are fully manageable via `/admin/roles`. Each role has a set of granular permissions drawn from 8 categories: **USERS**, **CONTENT**, **EVENTS**, **MEDIA**, **PASTORAL**, **COMMUNITY**, **SETTINGS**, **AUDIT**. `church-common` `AppConstants` also defines a `ROLE_GUEST` constant.

## Site settings

Site settings are key-value pairs in the `site_settings` table, managed at `/admin/settings`. Changes take effect immediately with no restart required. The gateway fetches all of them on every page render through `GET /api/settings/public`.

| Key | Seeded by | Used in (README) | Actually read by templates via `siteSettings[...]`? |
|---|---|---|---|
| `church_name` | V1 | Page titles, footer, home hero, services page | yes |
| `church_tagline` | V1 | Footer, home hero subtitle | yes |
| `church_address` | V1 | Footer, services page, contact page | yes |
| `church_phone` | V1 | Footer, services page, contact page | yes |
| `church_email` | V1 | Footer, services page, contact page (mailto link) | yes |
| `church_logo_url` | V1 | Logo image | **no** (not wired yet) |
| `facebook_url` | V1 | Footer social icons, contact page | yes |
| `instagram_url` | V1 | Footer social icons, contact page | yes |
| `church_youtube_channel` | V1 | Footer social icons, contact page, livestream YouTube button | yes |
| `default_livestream_url` | V1 | Fallback embed when no active livestream is set | **no** (not wired yet) |
| `about_us_text` | V1 | About page content | **no** (only the admin editor shows it, as a textarea) |
| `vision_statement` | V1 | Vision section | **no** (admin editor textarea only) |
| `mission_statement` | V1 | Mission section | **no** (admin editor textarea only) |
| `giving_bank_name` | V1 | Giving / donation page | **no** (not wired yet) |
| `giving_account_name` | V1 | Giving / donation page | **no** (not wired yet) |
| `giving_account_number` | V1 | Giving / donation page | **no** (not wired yet) |
| `giving_branch` | V1 | Giving / donation page | **no** (not wired yet) |
| `church_map_embed_url` | V7 | *(missing from README)* Google Maps iframe `src` on `/contact` and `/services` | yes |
| `church_map_link_url` | V7 | *(missing from README)* "Open in Google Maps" / "Get Directions" buttons on `/contact`, `/services`, `/new-here` | yes |

Long-text fields (`about_us_text`, `vision_statement`, `mission_statement`) render as textareas in the admin settings editor.

## Domain behaviour

- **Livestream**: add at `/admin/livestreams/create`. The embed URL must be `https://www.youtube.com/embed/VIDEO_ID`. Only one livestream can be active; activating one deactivates all others. The active stream appears on the homepage and `/livestream`. The YouTube button uses `church_youtube_channel` (fallback `https://youtube.com/@imanueljakarta`).
- **Service times**: managed at `/admin/service-times` (name, day of week, start/end time, location, description, active flag, sort order). Active ones appear automatically in the footer (every page), on `/services`, and on `/livestream`, both in the "Check back on Sundays at …" line and the schedule cards.
- **Events**: lifecycle draft → published → cancelled, with a featured flag, slug-based detail URLs, and a calendar JSON feed.
- **Announcements**: date-bounded with priority; shown as a banner on the homepage.
- **Newsletter**: members subscribe via the footer or profile page. When an admin publishes a news article and clicks "Send Newsletter", every subscriber gets an email with an excerpt and a link to the full article. The link uses `app.public-url` in `config-repo/church-interaction-service.yml` (`https://gmimimanueljakarta.or.id`), not the internal service URL, so recipients always get the production domain. To unsubscribe, members click the link in any newsletter email; a confirmation email verifies the request, and clicking it completes the removal.
- **Prayer requests**: public form; confidential entries are visible to `ROLE_ADMIN` / `ROLE_SUPER_ADMIN` only.
- **Passwords**: BCrypt-hashed. Changing a password requires an emailed 6-digit OTP (single-use, 5-minute expiry). An admin password reset emails a secure 12-character temporary password. Forgot-password with OTP starts from the login page.
- **Account status**: `ACTIVE` / `INACTIVE` / `LOCKED`, checked on every login.
- **Audit logging**: every write operation across the 6 backend services (auth, user, cms, event, media, interaction) is recorded to `church-audit-service` as a fire-and-forget HTTP call, so business operations are never blocked if audit is down. Recorded fields: actor (username), actor role, action name, entity type, entity ID, entity name, originating service, IP address, timestamp. The admin UI at `/admin/audit-logs` gives a searchable, filterable, paginated view with color-coded action badges.
- **Google Maps**: embedded with a plain `<iframe>`; no Google API key or billing is needed. To change the location, open Google Maps, go to the church, choose Share → Embed a map, copy the `src="…"` value into the `church_map_embed_url` setting, and put the share link in `church_map_link_url`. No redeploy is needed.
- **Birthdays**: optional `birthday DATE` on users; the admin birthday list is filterable by month (permission `USER_BIRTHDAY_VIEW`).
