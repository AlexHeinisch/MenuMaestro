import { test, expect } from "@playwright/test";
import { ORGANIZATIONS, SEEDED_USERS } from "../../fixtures/seeded-users";
import { authFile } from "../../fixtures/auth";

test.use({ storageState: authFile("user1") });

// Read-only assertions only - CoolOrg and its seeded membership must never be
// mutated by this spec (see application.yaml's menumaestro.initial-organizations).
test.describe("organization view", () => {
  test("shows CoolOrg's seeded members after navigating from the overview", async ({
    page,
  }) => {
    await page.goto("/organizations");
    await page.getByText(ORGANIZATIONS.coolOrg, { exact: true }).click();

    await expect(page).toHaveURL(/\/organizations\/\d+$/);
    await expect(
      page.getByRole("heading", { name: ORGANIZATIONS.coolOrg }),
    ).toBeVisible();

    const table = page.locator("table");
    for (const user of [
      SEEDED_USERS.admin.username,
      SEEDED_USERS.user1.username,
      SEEDED_USERS.user2.username,
    ]) {
      await expect(table.getByText(user, { exact: true })).toBeVisible();
    }
  });
});
