import { test, expect } from "@playwright/test";
import { SEEDED_USERS } from "../../fixtures/seeded-users";

// This spec intentionally does not use a stored auth state - it drives the
// real login form, unlike the other P0 specs which reuse the storageState
// produced by tests/auth.setup.ts.
test.describe("login", () => {
  test("logs in with valid seeded credentials and leaves /login", async ({
    page,
  }) => {
    await page.goto("/login");
    await page.locator("#username").fill(SEEDED_USERS.user1.username);
    await page.locator("#password").fill(SEEDED_USERS.user1.password);
    await page.getByTestId("login-submit").click();

    await expect(page).not.toHaveURL(/\/login$/);

    await page.goto("/recipes");
    await expect(page).toHaveURL(/\/recipes$/);
  });

  test("shows an error and stays on /login with a wrong password", async ({
    page,
  }) => {
    await page.goto("/login");
    await page.locator("#username").fill(SEEDED_USERS.user1.username);
    await page.locator("#password").fill("wrong-password");
    await page.getByTestId("login-submit").click();

    await expect(page.locator(".toast-error")).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });
});
