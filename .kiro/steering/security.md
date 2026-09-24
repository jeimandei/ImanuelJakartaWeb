---
inclusion: always
---

# Security: secrets, permissions, agent policy, MCP

<!-- Migrated 2026-09-24 from README.md "Security", .env.example (names only), a credential-pattern scan of
     all tracked files, the Claude Code harness configuration (~/.claude/launcher-settings.json permissions +
     Stop hook, policy-limits.json) and the safety rules the harness applied in every session. No secret VALUES
     are reproduced anywhere under .kiro/ or in AGENTS.md. -->

## 1. Secrets policy (applies to every agent)

- **Never copy, print, log or commit secret values**: passwords, JWT secrets, SMTP app passwords, SSH keys, tokens, `.env` contents. Refer to secrets **by variable name only** (`JWT_SECRET`, `DB_PASSWORD`, `MAIL_PASSWORD`, `SSH_KEYS`, `ENV_FILE`, …).
- Real values live only in the local `.env` (gitignored, created from `.env.example`), the server's `/home/ibytes/website/gmimimanueljakarta/.env` (`chmod 600`, written by the `deploy-env` workflow action), and the GitHub Actions secrets `SSH_KEYS` and `ENV_FILE`.
- `.env.example` holds names and placeholder values only. When adding a variable, add its **name** and a placeholder there (for example `NEW_VAR=change-me`), never a real value.
- If a file you're asked to share or copy looks like it contains credentials, stop and tell the maintainer which file and line, instead of inlining it.

## 2. Credential-bearing files found in this repo (flagged; values intentionally NOT copied)

| File:line | What | Risk / recommended action (needs maintainer decision) |
|---|---|---|
| `.env.example:7` | A concrete `JWT_SECRET` value, not a placeholder | High **if production relies on it** (see next row). Replace it with a placeholder and rotate the real secret |
| `config-repo/application.yml:23` | The **same** JWT secret as the fallback default `${JWT_SECRET:<value>}` | If `ENV_FILE` ever lacks `JWT_SECRET`, services silently sign/verify with a publicly committed key, so anyone could forge admin JWTs. Remove the fallback so startup fails fast, and confirm `ENV_FILE` sets a unique value |
| `.env.example:4`, `config-repo/application.yml:11` | Dev default DB password (also the `DB_PASSWORD` fallback) | Low (postgres has no host port), but make sure production overrides it |
| `README.md` "Local Development → Database Setup" and "Default Admin Credentials" | Dev DB password and the default admin login in plaintext | Make sure the production `admin` password was changed |
| `database/setup.sh` | Echoes the dev DB password and default admin credentials | Stale script; same as above |
| `database/V1__initial_schema.sql:256,259` | Comment with the default admin password in plaintext + the seeded bcrypt hash | Seed data; change the admin password after first login everywhere real |
| `.github/workflows/deploy.yml` `env:` | Production SSH host IP, SSH port, SSH user and deploy path, committed in plain text (the key itself is in the `SSH_KEYS` secret) | Not a credential, but consider moving host/port/user to secrets or vars |
| Placeholders only (fine) | `MAIL_USERNAME`/`MAIL_PASSWORD` defaults in `.env.example` and `config-repo/church-{user,interaction}-service.yml` are obvious `your-…` placeholders | None |

A scan for API keys, GitHub tokens, private keys and AWS keys found none. Claude's own credentials (`~/.claude.json` OAuth account, the harness `codesign-mcp-config.json` bearer token, `~/.claude/sessions/*.key`) lived only in the ephemeral cloud container. They are not part of this repo and were not migrated.

## 3. Application security model (README "Security", verbatim, with verification notes)

- Passwords hashed with BCrypt
- JWT signed with HS256, configurable expiration (default 24 h)
- Admin routes protected by Spring Security `@PreAuthorize`
- CSRF tokens on all HTML forms (gateway-service; backend REST services disable CSRF and are stateless JWT)
- Confidential prayer requests visible to `ROLE_ADMIN` / `ROLE_SUPER_ADMIN` only
- Account status (ACTIVE / INACTIVE / LOCKED) checked on every login
- Audit service is internal-only (not exposed via gateway). ⚠ **Not true as configured**, see §4
- Password change requires OTP email verification
- Admin password reset sends a secure 12-character temporary password by email

How it's wired:
- **Backend services:** stateless, `JwtAuthenticationFilter` (church-common) before `UsernamePasswordAuthenticationFilter`, CSRF disabled, `@EnableMethodSecurity`, public GETs listed in each `config/SecurityConfig.java` `permitAll()`, everything else `authenticated()`. Example: CMS permits `GET /api/settings/{key}`, which also matches `GET /api/settings/public`.
- **Gateway-service (browser-facing):** form login at `/login` (`GatewayAuthenticationProvider` exchanges credentials for a JWT with the auth service), logout `POST /logout` → `/login?logout=true`, access-denied page `/403`, `maximumSessions(1)`, BCrypt `PasswordEncoder`, explicit public-path allowlist, `/admin/**` → `hasAnyRole("ADMIN","SUPER_ADMIN","EDITOR")`, everything else `authenticated()`.

## 4. Open security findings (from code/config review on 2026-09-24; not tested against production)

1. **Audit API likely publicly reachable.** `config-repo/church-api-gateway.yml` routes `/api/audit/**` through the public gateway, and `church-audit-service` `SecurityConfig` is `anyRequest().permitAll()` with no `@PreAuthorize` on `AuditLogController`. Anyone could likely `GET /api/audit` (usernames, roles, IP addresses, actions) and `POST /api/audit` (forge entries). A possible fix, to be agreed with the maintainer: remove the public route and point the gateway-service `services.audit-service.url` at `AUDIT_SERVICE_URL` (`http://church-audit-service:8087`) on the internal network, or require an `ADMIN`/`SUPER_ADMIN` JWT on `GET` and restrict `POST` to internal callers.
2. **JWT secret fallback** is committed (see §2).
3. **Production runs with verbose settings from `config-repo`**: `com.jeimandei: DEBUG`, `org.springframework.web.client: DEBUG` (gateway-service) and `spring.thymeleaf.cache: false`. This is informational; decide if that's intended.
4. `ROLE_PASTOR`, `ROLE_MEDIA_MANAGER` and `ROLE_EVENT_MANAGER` have DB permissions but can't open `/admin/**` as coded (gateway allows only ADMIN/SUPER_ADMIN/EDITOR).

Don't fix these silently as a side effect of other work. Raise them, and fix them in dedicated PRs when asked.

## 5. Agent permissions policy (migrated from the Claude setup)

What the Claude setup actually had:
- **Project level:** no `.claude/settings.json`, no `.claude/settings.local.json`, no allow/deny lists, no hooks, no MCP config.
- **Claude cloud harness** (`~/.claude/launcher-settings.json`, not in the repo): `permissions.allow: ["Skill"]` (Claude's Skill tool, which has no Kiro equivalent and nothing to re-apply) and a **Stop hook** running `~/.claude/stop-hook-git-check.sh`. That hook blocked the agent from finishing while there were uncommitted changes, untracked files or unpushed commits on the current branch (plus a commit-signing check specific to Claude's infrastructure).
- **Org policy** (`~/.claude/policy-limits.json`): `enforce_web_search_mcp_isolation.allowed: false`, `remote_control_at_startup: false`. These are Claude product settings with no Kiro equivalent.
- The cloud sandbox also had a **network egress allowlist**. It blocked, for example, `maps.app.goo.gl` and `kiro.dev`. That was environment-specific, not project policy.

Intended policy to re-apply in Kiro (the formats differ, so set these in **Kiro's own settings**, not from this file):
- **Stop-hook rule, as a standing rule:** before declaring any task done, run `git status` and make sure the tree is clean and the branch is pushed. Optionally recreate it as a Kiro Agent Hook if your Kiro version offers an "agent stop"/"agent turn complete" trigger (create it in the Hooks UI so the schema is right).
- **Safe to auto-approve** (read-only / local): `git status`, `git diff`, `git log`, `git fetch`, `mvn -B -q test-compile`, `mvn test -fae`, `mvn -pl <module> -am test`, `mvn clean install -DskipTests`, `ls`, `cat`, `grep`.
- **Always ask first:** `git push`, `git push --force-with-lease`, `git reset --hard`, `git clean`, `rm -rf`, `gh pr create`, `gh pr merge`, `gh workflow run` (deploys), anything with `ssh`, `rsync`, `podman`, `psql` against a non-local database, and editing `.env`.
- **Never:** print or commit secrets; run commands on the production server unless explicitly asked.

## 6. MCP servers

- **Configured in the repo or `.claude`: none.** There was nothing to translate, so no `.kiro/settings/mcp.json` was created.
- **Provided by the Claude cloud environment at runtime** (not project config): `github` (the GitHub MCP server), used for PR work in this repo: `create_pull_request` (always as draft), `list_pull_requests`, `pull_request_read` (check runs, review comments), plus the harness-only `subscribe_pr_activity`, which has no GitHub-MCP equivalent. Others present but unused for this project: Claude Code Remote, Claude Docs, Figma, Google Drive, Fireflies (Whisper_OpenAI).
- **Proposal, not activated; confirm before creating `.kiro/settings/mcp.json`.** The simplest Kiro option is the `gh` CLI with no MCP. If you want a GitHub MCP server instead:

```json
{
  "mcpServers": {
    "github": {
      "command": "podman",
      "args": ["run", "-i", "--rm", "-e", "GITHUB_PERSONAL_ACCESS_TOKEN", "ghcr.io/github/github-mcp-server"],
      "env": { "GITHUB_PERSONAL_ACCESS_TOKEN": "${GITHUB_PERSONAL_ACCESS_TOKEN}" },
      "disabled": true,
      "autoApprove": []
    }
  }
}
```

Notes: use `docker` instead of `podman` if that's your local engine. Check that your Kiro version expands `${VAR}` in `env`; if it doesn't, remove the `env` block and export `GITHUB_PERSONAL_ACCESS_TOKEN` in the environment Kiro starts from (the `-e NAME` flag forwards it). Keep `autoApprove` empty or read-only tools only. The token needs repo scope for `jeimandei/ImanuelJakartaWeb` only.
