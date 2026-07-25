import { test, expect } from "@playwright/test";
import { uniqueName } from "../../fixtures/test-data";
import { createOrganization, SEEDED_INGREDIENT } from "../../fixtures/actions";
import { authFile } from "../../fixtures/auth";

test.use({ storageState: authFile("user1") });

// Uses a throwaway organization's stash (not CoolOrg's/LonelyOrg's shared
// stash) so these mutations never touch shared seed data.
test.describe("stash CRUD", () => {
  test("adds, edits, and removes an ingredient from an organization's stash", async ({
    page,
  }) => {
    const orgName = uniqueName("E2E Stash Org");
    await createOrganization(page, orgName);

    await page.getByRole("button", { name: "View Stash" }).click();
    await expect(page).toHaveURL(/\/stashes\/\d+$/);

    // Add
    await page.getByRole("button", { name: "Ingredient" }).click();
    await page.locator("#ingredientAdd").fill(SEEDED_INGREDIENT);
    await page.getByText(SEEDED_INGREDIENT, { exact: true }).click();
    await page.locator("#ingredientAmount").fill("5");
    await page.getByRole("button", { name: "Submit" }).click();

    const row = page.locator("tr", { hasText: SEEDED_INGREDIENT });
    await expect(row.getByText(/5\s*piece/)).toBeVisible();

    // Edit
    await row.getByRole("button", { name: "Edit Stash Entry" }).click();
    await page.locator("#ingredientAmount").fill("9");
    await page.getByRole("button", { name: "Confirm" }).click();
    await expect(row.getByText(/9\s*piece/)).toBeVisible();

    // Remove
    await row.getByRole("button", { name: "Delete Stash Entry" }).click();
    // exact: true - "Delete" is otherwise a substring of both "Delete
    // Selected" (bulk action bar) and "Delete Stash Entry" (this row's own
    // icon), which are still present/visible behind the confirmation modal.
    await page.getByRole("button", { name: "Delete", exact: true }).click();
    await expect(row).not.toBeVisible();
    await expect(page.getByText("This stash is empty.")).toBeVisible();
  });
});
