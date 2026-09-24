---
inclusion: fileMatch
fileMatchPattern: ".github/workflows/**"
---

# Deploy workflow (`.github/workflows/deploy.yml`)

<!-- Migrated 2026-09-24 from deploy.yml itself, README "Production Deployment", and the Claude-session fix in
     PR #108 (heredoc quoting analysis + git sync before migrations). Operational usage is in commands.md §6. -->

## Shape of the workflow

- `name: Deploy — Church Platform`, trigger **only** `workflow_dispatch`. There is no push/PR trigger and no other workflow file (no CI).
- Inputs:
  - `action` (required, `choice`): `full-deploy`, `build-service`, `restart-service`, `restart-all`, `deploy-config`, `deploy-env`, `run-migrations`. The input `description` explains each one; keep it in sync when adding an action.
  - `services` (string, default `''`): space-separated names, required for `build-service` / `restart-service`. Valid names: postgres, church-config-server, church-auth-service, church-user-service, church-cms-service, church-event-service, church-media-service, church-interaction-service, church-audit-service, church-gateway-service, church-api-gateway.
  - `branch` (string, default `'main'`).
- `concurrency: { group: deploy, cancel-in-progress: false }`: deploys queue, never cancel each other.
- `env:` `DEPLOY_DIR: /home/ibytes/website/gmimimanueljakarta`, `SSH_HOST`, `SSH_PORT`, `SSH_USER`. The host/port/user are committed here in plain text; `security.md` suggests moving them to secrets or vars.
- One job `deploy`, name `"${{ inputs.action }}${{ inputs.services != '' && format(' [{0}]', inputs.services) || '' }} @ ${{ inputs.branch }}"`, `runs-on: ubuntu-latest`, `timeout-minutes: 60`.

## Steps (each gated by `if: inputs.action == '…'`)

1. **Validate** that `services` is non-empty for `build-service` / `restart-service` (`::error::` + `exit 1`).
2. **Checkout** `${{ inputs.branch }}` with `actions/checkout@v4`, only for `deploy-config` (the other actions work over SSH on the server's own git checkout).
3. **Setup SSH**: `printf '%s\n' "${{ secrets.SSH_KEYS }}" > ~/.ssh/id_rsa`, `chmod 600`, `ssh-keyscan -p $SSH_PORT -H $SSH_HOST >> ~/.ssh/known_hosts`.
4. One step per action (`[deploy-env]`, `[deploy-config]` ×2, `[full-deploy]`, `[build-service]`, `[restart-service]`, `[restart-all]`, `[run-migrations]`). The exact server commands are listed in commands.md §6.
5. **Container status** `podman compose --env-file .env ps` after `sleep 10`, for every action except `deploy-env`, `deploy-config` and `run-migrations`.
6. **Health check**, API gateway: `curl -sf http://localhost:8080/actuator/health | grep -q UP && echo '✓ API gateway healthy' || echo '✗ API gateway not yet healthy (may still be starting)'`, for `full-deploy` or `build-service` whose services include `church-api-gateway`.

## Heredoc quoting rules: don't break these

Each server-side step is `ssh … << EOF` or `ssh … << 'EOF'`.
- `${{ … }}` expressions (`${{ env.DEPLOY_DIR }}`, `${{ inputs.branch }}`, `${{ inputs.services }}`) are substituted by **GitHub Actions before bash runs**, so they work inside both quoted and unquoted heredocs.
- **Unquoted `<< EOF`** (`full-deploy`, `build-service`, `restart-service`, `restart-all`): the **runner's** shell expands `$VARS` before sending. To expand on the server instead, escape it: `full-deploy` loops with `for svc in …; do … podman compose --env-file .env build \$svc; done`.
- **Quoted `<< 'EOF'`** (`run-migrations`, the second `deploy-config` step): nothing is expanded locally. **`run-migrations` must stay quoted.** `'psql -U $POSTGRES_USER -d imanuelweb_db'` has to reach `podman exec … sh -c` literally so `$POSTGRES_USER` expands **inside the postgres container**. An unquoted heredoc would expand it on the runner, to an empty string.
- `set -e` is at the top of every multi-command heredoc. `psql` returns 0 even when a statement fails (no `ON_ERROR_STOP`), so `set -e` does not catch SQL errors.

## `run-migrations` step: current body (keep this structure)

```yaml
      - name: "[run-migrations] Run pending SQL migrations"
        if: inputs.action == 'run-migrations'
        run: |
          ssh -p ${{ env.SSH_PORT }} ${{ env.SSH_USER }}@${{ env.SSH_HOST }} << 'EOF'
            set -e
            cd ${{ env.DEPLOY_DIR }}
            echo "→ Pulling latest code from branch ${{ inputs.branch }}"
            git fetch origin
            git checkout ${{ inputs.branch }}
            git reset --hard origin/${{ inputs.branch }}
            echo "→ Running V2__add_permissions.sql"
            podman exec -i church-postgres sh -c \
              'psql -U $POSTGRES_USER -d imanuelweb_db' \
              < database/V2__add_permissions.sql
            # … one identical block per migration, V3 … V7 …
            echo "✓ Migrations complete"
          EOF
```
- The git sync at the top was added in PR #108. Without it, a migration merged after the server's last pull fails with `database/V7__add_map_settings.sql: No such file or directory`.
- Adding a migration: append one block (echo + `podman exec … < database/V<N>__<name>.sql`) before `echo "✓ Migrations complete"`, in the same PR that adds the SQL file and the `compose.yml` bind mount.
- V1 is intentionally absent; it runs only at fresh DB init.

## Adding or changing an action

1. Add it to `inputs.action.options` **and** the `description` text.
2. Add a step `name: "[<action>] …"`, `if: inputs.action == '<action>'`, using the same SSH + `cd ${{ env.DEPLOY_DIR }}` + `set -e` pattern and the quoting rules above.
3. Decide whether it needs the post-deploy status or health steps (update their `if:`).
4. Update README "Workflow Actions" and `.kiro/steering/commands.md` §6.

## Secrets and safety

- Only `secrets.SSH_KEYS` and `secrets.ENV_FILE` are used. `ENV_FILE` is passed through an `env:` variable and piped to the server with `printf '%s\n' "$ENV_FILE" | ssh … "cat > …/.env && chmod 600 …/.env"`. Never `echo` a secret or enable `set -x` in these steps.
- `full-deploy` runs `git reset --hard` and `git clean -fd` on the server. That is safe for `.env` and `logs/` (both gitignored, and `-x` isn't used), but it removes any other untracked server-side edits.
- Agents never trigger this workflow themselves unless the maintainer explicitly asks (see conventions.md).
- If a PR build/test workflow is ever added, fix the 9 currently failing tests first (commands.md §4) or it will start red.
