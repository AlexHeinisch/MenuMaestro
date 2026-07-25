import { test, expect } from "@playwright/test";
import { uniqueName } from "../../fixtures/test-data";
import { addMealToMenu, createMenu, createRecipe } from "../../fixtures/actions";
import { ORGANIZATIONS } from "../../fixtures/seeded-users";
import { authFile } from "../../fixtures/auth";

test.use({ storageState: authFile("user1") });

test.describe("menu and meal", () => {
  test("creates a menu, adds a meal to it, and shows correctly scaled ingredient quantities", async ({
    page,
  }) => {
    const menuName = uniqueName("E2E Menu");
    const recipeName = uniqueName("E2E Meal Recipe");
    const menuPeopleCount = 8; // double the recipe's 4 servings, to make scaling observable

    await createMenu(page, menuName, ORGANIZATIONS.coolOrg, menuPeopleCount);
    const menuUrl = page.url();

    await createRecipe(page, recipeName);
    await addMealToMenu(page, ORGANIZATIONS.coolOrg, menuName);

    await page.goto(menuUrl);
    await page.getByText(recipeName, { exact: true }).click();

    await expect(page).toHaveURL(/\/menus\/\d+\/meal\/\d+$/);
    await expect(page.getByRole("heading", { name: recipeName })).toBeVisible();
    await expect(page.getByText(`${menuPeopleCount} servings`)).toBeVisible();
    // Recipe has 1 piece of Apple for 4 servings; scaled to 8 people -> 2 piece.
    await expect(page.getByText("2 piece")).toBeVisible();
    await expect(page.getByText("Apple")).toBeVisible();
  });
});
