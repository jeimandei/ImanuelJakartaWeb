---
inclusion: always
---

# Commands: every runnable command in this project

<!-- Migrated 2026-09-24 from README.md (Local Development, Running Tests, Production Deployment,
     Viewing Logs), Containerfile, compose.yml, .github/workflows/deploy.yml, .gitignore, and the
     git/PR procedure the Claude Code harness enforced. Each command is tagged:
       [verified]  executed during this migration on 2026-09-24 (OpenJDK 21.0.10, Maven 3.9.11) — result noted
       [README]    copied from README.md, not executed here
       [workflow]  what .github/workflows/deploy.yml runs on the production server
       [derived]   not written anywhere in the repo; standard tool usage inferred from the config — verify first -->

There is **no** package.json, Makefile, Justfile or Taskfile. Everything runs through Maven, Podman Compose or the GitHub Actions deploy workflow. Run commands from the repository root unless a `cd` is shown.

## 1. Setup / install

| Purpose | Command | Tag |
|---|---|---|
| Prerequisites | Java 21+, Maven 3.9+, PostgreSQL 15+ (containers: Podman + `podman compose`) | [README] |
| Create local env file | `cp .env.example .env` then fill in real values. `.env` is gitignored; `.gitignore` says "copy .env.example to .env and fill in real values" | [derived from .gitignore] |
| Install all modules into `~/.m2` (needed before `mvn spring-boot:run` in a module) | `mvn clean install -DskipTests` | [verified] BUILD SUCCESS (~19 s warm) |
| Pre-fetch dependencies (what the Containerfile does) | `mvn dependency:go-offline -B -s maven-settings.xml` | [workflow/Containerfile] |

### Local database (PostgreSQL 15+)

```bash
psql -U postgres -c "CREATE USER church_app WITH PASSWORD '<db-password>';"   # README uses the dev default; see security.md
psql -U postgres -c "CREATE DATABASE imanuelweb_db OWNER church_app;"

# Apply all migrations in order
psql -U church_app -d imanuelweb_db -f database/V1__initial_schema.sql
psql -U church_app -d imanuelweb_db -f database/V2__add_permissions.sql
psql -U church_app -d imanuelweb_db -f database/V3__extended_permissions.sql
psql -U church_app -d imanuelweb_db -f database/V4__permission_categories.sql
psql -U church_app -d imanuelweb_db -f database/V5__service_times.sql
psql -U church_app -d imanuelweb_db -f database/V6__add_birthday_to_users.sql
psql -U church_app -d imanuelweb_db -f database/V7__add_map_settings.sql     # missing from README's list
```
Tags: [README] for V1–V6; V7 [derived] (same pattern). The password must match `DB_PASSWORD` (or the dev fallback in `config-repo/application.yml`). `database/setup.sh` is **stale**: it creates `church_db` and applies only V1. Don't use it.

## 2. Build

| Purpose | Command | Tag |
|---|---|---|
| Build everything | `mvn clean install -DskipTests` | [verified] |
| Build one service + its deps (exactly what the Containerfile runs) | `mvn package -pl <service> -am -DskipTests -B -s maven-settings.xml` | [verified] with `church-gateway-service` → `church-gateway-service/target/church-gateway-service-1.0.0-SNAPSHOT.jar` |
| Fast compile check of main + test code (closest thing to a "typecheck") | `mvn -B -q test-compile` | [verified] exit 0 |
| Build a container image for one service | `podman compose --env-file .env build <service>` | [workflow] |

## 3. Run locally

### Option A: plain Maven (README "Start Services (in order)"), one terminal per service

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
[README]. Run `mvn clean install -DskipTests` first so `church-common` is in `~/.m2`.

⚠ [derived, not verified] Every client's `application.yml` hardcodes `spring.config.import: "configserver:http://church-config-server:8888"`, a container hostname. Outside containers, export this in each client terminal before `mvn spring-boot:run`:
```bash
export SPRING_CONFIG_IMPORT=configserver:http://localhost:8888
```
Alternatively, map `church-config-server` to `127.0.0.1` in your hosts file. The config server itself finds `../config-repo` when started from `church-config-server/` (`CONFIG_REPO_PATH` default).

| URL | What |
|---|---|
| http://localhost:8080 | Public church website |
| http://localhost:8080/calendar | Interactive event calendar |
| http://localhost:8080/admin | Admin dashboard |
| http://localhost:8080/login | Login page (default admin: username `admin`; password in README "Default Admin Credentials". Change it anywhere real) |

Useful dev toggles are listed in tech.md (`ddl-auto: update` on first run then back to `validate`; Thymeleaf cache; DEBUG logging).

### Option B: containers (same commands the deploy workflow runs on the server)

| Purpose | Command | Tag |
|---|---|---|
| Prepare log dir | `mkdir -p logs && chmod 777 logs` | [workflow] |
| Build one image | `podman compose --env-file .env build <service>` | [workflow] |
| Start the whole stack | `podman compose --env-file .env up -d` | [workflow] |
| Rebuild + replace specific services | `podman compose --env-file .env build <services>` then `podman compose --env-file .env up -d --force-recreate --no-deps <services>` | [workflow] |
| Restart specific services / all | `podman compose --env-file .env restart <services>` / `podman compose --env-file .env restart` | [workflow] |
| Status | `podman compose --env-file .env ps` | [workflow] |
| API gateway health | `curl -sf http://localhost:8080/actuator/health \| grep -q UP && echo '✓ API gateway healthy'` | [workflow] |
| Apply a migration to the running DB | `podman exec -i church-postgres sh -c 'psql -U $POSTGRES_USER -d imanuelweb_db' < database/V<N>__<name>.sql` | [workflow] |

Fresh init: the `database/V1…V7` files are bind-mounted into `/docker-entrypoint-initdb.d/` and run automatically **only when the `postgres-data` volume is empty**.

## 4. Test

| Purpose | Command | Tag |
|---|---|---|
| All tests, fail at end | `mvn test -fae` | [verified] **175 tests run, 9 FAIL → BUILD FAILURE** on main @ 22c734c (details below) |
| One module (+ the modules it depends on) | `mvn -B -pl <module> -am test` | [verified] `church-event-service`: 38 (common) + 13 (event) pass |
| One test class | `mvn -B -q -pl <module> -am test -Dtest=<ClassName> -Dsurefire.failIfNoSpecifiedTests=false` | [verified] with `church-event-service` / `EventServiceImplTest` |

README: "175 unit tests across all 9 modules — JUnit 5 + Mockito, no Spring context or database required." Per module: church-common 38, church-auth-service 10, church-user-service 14, church-cms-service 18, church-event-service 13, church-media-service 22, church-interaction-service 12, church-audit-service 21, church-gateway-service 27. The count matches the verified run. `church-api-gateway` and `church-config-server` have no tests.

**Currently failing (verified 2026-09-24). These are stale tests, not production bugs:**
- `church-cms-service` (4): `CmsPageServiceImplTest$DeletePage` ×2, `NewsArticleServiceImplTest$DeleteArticle` ×2. The tests stub/verify `existsById`, but `deletePage`/`deleteArticle` now call `findById`.
- `church-media-service` (1 failure + 3 errors): `GalleryServiceImplTest$DeleteGalleryItem` ×2, `SermonServiceImplTest.deleteSermon_*` ×2. Same `existsById`→`findById` drift, plus Mockito `UnnecessaryStubbingException`.
- `church-gateway-service` (1): `InteractionControllerTest$NewsletterSubscribe.post_validEmail_redirectsToHome`. It expects `subscribeNewsletter(email, name)`, but the controller now calls the 3-arg overload `(email, name, null)`.

There is no CI on pushes or PRs, so nothing catches this automatically. Run `mvn test -fae` yourself before pushing, and don't make the failure count worse.

## 5. Lint / format / typecheck

**None configured.** There is no Checkstyle, Spotless, PMD, SpotBugs, formatter plugin, `.editorconfig`, ESLint or Prettier. Use `mvn -B -q test-compile` as the compile gate, and follow the style rules in `conventions.md` by hand. Don't mass-reformat files.

## 6. Deploy: GitHub Actions "Deploy — Church Platform" (`.github/workflows/deploy.yml`)

Manual `workflow_dispatch` only; concurrency group `deploy` (queued, never cancelled); `ubuntu-latest`, 60-minute timeout. Trigger it in GitHub: **Actions → Deploy — Church Platform → Run workflow**. [derived] CLI: `gh workflow run deploy.yml -f action=<action> -f services="<svc ...>" -f branch=main`.
**The maintainer triggers deploys. Don't trigger one yourself unless explicitly asked.**

Inputs: `action` (required choice), `services` (space-separated; required for `build-service` and `restart-service`; valid: `postgres, church-config-server, church-auth-service, church-user-service, church-cms-service, church-event-service, church-media-service, church-interaction-service, church-audit-service, church-gateway-service, church-api-gateway`), `branch` (default `main`). Required secrets: `SSH_KEYS`, `ENV_FILE`. The workflow `env:` defines `DEPLOY_DIR=/home/ibytes/website/gmimimanueljakarta` plus `SSH_HOST`, `SSH_PORT`, `SSH_USER` (see the file).

| Action | Input | What it does on the server [workflow] |
|---|---|---|
| `full-deploy` | `branch` | First time: `git init`, `git remote add origin https://github.com/<repo>.git`, `git fetch origin`, `git checkout -b <branch> --track origin/<branch>`. Otherwise: `git fetch origin`, `git checkout <branch>`, `git reset --hard origin/<branch>`, `git clean -fd`. Then `mkdir -p logs && chmod 777 logs`; `podman compose --env-file .env build <svc>` one by one for church-config-server, church-auth-service, church-user-service, church-cms-service, church-event-service, church-media-service, church-interaction-service, church-audit-service, church-gateway-service, church-api-gateway; then `podman compose --env-file .env up -d` |
| `build-service` | `services`, `branch` | `git fetch origin`, `git checkout <branch>`, `git reset --hard origin/<branch>`, `podman compose --env-file .env build <services>`, `podman compose --env-file .env up -d --force-recreate --no-deps <services>` |
| `restart-service` | `services` | `podman compose --env-file .env restart <services>` (no rebuild, no git pull) |
| `restart-all` | — | `podman compose --env-file .env restart` |
| `deploy-config` | `branch` | Checks out `<branch>` on the runner, `rsync -az --delete -e "ssh -p <port>" config-repo/ <user>@<host>:<DEPLOY_DIR>/config-repo/`, then on the server `curl -sf -X POST http://localhost:8888/actuator/refresh -H "Content-Type: application/json" \|\| true` and `podman compose --env-file .env restart church-api-gateway` |
| `deploy-env` | — | `printf '%s\n' "$ENV_FILE" \| ssh … "cat > <DEPLOY_DIR>/.env && chmod 600 <DEPLOY_DIR>/.env"` |
| `run-migrations` | (`branch`) | `git fetch origin`, `git checkout <branch>`, `git reset --hard origin/<branch>` (added in PR #108, because files merged after the last pull were missing on the server), then for **V2…V7** in order: `podman exec -i church-postgres sh -c 'psql -U $POSTGRES_USER -d imanuelweb_db' < database/V<N>__<name>.sql`. V1 is not re-run |

After the action: container status `podman compose --env-file .env ps` (skipped for deploy-env, deploy-config, run-migrations), and an API gateway health check for `full-deploy`, or for `build-service` when the services include `church-api-gateway`.

### Deploy recipes (from README + session practice)
- **First-time server setup:** `deploy-env` → `full-deploy` (branch `main`).
- **Code change in one service:** `build-service` with that service name (for example `church-gateway-service`), branch `main`. Template/CSS/JS changes live in `church-gateway-service`, so rebuild that.
- **Gateway now calls a new backend endpoint:** rebuild the backend **first or in the same run**, for example `build-service → church-cms-service church-gateway-service`. Deploying only the gateway caused a live 404 ("SiteSetting not found with key: public").
- **Seed/settings-only migration** (for example V7): `run-migrations` only. No rebuild, because settings are read at request time.
- **Schema migration that `ddl-auto: validate` checks** (README order):
  ```
  1. build-service  → church-cms-service church-gateway-service
                      (gets new code onto server without restarting auth/user yet)
  2. run-migrations → applies pending V*.sql scripts
  3. build-service  → church-auth-service church-user-service
                      (now safe to restart — validate passes with the new column)
  ```
  Generalise it: migrate before restarting any service whose entities reference the new columns. **Never use `full-deploy` for incremental schema changes**: it builds and restarts all services before migrations can run.
- **Config change** (`config-repo/`): `deploy-config`, then `restart-service` for every affected service other than `church-api-gateway`, which deploy-config already restarts. ⚠ [derived] The workflow's `/actuator/refresh` call is best-effort (`|| true`). Port 8888 isn't published to the host, clients expose only `health`, and there is no `@RefreshScope`, so don't rely on hot refresh.
- **New env var:** update the `ENV_FILE` secret → `deploy-env` → `build-service`/`restart-service` for the consumers.
- ⚠ [derived] `run-migrations` runs `psql` without `-v ON_ERROR_STOP=1`, so a failing statement does **not** fail the job. Read the job log for `ERROR:` lines.

## 7. Logs (on the server, in `DEPLOY_DIR`)

```bash
# Tail a specific service
tail -f logs/church-gateway-service.log

# Watch all services at once
tail -f logs/church-*.log
```
[README]. Compose also sends container stdout to journald (tag = container name).

## 8. Git and PR workflow commands

| Purpose | Command | Tag |
|---|---|---|
| Fetch a specific branch | `git fetch origin <branch>` (prefer specific branches) | [harness rule] |
| Pull | `git pull origin <branch>` | [harness rule] |
| Create a working branch from latest main | `git fetch origin main && git checkout -b <type>/<kebab-name> origin/main` | [derived] |
| Push | `git push -u origin <branch>`; on **network** errors only, retry up to 4 times with backoff 2 s, 4 s, 8 s, 16 s | [harness rule] |
| Restart a branch whose PR was already merged (keep the name) | `git fetch origin main && git checkout -B <branch> origin/main`. `git push --force-with-lease` is fine only when the branch holds nothing but already-merged history; otherwise rebase the unmerged commits onto the new base | [harness rule] |
| Open a draft PR to main | `gh pr create --draft --base main --title "<type>: <summary>" --body-file <file>` (the Claude sessions used the GitHub MCP server instead; see security.md) | [derived] |
| Multi-line commit message | see the block below | [practice] |

```bash
git commit -m "$(cat <<'EOF'
<type>: <imperative summary>

<why the change was needed; what areas/files it touches>

Deploy: <workflow action + services + order, or "run-migrations only — no service rebuild needed">
EOF
)"
```

Don't use interactive git flags (`git rebase -i`, `git add -i`); agent terminals can't drive them.

## 9. Commands that run inside containers or CI (reference; you don't type these)

| Where | Command | Tag |
|---|---|---|
| Every service container entrypoint | `java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -jar app.jar` | [Containerfile] |
| Postgres healthcheck | `pg_isready -U ${DB_USERNAME} -d imanuelweb_db` | [compose.yml] |
| Spring service healthcheck | `wget -qO- http://localhost:<port>/actuator/health \| grep -q UP` | [compose.yml] |
| CI runner SSH setup | `printf '%s\n' "${{ secrets.SSH_KEYS }}" > ~/.ssh/id_rsa`, `chmod 600 ~/.ssh/id_rsa`, `ssh-keyscan -p <SSH_PORT> -H <SSH_HOST> >> ~/.ssh/known_hosts` | [workflow] |
| Post-deploy status | `sleep 10`, then `ssh … "cd <DEPLOY_DIR> && podman compose --env-file .env ps"` | [workflow] |
