---
inclusion: manual
---

# API reference (manual steering — pull in with `#api-reference` when working on endpoints)

<!-- Migrated 2026-09-24. Part A is copied verbatim from README.md "## REST API Reference".
     Parts B and C were generated from the controller source on main @ 22c734c by parsing
     @RequestMapping / @Get|Post|Put|Delete|PatchMapping / @PreAuthorize annotations.
     Regenerate B/C after adding endpoints (see "How to regenerate" at the end). -->

All backend endpoints are reached **through `church-api-gateway` on :8080**. A path prefix only works if it is listed in the route predicates of `config-repo/church-api-gateway.yml`. Backend responses use the `ApiResponse<T>` envelope from `church-common` (`success`, `message`, `data`, `timestamp`, `errors`; null fields omitted).

**Authorization column:** an `@PreAuthorize` expression if present. `—` means no annotation, so access is decided by that service's `config/SecurityConfig.java`: either an explicit `permitAll()` matcher for public GETs, or `anyRequest().authenticated()` (any logged-in user with a valid JWT).

## Part A: README REST API Reference (verbatim)


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


## Part B: Complete backend endpoint list (generated from source)

README's list (Part A) is incomplete. It omits, for example, `/api/cms/blocks/**`, `/api/news/all`, `/api/events/all`, `/api/events/{id}`, testimony/volunteer admin endpoints, `/api/newsletter/check`, `/api/newsletter/notify-news`, `/api/users/forgot-password`, `/api/users/reset-password-otp`, `/api/permission-categories/**`, and livestream deactivate/update. Treat this table as the source of truth.

| Method | Path | Handler | Authorization |
|---|---|---|---|
| `POST` | `/api/audit` | `AuditLogController.create` | — |
| `GET` | `/api/audit` | `AuditLogController.query` | — |
| `POST` | `/api/auth/register` | `AuthController.register` | — |
| `POST` | `/api/auth/login` | `AuthController.login` | — |
| `POST` | `/api/auth/logout` | `AuthController.logout` | — |
| `GET` | `/api/auth/me` | `AuthController.getCurrentUser` | — |
| `GET` | `/api/announcements/active` | `AnnouncementController.getActiveAnnouncements` | — |
| `GET` | `/api/announcements` | `AnnouncementController.getAllAnnouncements` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `GET` | `/api/announcements/{id}` | `AnnouncementController.getAnnouncementById` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `POST` | `/api/announcements` | `AnnouncementController.createAnnouncement` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `PUT` | `/api/announcements/{id}` | `AnnouncementController.updateAnnouncement` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `DELETE` | `/api/announcements/{id}` | `AnnouncementController.deleteAnnouncement` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |
| `GET` | `/api/cms/pages` | `CmsPageController.getAllPages` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `GET` | `/api/cms/pages/published` | `CmsPageController.getPublishedPages` | — |
| `GET` | `/api/cms/pages/{id}` | `CmsPageController.getPageById` | — |
| `GET` | `/api/cms/pages/slug/{slug}` | `CmsPageController.getPageBySlug` | — |
| `GET` | `/api/cms/pages/slug/{slug}/published` | `CmsPageController.getPublishedPageBySlug` | — |
| `POST` | `/api/cms/pages` | `CmsPageController.createPage` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `PUT` | `/api/cms/pages/{id}` | `CmsPageController.updatePage` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `PUT` | `/api/cms/pages/{id}/publish` | `CmsPageController.publishPage` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `PUT` | `/api/cms/pages/{id}/unpublish` | `CmsPageController.unpublishPage` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `DELETE` | `/api/cms/pages/{id}` | `CmsPageController.deletePage` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |
| `GET` | `/api/cms/blocks/page/{pageId}` | `ContentBlockController.getBlocksByPage` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/api/cms/blocks` | `ContentBlockController.createBlock` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `PUT` | `/api/cms/blocks/{id}` | `ContentBlockController.updateBlock` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `DELETE` | `/api/cms/blocks/{id}` | `ContentBlockController.deleteBlock` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `PUT` | `/api/cms/blocks/{id}/toggle` | `ContentBlockController.toggleActive` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/api/news` | `NewsArticleController.getPublishedArticles` | — |
| `GET` | `/api/news/all` | `NewsArticleController.getAllArticles` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `GET` | `/api/news/{id}` | `NewsArticleController.getArticleById` | — |
| `GET` | `/api/news/slug/{slug}` | `NewsArticleController.getArticleBySlug` | — |
| `POST` | `/api/news` | `NewsArticleController.createArticle` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `PUT` | `/api/news/{id}` | `NewsArticleController.updateArticle` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `PUT` | `/api/news/{id}/publish` | `NewsArticleController.publishArticle` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `DELETE` | `/api/news/{id}` | `NewsArticleController.deleteArticle` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |
| `GET` | `/api/service-times/active` | `ServiceTimeController.getActiveServiceTimes` | — |
| `GET` | `/api/service-times` | `ServiceTimeController.getAllServiceTimes` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `GET` | `/api/service-times/{id}` | `ServiceTimeController.getServiceTimeById` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `POST` | `/api/service-times` | `ServiceTimeController.createServiceTime` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `PUT` | `/api/service-times/{id}` | `ServiceTimeController.updateServiceTime` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR')` |
| `DELETE` | `/api/service-times/{id}` | `ServiceTimeController.deleteServiceTime` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |
| `GET` | `/api/settings` | `SiteSettingController.getAllSettings` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |
| `GET` | `/api/settings/public` | `SiteSettingController.getPublicSettings` | — |
| `GET` | `/api/settings/{key}` | `SiteSettingController.getSettingByKey` | — |
| `PUT` | `/api/settings/{key}` | `SiteSettingController.updateSetting` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |
| `GET` | `/api/events` | `EventController.getPublishedEvents` | — |
| `GET` | `/api/events/all` | `EventController.getAllEvents` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `GET` | `/api/events/upcoming` | `EventController.getUpcomingEvents` | — |
| `GET` | `/api/events/past` | `EventController.getPastEvents` | — |
| `GET` | `/api/events/featured` | `EventController.getFeaturedEvents` | — |
| `GET` | `/api/events/{id}` | `EventController.getEventById` | — |
| `GET` | `/api/events/slug/{slug}` | `EventController.getEventBySlug` | — |
| `POST` | `/api/events` | `EventController.createEvent` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `PUT` | `/api/events/{id}` | `EventController.updateEvent` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `DELETE` | `/api/events/{id}` | `EventController.deleteEvent` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/events/{id}/featured` | `EventController.toggleFeatured` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `PUT` | `/api/events/{id}/publish` | `EventController.publishEvent` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `PUT` | `/api/events/{id}/cancel` | `EventController.cancelEvent` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `POST` | `/api/contact-messages` | `ContactMessageController.submitContactMessage` | — |
| `GET` | `/api/contact-messages` | `ContactMessageController.getAllMessages` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `GET` | `/api/contact-messages/{id}` | `ContactMessageController.getMessageById` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/contact-messages/{id}/status` | `ContactMessageController.updateStatus` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `GET` | `/api/contact-messages/unread-count` | `ContactMessageController.getUnreadCount` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `POST` | `/api/newsletter/subscribe` | `NewsletterController.subscribe` | — |
| `DELETE` | `/api/newsletter/unsubscribe` | `NewsletterController.unsubscribe` | — |
| `POST` | `/api/newsletter/request-unsubscribe` | `NewsletterController.requestUnsubscribe` | — |
| `GET` | `/api/newsletter/confirm-unsubscribe` | `NewsletterController.confirmUnsubscribe` | — |
| `DELETE` | `/api/newsletter/{id}` | `NewsletterController.deleteSubscriber` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `POST` | `/api/newsletter/notify-news` | `NewsletterController.notifyNews` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `GET` | `/api/newsletter/check` | `NewsletterController.checkSubscription` | — |
| `GET` | `/api/newsletter` | `NewsletterController.getAllSubscriptions` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `POST` | `/api/prayer-requests` | `PrayerRequestController.submitPrayerRequest` | — |
| `GET` | `/api/prayer-requests` | `PrayerRequestController.getAllPrayerRequests` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `GET` | `/api/prayer-requests/{id}` | `PrayerRequestController.getPrayerRequestById` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `PUT` | `/api/prayer-requests/{id}/status` | `PrayerRequestController.updateStatus` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `GET` | `/api/prayer-requests/stats` | `PrayerRequestController.getStats` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `POST` | `/api/testimonies` | `TestimonyController.submitTestimony` | — |
| `GET` | `/api/testimonies` | `TestimonyController.getApprovedTestimonies` | — |
| `GET` | `/api/testimonies/all` | `TestimonyController.getAllTestimonies` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/testimonies/{id}/approve` | `TestimonyController.approveTestimony` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `POST` | `/api/volunteer` | `VolunteerController.submitApplication` | — |
| `GET` | `/api/volunteer` | `VolunteerController.getAllApplications` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `GET` | `/api/volunteer/ministry/{ministry}` | `VolunteerController.getByMinistry` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/volunteer/{id}/status` | `VolunteerController.updateStatus` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `GET` | `/api/gallery` | `GalleryController.getAllGalleryItems` | — |
| `GET` | `/api/gallery/albums` | `GalleryController.getAlbumNames` | — |
| `GET` | `/api/gallery/album/{albumName}` | `GalleryController.getGalleryByAlbum` | — |
| `POST` | `/api/gallery` | `GalleryController.createGalleryItem` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/gallery/{id}` | `GalleryController.updateGalleryItem` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `DELETE` | `/api/gallery/{id}` | `GalleryController.deleteGalleryItem` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/gallery/{id}/toggle` | `GalleryController.toggleActive` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `GET` | `/api/livestreams/active` | `LivestreamController.getActiveLivestream` | — |
| `GET` | `/api/livestreams` | `LivestreamController.getAllLivestreams` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `GET` | `/api/livestreams/{id}` | `LivestreamController.getLivestreamById` | — |
| `POST` | `/api/livestreams` | `LivestreamController.createLivestream` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/livestreams/{id}` | `LivestreamController.updateLivestream` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `DELETE` | `/api/livestreams/{id}` | `LivestreamController.deleteLivestream` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/livestreams/{id}/activate` | `LivestreamController.activateLivestream` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/livestreams/{id}/deactivate` | `LivestreamController.deactivateLivestream` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `GET` | `/api/sermons` | `SermonController.getSermons` | — |
| `GET` | `/api/sermons/latest` | `SermonController.getLatestSermons` | — |
| `GET` | `/api/sermons/{id}` | `SermonController.getSermonById` | — |
| `POST` | `/api/sermons` | `SermonController.createSermon` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `PUT` | `/api/sermons/{id}` | `SermonController.updateSermon` | `hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EDITOR')` |
| `DELETE` | `/api/sermons/{id}` | `SermonController.deleteSermon` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `GET` | `/api/permission-categories` | `PermissionCategoryController.getAll` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/api/permission-categories/{id}` | `PermissionCategoryController.getById` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `POST` | `/api/permission-categories` | `PermissionCategoryController.create` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `PUT` | `/api/permission-categories/{id}` | `PermissionCategoryController.update` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `DELETE` | `/api/permission-categories/{id}` | `PermissionCategoryController.delete` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/api/permissions` | `PermissionController.getAllPermissions` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/api/permissions/{id}` | `PermissionController.getPermissionById` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `POST` | `/api/permissions` | `PermissionController.createPermission` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `PUT` | `/api/permissions/{id}` | `PermissionController.updatePermission` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `DELETE` | `/api/permissions/{id}` | `PermissionController.deletePermission` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/api/roles` | `RoleController.getAllRoles` | — |
| `GET` | `/api/roles/{id}` | `RoleController.getRoleById` | — |
| `POST` | `/api/roles` | `RoleController.createRole` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/roles/{id}` | `RoleController.updateRole` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `DELETE` | `/api/roles/{id}` | `RoleController.deleteRole` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `GET` | `/api/users` | `UserController.getUsers` | — |
| `GET` | `/api/users/birthdays` | `UserController.getBirthdays` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `GET` | `/api/users/{id}` | `UserController.getUserById` | — |
| `GET` | `/api/users/username/{username}` | `UserController.getUserByUsername` | — |
| `POST` | `/api/users` | `UserController.createUser` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/users/{id}` | `UserController.updateUser` | — |
| `PUT` | `/api/users/{id}/status` | `UserController.updateUserStatus` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/users/{id}/roles` | `UserController.assignRoles` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `PUT` | `/api/users/{id}/password` | `UserController.changePassword` | — |
| `POST` | `/api/users/{id}/request-password-otp` | `UserController.requestPasswordOtp` | — |
| `PUT` | `/api/users/{id}/change-password-otp` | `UserController.changePasswordWithOtp` | — |
| `POST` | `/api/users/{id}/reset-password` | `UserController.resetPassword` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `DELETE` | `/api/users/{id}` | `UserController.deleteUser` | `hasAnyRole('ADMIN', 'SUPER_ADMIN')` |
| `POST` | `/api/users/forgot-password` | `UserController.forgotPassword` | — |
| `POST` | `/api/users/reset-password-otp` | `UserController.resetPasswordWithOtp` | — |


## Part C: Frontend routes served by `church-gateway-service` (generated from source)

Public routes must also be listed in the `permitAll()` matcher of `church-gateway-service/.../config/SecurityConfig.java`. `/admin/**` requires `ADMIN`, `SUPER_ADMIN` or `EDITOR`, and everything else requires login.

| Method | Path | Handler | Authorization |
|---|---|---|---|
| `GET` | `/login` | `AuthController.loginPage` | — |
| `GET` | `/register` | `AuthController.registerPage` | — |
| `POST` | `/register` | `AuthController.register` | — |
| `GET` | `/profile` | `AuthController.profile` | `isAuthenticated()` |
| `POST` | `/profile` | `AuthController.updateProfile` | `isAuthenticated()` |
| `POST` | `/profile/request-otp` | `AuthController.requestPasswordOtp` | `isAuthenticated()` |
| `GET` | `/forgot-password` | `AuthController.forgotPasswordPage` | — |
| `POST` | `/forgot-password` | `AuthController.forgotPasswordSubmit` | — |
| `GET` | `/forgot-password/reset` | `AuthController.forgotPasswordResetPage` | — |
| `POST` | `/forgot-password/reset` | `AuthController.forgotPasswordReset` | — |
| `POST` | `/profile/change-password` | `AuthController.changePasswordWithOtp` | `isAuthenticated()` |
| `POST` | `/profile/newsletter-subscribe` | `AuthController.newsletterSubscribe` | `isAuthenticated()` |
| `POST` | `/profile/newsletter-unsubscribe` | `AuthController.newsletterUnsubscribe` | `isAuthenticated()` |
| `GET` | `/calendar` | `CalendarPublicController.calendar` | — |
| `GET` | `/calendar/feed` | `CalendarPublicController.feed` | — |
| `GET` | `/events` | `EventPublicController.events` | — |
| `GET` | `/events/{slug}` | `EventPublicController.eventDetail` | — |
| `GET` | `/gallery` | `GalleryPublicController.gallery` | — |
| `GET` | `/` | `HomeController.home` | — |
| `GET` | `/about` | `HomeController.about` | — |
| `GET` | `/services` | `HomeController.services` | — |
| `GET` | `/ministries` | `HomeController.ministries` | — |
| `GET` | `/giving` | `HomeController.giving` | — |
| `GET` | `/new-here` | `HomeController.newHere` | — |
| `GET` | `/faq` | `HomeController.faq` | — |
| `GET` | `/leadership` | `HomeController.leadership` | — |
| `GET` | `/403` | `HomeController.forbidden` | — |
| `GET` | `/prayer-request` | `InteractionController.prayerRequestForm` | — |
| `POST` | `/prayer-request` | `InteractionController.submitPrayerRequest` | — |
| `GET` | `/contact` | `InteractionController.contactForm` | — |
| `POST` | `/contact` | `InteractionController.submitContact` | — |
| `POST` | `/newsletter/subscribe` | `InteractionController.subscribeNewsletter` | — |
| `GET` | `/newsletter/confirm-unsubscribe` | `InteractionController.confirmUnsubscribe` | — |
| `GET` | `/livestream` | `LivestreamPublicController.livestream` | — |
| `GET` | `/news` | `NewsPublicController.news` | — |
| `GET` | `/news/{slug}` | `NewsPublicController.newsDetail` | — |
| `GET` | `/sermons` | `SermonPublicController.sermons` | — |
| `GET` | `/sermons/{id}` | `SermonPublicController.sermonDetail` | — |
| `GET` | `/admin/announcements` | `AdminAnnouncementController.listAnnouncements` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/announcements/` | `AdminAnnouncementController.listAnnouncements` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/announcements/create` | `AdminAnnouncementController.createForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/announcements/create` | `AdminAnnouncementController.createAnnouncement` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/announcements/{id}/edit` | `AdminAnnouncementController.editForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/announcements/{id}/edit` | `AdminAnnouncementController.updateAnnouncement` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/announcements/{id}/delete` | `AdminAnnouncementController.deleteAnnouncement` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/audit-logs` | `AdminAuditLogController.list` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/cms` | `AdminCmsController.listPages` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/cms/` | `AdminCmsController.listPages` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/cms/new` | `AdminCmsController.createForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/cms/create` | `AdminCmsController.createPage` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/cms` | `AdminCmsController.createPage` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/cms/{id}/edit` | `AdminCmsController.editForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/cms/{id}/edit` | `AdminCmsController.updatePage` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/cms/{id}/publish` | `AdminCmsController.publishPage` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/cms/{id}/unpublish` | `AdminCmsController.unpublishPage` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/cms/{id}/delete` | `AdminCmsController.deletePage` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |
| `GET` | `/admin/contacts` | `AdminContactController.listContacts` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/contacts/` | `AdminContactController.listContacts` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/contacts/{id}/status` | `AdminContactController.updateStatus` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin` | `AdminDashboardController.dashboard` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/` | `AdminDashboardController.dashboard` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/events` | `AdminEventController.listEvents` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/events/` | `AdminEventController.listEvents` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/events/create` | `AdminEventController.createForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/events/create` | `AdminEventController.createEvent` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/events/{id}/edit` | `AdminEventController.editForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/events/{id}/edit` | `AdminEventController.updateEvent` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/events/{id}/delete` | `AdminEventController.deleteEvent` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/gallery` | `AdminGalleryController.listGallery` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/gallery/` | `AdminGalleryController.listGallery` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/gallery/new` | `AdminGalleryController.createForm` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/gallery/create` | `AdminGalleryController.createGalleryItem` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/gallery` | `AdminGalleryController.createGalleryItem` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/gallery/{id}/edit` | `AdminGalleryController.editForm` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/gallery/{id}/edit` | `AdminGalleryController.updateGalleryItem` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/gallery/{id}/delete` | `AdminGalleryController.deleteGalleryItem` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/livestreams` | `AdminLivestreamController.listLivestreams` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/livestreams/` | `AdminLivestreamController.listLivestreams` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/livestreams/create` | `AdminLivestreamController.createForm` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/livestreams/create` | `AdminLivestreamController.createLivestream` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/livestreams/{id}/edit` | `AdminLivestreamController.editForm` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/livestreams/{id}/edit` | `AdminLivestreamController.updateLivestream` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/livestreams/{id}/activate` | `AdminLivestreamController.activateLivestream` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/livestreams/{id}/delete` | `AdminLivestreamController.deleteLivestream` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/news` | `AdminNewsController.listNews` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/news/` | `AdminNewsController.listNews` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/news/create` | `AdminNewsController.createForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/news/create` | `AdminNewsController.createNews` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/news/{id}/edit` | `AdminNewsController.editForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/news/{id}/edit` | `AdminNewsController.updateNews` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/news/{id}/publish` | `AdminNewsController.publishNews` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/news/{id}/delete` | `AdminNewsController.deleteNews` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/newsletter` | `AdminNewsletterController.list` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/newsletter/` | `AdminNewsletterController.list` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/newsletter/{id}/delete` | `AdminNewsletterController.delete` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/permissions` | `AdminPermissionController.listPermissions` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/permissions/` | `AdminPermissionController.listPermissions` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/permissions/new` | `AdminPermissionController.createForm` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/permissions` | `AdminPermissionController.createPermission` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/permissions/{id}/edit` | `AdminPermissionController.editForm` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/permissions/{id}/edit` | `AdminPermissionController.updatePermission` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/permissions/{id}/delete` | `AdminPermissionController.deletePermission` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/prayer-requests` | `AdminPrayerController.listPrayerRequests` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/prayer-requests/` | `AdminPrayerController.listPrayerRequests` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/prayer-requests/{id}/status` | `AdminPrayerController.updateStatus` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/roles` | `AdminRoleController.listRoles` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/roles/` | `AdminRoleController.listRoles` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/roles/new` | `AdminRoleController.createForm` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/roles` | `AdminRoleController.createRole` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/roles/{id}/edit` | `AdminRoleController.editForm` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/roles/{id}/edit` | `AdminRoleController.updateRole` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/roles/{id}/delete` | `AdminRoleController.deleteRole` | `hasAnyRole('ADMIN', 'SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/sermons` | `AdminSermonController.listSermons` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/sermons/` | `AdminSermonController.listSermons` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/sermons/create` | `AdminSermonController.createForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/sermons/create` | `AdminSermonController.createSermon` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/sermons/{id}/edit` | `AdminSermonController.editForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/sermons/{id}/edit` | `AdminSermonController.updateSermon` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/sermons/{id}/delete` | `AdminSermonController.deleteSermon` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/service-times` | `AdminServiceTimeController.list` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/service-times/` | `AdminServiceTimeController.list` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/service-times/new` | `AdminServiceTimeController.createForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/service-times/create` | `AdminServiceTimeController.create` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `GET` | `/admin/service-times/{id}/edit` | `AdminServiceTimeController.editForm` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/service-times/{id}/edit` | `AdminServiceTimeController.update` | `hasAnyRole('ADMIN','SUPER_ADMIN','EDITOR') (class-level)` |
| `POST` | `/admin/service-times/{id}/delete` | `AdminServiceTimeController.delete` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |
| `GET` | `/admin/settings` | `AdminSettingsController.settings` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/settings/` | `AdminSettingsController.settings` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/settings/update` | `AdminSettingsController.updateSettings` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/users` | `AdminUserController.listUsers` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/users/` | `AdminUserController.listUsers` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/users/birthdays` | `AdminUserController.birthdays` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/users/new` | `AdminUserController.createForm` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/users` | `AdminUserController.createUser` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `GET` | `/admin/users/{id}/edit` | `AdminUserController.editForm` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/users/{id}/edit` | `AdminUserController.updateUser` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/users/{id}/status` | `AdminUserController.updateStatus` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/users/{id}/roles` | `AdminUserController.assignRoles` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/users/{id}/reset-password` | `AdminUserController.resetPassword` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |
| `POST` | `/admin/users/{id}/delete` | `AdminUserController.deleteUser` | `hasAnyRole('ADMIN','SUPER_ADMIN') (class-level)` |


## How to regenerate Parts B/C
Re-run the extraction after adding or changing controllers. Any equivalent script works: collect each controller's class-level `@RequestMapping` + `@PreAuthorize`, then every method-level mapping annotation. Cross-check the row count per controller with:

```bash
for f in $(git ls-files 'church-*/src/main/java/*controller/*.java'); do printf "%-40s %s\n" "$(basename $f .java)" "$(grep -cE '^\s*@(Get|Post|Put|Delete|Patch)Mapping' $f)"; done
```
