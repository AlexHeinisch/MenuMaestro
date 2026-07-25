# Agent Instructions

See [Claude.md](Claude.md) for project overview, architecture, and build/run commands.

## Before finalizing a feature touching a core user flow

If your change touches a core user flow - authentication, recipes, menus, meals,
organizations, or shopping lists - run the full Playwright E2E suite locally before
considering the feature done, in addition to any unit/integration tests:

1. Build and run the backend (with the Angular UI embedded) against a local Postgres,
   per [Claude.md](Claude.md)'s run instructions.
2. `cd e2e && npm ci && npx playwright install --with-deps chromium && npm test`

See [`e2e/README.md`](e2e/README.md) for full setup details, the seeded test accounts,
and the suite's selector conventions. This suite is not run on pull requests in CI
(only on push to `main`, see `.github/workflows/e2e.yml`), so this local run is the
primary safety net for core-flow regressions before a PR is opened.
