---
inclusion: fileMatch
fileMatchPattern: "database/**"
---

# Database migrations (`database/V<N>__*.sql`)

<!-- Migrated 2026-09-24 from README.md "Database Migrations" + "Running Database Migrations on a Live Server",
     the V1–V7 scripts themselves, compose.yml, deploy.yml and the Claude-session incidents (PRs #71, #84, #85,
     #108, commit b5c61f7). The same checklist is summarised in conventions.md §3A, which is always included. -->

## How schema is managed

- Every Spring service except audit runs with `spring.jpa.hibernate.ddl-auto: validate`. Hibernate **never** creates or alters tables; it only checks that entities match the DB, and a service **won't start** if a mapped column is missing.
- The shared schema (database `imanuelweb_db`) is changed **only** by hand-written SQL files in `database/`, named `V<N>__<snake_case_description>.sql`, applied in numeric order.
- There is no Flyway for the shared schema; the `V<N>__` naming is only a convention. **Exception:** `church-audit-service` runs Flyway itself (`classpath:db/migration`, history table `flyway_schema_history_audit`, `baseline-on-migrate: true`, `baseline-version: 0`) for its `audit_logs` table. Put audit schema changes in `church-audit-service/src/main/resources/db/migration/`, not in `database/`.
- All migrations are safe to re-run (`IF NOT EXISTS` / `ON CONFLICT DO NOTHING`), and **must stay that way**: the `run-migrations` workflow re-executes V2…VN every time it runs.

## Existing migrations

| File | Description |
|---|---|
| `V1__initial_schema.sql` | 17 core tables (`roles`, `users`, `user_roles`, `cms_pages`, `cms_content_blocks`, `events`, `sermons`, `livestreams`, `announcements`, `prayer_requests`, `contact_messages`, `news_articles`, `gallery_items`, `site_settings`, `newsletter_subscriptions`, `testimony_submissions`, `volunteer_applications`), seed roles (MEMBER → SUPER_ADMIN), admin user, sample site settings |
| `V2__add_permissions.sql` | `permissions` + `role_permissions` tables, 28 permissions, 3 new roles (PASTOR, MEDIA_MANAGER, EVENT_MANAGER), role-permission assignments |
| `V3__extended_permissions.sql` | Granular permissions for every capability (USER_MANAGE_ROLES, LIVE_MANAGE, GALLERY_*, NEWSLETTER_*, etc.) |
| `V4__permission_categories.sql` | `permission_categories` lookup table, seeds all 8 category names |
| `V5__service_times.sql` | `service_times` table with day_of_week, start/end time, location, description, active flag, sort_order; SERVICE_TIME_* permissions |
| `V6__add_birthday_to_users.sql` | `birthday DATE` column on `users`; `USER_BIRTHDAY_VIEW` permission granted to ADMIN/SUPER_ADMIN |
| `V7__add_map_settings.sql` | Seeds `church_map_embed_url` (Google Maps iframe src; default is a search-based embed for "GMIM Imanuel Jakarta") and `church_map_link_url` (`https://maps.app.goo.gl/5HjLYiw4aH42j3Z96`). Missing from README's table |

Permission categories (V4): `USERS`, `CONTENT`, `EVENTS`, `MEDIA`, `PASTORAL`, `COMMUNITY`, `SETTINGS`, `AUDIT`.

## Writing a new migration: template

```sql
-- ============================================================
-- Imanuel Jakarta Church Platform — DB Migration V<N>
-- <One or more lines: what this adds/changes and why.>
-- ============================================================

BEGIN;

-- Schema
CREATE TABLE IF NOT EXISTS example_things (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
ALTER TABLE users ADD COLUMN IF NOT EXISTS some_column DATE;
CREATE INDEX IF NOT EXISTS idx_example_things_name ON example_things(name);

-- ============================================================
-- Permissions
-- ============================================================

INSERT INTO permissions (name, description, category) VALUES
    ('EXAMPLE_VIEW', 'View example things', 'CONTENT')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- Role → permission assignments
-- ============================================================
-- SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_name = 'ROLE_SUPER_ADMIN'
  AND p.name IN ('EXAMPLE_VIEW')
ON CONFLICT DO NOTHING;

-- ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_name = 'ROLE_ADMIN'
  AND p.name IN ('EXAMPLE_VIEW')
ON CONFLICT DO NOTHING;

-- Seed data
INSERT INTO site_settings (setting_key, setting_value, description) VALUES
    ('example_key', 'value', 'Human-readable description shown as the label in /admin/settings')
ON CONFLICT (setting_key) DO NOTHING;

COMMIT;
```

Rules:
- Idempotent only: `CREATE TABLE IF NOT EXISTS`, `ALTER TABLE … ADD COLUMN IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS`, `INSERT … ON CONFLICT (…) DO NOTHING`. Anything that can't be made idempotent (renames, drops, data rewrites) needs a guard (`DO $$ … IF EXISTS … $$`) and a discussion with the maintainer first.
- Wrap in `BEGIN; … COMMIT;` (V5+ style). V2–V4 end with a stray `COMMIT;` and no `BEGIN;` (a harmless warning); don't copy that.
- Keep the header block. V7 lacks it; don't copy that either.
- Style: 4-space indent, aligned column definitions, UPPERCASE SQL keywords, snake_case identifiers, plural table names.
- `site_settings.description` is shown as the field label in the admin editor, so make it human-readable.
- The `site_settings` columns are `setting_key` (unique), `setting_value` (TEXT), `description`, `updated_at`.

## Wiring a new migration (all required, in the same PR)

1. The file `database/V<N>__<name>.sql` (next number; latest today is **V7**).
2. `compose.yml` → `services.postgres.volumes`, after the previous line:
   ```yaml
         - ./database/V<N>__<name>.sql:/docker-entrypoint-initdb.d/V<N>__<name>.sql:ro
   ```
3. `.github/workflows/deploy.yml` → the `[run-migrations]` step, after the previous block and before `echo "✓ Migrations complete"`:
   ```bash
               echo "→ Running V<N>__<name>.sql"
               podman exec -i church-postgres sh -c \
                 'psql -U $POSTGRES_USER -d imanuelweb_db' \
                 < database/V<N>__<name>.sql
   ```
   Keep the step's quoted heredoc `<< 'EOF'` (see `deploy-workflow.md`).
4. README: the "Database Migrations" table, the local-dev `psql -U church_app -d imanuelweb_db -f database/V<N>__….sql` list, and every "V1–V<N>" mention.
5. Entity/DTO/mapper changes in the affected services, plus unit tests.
6. The PR summary states the deploy order.

The maintainer had to ask "did you make the same run mitigation for the new thing?" when step 3 was missed. Treat steps 1–4 as one unit.

## Applying migrations

- **Fresh container DB:** files bind-mounted into `/docker-entrypoint-initdb.d/` run automatically, **only** when the `postgres-data` volume is first initialised.
- **Live database:** the GitHub Actions `run-migrations` action (see commands.md §6). It first syncs the server checkout (`git fetch origin` / `git checkout <branch>` / `git reset --hard origin/<branch>`, added in PR #108), then pipes **V2…VN** through `podman exec -i church-postgres sh -c 'psql -U $POSTGRES_USER -d imanuelweb_db' < database/V<N>__….sql`. V1 is never re-run.
- **Local:** `psql -U church_app -d imanuelweb_db -f database/V<N>__<name>.sql`.

### Deploy ordering with `ddl-auto: validate` (README)
When migrations must be applied before new code runs (for example adding a column that `ddl-auto: validate` will check), follow this order:
```
1. build-service  → church-cms-service church-gateway-service
                    (gets new code onto server without restarting auth/user yet)
2. run-migrations → applies pending V*.sql scripts
3. build-service  → church-auth-service church-user-service
                    (now safe to restart — validate passes with the new column)
```
In general: get the new code and migration files onto the server, run the migration, then restart the services whose entities use the new columns. Never use `full-deploy` for incremental schema changes. Seed-only migrations (like V7) need only `run-migrations`.

## Caveats

- ⚠ **psql doesn't stop on errors** in `run-migrations` (no `-v ON_ERROR_STOP=1`), so the job can pass while a statement failed. Read the job log for `ERROR:`.
- ⚠ **V10 ordering:** `/docker-entrypoint-initdb.d` runs files in lexical order, so `V10__…` sorts before `V2__…`. Before adding V10, agree with the maintainer on a fix (for example zero-padding the mount target names).
- `database/setup.sh` and V1's header comment reference the old database name `church_db` and `psql -U postgres -f setup.sh` (setup.sh is a bash script). Both are stale; the real database is `imanuelweb_db`.
- V1 seeds a default `admin` user whose password is documented in README and in a V1 comment. Never rely on it outside local dev.
