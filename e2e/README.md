# MenuMaestro E2E Tests

Playwright end-to-end tests that exercise MenuMaestro's core user journeys against the
full stack (Postgres + Spring Boot backend + the built Angular UI served at `/ui`).

## Overview

The suite covers the app's core domain end-to-end: authentication, recipes (CRUD and
visibility), organizations (viewing, member invite/role-change/removal, permission
restrictions), menus (creation, adding meals, closing), shopping lists (generation,
guest/QR-code access), and stash (ingredient inventory CRUD). It runs Chromium only for
now (see `playwright.config.ts` for how to add Firefox/WebKit later). See
[Known gaps / P1 backlog](#known-gaps--p1-backlog) for what's intentionally not covered
yet.

## Prerequisites

- Node 22.22.3 (matches `frontend/`'s version; a pinned binary lives at
  `frontend/node/` if you don't have this globally)
- A Postgres instance matching `application/src/main/resources/application.yaml`'s
  datasource: database `dev`, user `db_user`, password `u3iGTeLr`, on `localhost:5432`
  (see `k8s/postgres.yaml` for a plain `postgres:16` reference, or run one via Docker
  yourself)
- The backend built and running on `localhost:8080` with the Angular UI embedded,
  e.g. from the repo root:

  ```bash
  mvn clean install -DskipTests
  mvn spring-boot:run -pl application
  ```

  Note: `/actuator/health` will report `DOWN` unless you either have a reachable SMTP
  server configured, or set `MANAGEMENT_HEALTH_MAIL_ENABLED=false` in the backend's
  environment - the app itself still works fine either way, this only affects the
  health endpoint (used by CI to detect readiness, see `.github/workflows/e2e.yml`).

  `menumaestro.initial-accounts`/`initial-organizations` must stay enabled (the
  default) so the seeded accounts below exist.

## Running locally

```bash
cd e2e
npm ci
npx playwright install --with-deps chromium
npm test
```

Other useful scripts:

- `npm run test:headed` - run with a visible browser window
- `npm run test:ui` - Playwright's interactive UI mode, great for debugging a single spec
- `npm run report` - open the HTML report from the last run

## Configuration

`E2E_BASE_URL` controls where the suite points (`playwright.config.ts`); it defaults to
`http://localhost:8080/ui`. You can point it at `ng serve`'s `http://localhost:4200/ui`
instead for faster frontend-only iteration - the backend still needs to be running
either way, since `ng serve` only proxies `/api` and `/ws` to it.

## Test data & seeded accounts

The backend seeds dev accounts/organizations by default (see
`application/src/main/resources/application.yaml`, `menumaestro.initial-accounts` /
`initial-organizations`):

| Username | Password  | CoolOrg role | LonelyOrg role |
| -------- | --------- | ------------ | --------------- |
| admin    | hallo123  | Owner        | -               |
| user1    | hallo123  | Admin        | -               |
| user2    | hallo123  | Member       | -               |
| user3    | hallo123  | Invited      | Planner         |
| user4    | hallo123  | -            | Owner           |

**Never mutate these shared accounts/organizations directly in a test.** Every spec
that needs a recipe/menu/organization/stash creates its own uniquely-named entity (see
`fixtures/test-data.ts`'s `uniqueName()` helper) and asserts against what it created,
so the suite is safe to re-run repeatedly and in parallel against the same seeded
Postgres instance. `tests/auth.setup.ts` (a Playwright "setup" project) logs in once
per seeded user needed across specs - currently `user1` (the default identity for most
specs), `user2` (a lower-permission CoolOrg Member, used in the permission and
member-management specs), and `user4` (an "outsider" sharing no organization with
`user1`, used in the recipe-visibility spec) - and persists each as `storageState` in
`fixtures/auth.ts`'s `authFile(username)`. `auth/login.spec.ts` is the only spec that
drives the real login form instead of reusing a storageState.

**Multiple sessions in one spec** (e.g. an owner inviting a member, then that member
accepting): open a second `browser.newContext()` with an explicit `storageState`
(either `authFile(otherUsername)`, or `{ cookies: [], origins: [] }` for a genuinely
unauthenticated guest). Always pass `storageState` explicitly on these manual
contexts - a bare `browser.newContext()` inherits the spec file's top-level
`test.use({ storageState })` instead of starting blank, which silently makes a
"guest" context still logged in as whichever user the file's default `page` uses.

**Local-only accumulation caveat:** re-running specs many times against the same
persistent local Postgres (not an issue in CI, whose Postgres service container is
fresh per run) can eventually make a helper that browses a paginated, unfiltered list
(e.g. the organizations overview, or a menu's "Add to Menu" recipient dropdown) unable
to find a just-created entity once enough same-named entities pile up. If specs that
were passing start intermittently failing to find their own just-created org/menu
after many local runs, delete leftover `E2E %`-named rows from `menu`, `recipe_value`,
and `organization` (and their dependents) rather than assuming it's a real bug.

## Selector convention

Most of the app has no `data-testid` attributes, and Angular's `<label for="">`
association only works where a component explicitly passes an `[id]`. Rather than
mixing locator strategies ad hoc, this suite uses:

- `page.locator('#some-id')` wherever a stable `[id]` already exists (most forms)
- `data-testid`/`[ariaLabel]` where nothing else was stable (currently: the login
  submit button, the menu-detail page's icon-only "generate shopping list" trigger,
  the organizations overview's accept/decline-invitation icon buttons, and the
  stash page's per-row edit/delete icon buttons)
- `getByRole`/text locators for plain buttons and read-only assertions - prefer
  `{ exact: true }` whenever another button's name could otherwise be a substring
  match (e.g. a bare "Delete" button vs. a page that also has "Delete Selected")

When you add a new spec that needs to interact with an element that has none of the
above, add a small `[id]` or `data-testid` to that template rather than reaching for a
brittle CSS/structural selector - see `frontend/src/app/pages/auth/login/login-page.html`
for an example of the pattern.

## CI

`.github/workflows/e2e.yml` runs this suite on every push to `main` (not on PRs, since
a full Maven build + JVM boot + Postgres + browser automation is much heavier than the
existing Karma unit-test CI in `frontend-ci.yml`). See [`AGENTS.md`](../AGENTS.md) at
the repo root for guidance on running the suite locally before finalizing a feature
that touches a core flow.

On a failed run, check the workflow's uploaded artifacts:

- `playwright-report` - the HTML report, including traces/screenshots/video for failed
  tests
- `application-log` - the backend's stdout/stderr from that run

## Known gaps / P1 backlog

Deliberately still out of scope:

- Registration + email verification (needs a mail-catcher; the backend currently
  points at a real, non-functional SMTP server in this environment)
- Password reset flow (same mail-catcher dependency)
- Real-time collaborative shopping list updates over WebSocket/STOMP (multiple
  clients seeing each other's checkbox changes live)
- Multi-browser (Firefox/WebKit) coverage
- Finer-grained permission/role tests (e.g. Shopper vs. Planner distinctions,
  Invited-but-not-yet-accepted restrictions beyond what's already covered)
