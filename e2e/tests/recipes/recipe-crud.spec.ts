import { test, expect } from "@playwright/test";
import { uniqueName } from "../../fixtures/test-data";
import { createRecipe } from "../../fixtures/actions";
import { authFile } from "../../fixtures/auth";

test.use({ storageState: authFile("user1") });

test.describe("recipe CRUD", () => {
  test("creates a recipe, sees it on the overview/detail pages, then edits it", async ({
    page,
  }) => {
    const recipeName = uniqueName("E2E Recipe");

    await createRecipe(page, recipeName);
    await expect(page.getByRole("heading", { name: recipeName })).toBeVisible();

    await page.goto("/recipes");
    // The recipe list is paginated and can accumulate many recipes across
    // runs - filter down to this test's own recipe via the search box rather
    // than assuming it's on the first page of an unfiltered listing.
    await page
      .getByRole("textbox", { name: "What would you like to eat?" })
      .fill(recipeName);
    await expect(page.getByText(recipeName)).toBeVisible();
    await page.getByText(recipeName).click();
    await expect(page).toHaveURL(/\/recipes\/\d+$/);

    await page.getByRole("button", { name: "Edit" }).click();
    await expect(page).toHaveURL(/\/recipes\/\d+\/edit$/);
    await page.locator("#servings").fill("6");
    await page.getByRole("button", { name: "Save" }).click();

    await expect(page).toHaveURL(/\/recipes\/\d+$/);
    await expect(page.getByText("6 servings")).toBeVisible();
  });
});
