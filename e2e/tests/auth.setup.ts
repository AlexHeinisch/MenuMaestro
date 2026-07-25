import { test as setup, expect } from "@playwright/test";
import { SEEDED_USERS } from "../fixtures/seeded-users";
import { authFile } from "../fixtures/auth";

/**
 * Logs in once per seeded user needed across specs, via the real login form,
 * and persists the resulting JWT (stored in localStorage) as Playwright
 * storageState, so other specs can start already authenticated without
 * re-driving the login UI in every file. auth/login.spec.ts is the one place
 * that exercises the login form itself.
 *
 * - user1: ADMIN of CoolOrg - the default identity for most specs.
 * - user2: MEMBER of CoolOrg - a lower-permission user, and the invitee in
 *   the org-member-management spec.
 * - user4: OWNER of LonelyOrg, shares no organization with user1 - the
 *   "outsider" identity for the recipe-visibility spec.
 */
const usersToAuthenticate = [
  SEEDED_USERS.user1,
  SEEDED_USERS.user2,
  SEEDED_USERS.user4,
];

for (const user of usersToAuthenticate) {
  setup(`authenticate as ${user.username}`, async ({ page }) => {
    await page.goto("/login");
    await page.locator("#username").fill(user.username);
    await page.locator("#password").fill(user.password);
    await page.getByTestId("login-submit").click();

    await expect(page).not.toHaveURL(/\/login$/);
    await page.context().storageState({ path: authFile(user.username) });
  });
}
