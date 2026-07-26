# Agent Instructions

See [Claude.md](Claude.md) for project overview, architecture, and build/run commands.

## Before finalizing a feature touching a core user flow

If your change touches a core user flow - authentication, recipes, menus, meals,
organizations, or shopping lists - run the full Playwright E2E suite locally before
considering the feature done, in addition to any unit/integration tests. Full runbook:

### 1. Start Postgres

A plain `postgres:16` container matching `application/src/main/resources/application.yaml`'s
datasource (db `dev`, user `db_user`, password `u3iGTeLr`, port `5432` - same
credentials as `k8s/postgres.yaml`'s deployment):

```bash
docker run -d --name menumaestro-pg -p 5432:5432 \
  -e POSTGRES_DB=dev -e POSTGRES_USER=db_user -e POSTGRES_PASSWORD=u3iGTeLr \
  postgres:16
```

### 2. Build and run the backend

```bash
mvn clean package -DskipTests -DskipUnitTests -DskipIntegrationTests
MANAGEMENT_HEALTH_MAIL_ENABLED=false nohup java -jar application/target/application-*.jar > app.log 2>&1 &
```

`-DskipTests` etc. keep this fast for iteration - run the full `mvn clean install`
(no skip flags) before finalizing, per [Claude.md](Claude.md). `MANAGEMENT_HEALTH_MAIL_ENABLED=false`
is needed because `/actuator/health` reports `DOWN` without a reachable SMTP server in
this environment - the app itself works fine either way, this only affects the health
probe (used below, and by CI to detect readiness). Poll until it's ready before running
tests against it:

```bash
until curl -s http://localhost:8080/actuator/health | grep -q '"status":"UP"'; do sleep 2; done
```

`menumaestro.initial-accounts`/`initial-organizations` must stay enabled (the default)
so the E2E suite's seeded accounts exist. Re-run the build+restart after any frontend
template/backend change you want reflected - the running jar is a static snapshot.

### 3. Run the E2E suite

```bash
cd e2e && npm ci && npx playwright install --with-deps chromium && npm test
```

See [`e2e/README.md`](e2e/README.md) for full setup details, the seeded test accounts,
and the suite's selector conventions. This suite is not run on pull requests in CI
(only on push to `main`, see `.github/workflows/e2e.yml`), so this local run is the
primary safety net for core-flow regressions before a PR is opened.

### Local-only test-data cleanup

Specs create their own uniquely-named (`E2E %`) recipes/menus/organizations rather than
mutating seeded data, so the suite is safe to re-run repeatedly against the same
Postgres instance. But after enough repeated local runs, a helper that browses an
unfiltered/paginated list (e.g. the organizations overview, or a menu's "Add to Menu"
recipient dropdown) can fail to find its own just-created entity once enough
same-named rows pile up (not an issue in CI, whose Postgres is fresh per run). If
specs that were passing start intermittently failing to find their own just-created
org/menu, clean up leftover `E2E %`-named rows - respecting foreign keys - roughly:

```sql
-- menus and dependents (shopping_list_item, shopping_list, menu_item, then menu itself and its stash)
-- recipes and dependents (recipe_ingredient_use, recipe_cooking_appliance_use, recipe, then recipe_value)
-- organizations and dependents (organization_account_relation, then organization itself and its stash)
DELETE FROM menu WHERE name LIKE 'E2E %';
DELETE FROM recipe_value WHERE name LIKE 'E2E %';
DELETE FROM organization WHERE name LIKE 'E2E %';
```

Delete in FK-safe order (children before parents) - check each table's foreign keys
first with `\d <table>` in `psql` rather than assuming this exact list is complete.
