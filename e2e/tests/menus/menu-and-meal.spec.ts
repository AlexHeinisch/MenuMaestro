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

  test("Return to Menu button appears only on a meal's detail page and navigates back to the menu", async ({
    page,
  }) => {
    const menuName = uniqueName("E2E Return Menu");
    const recipeName = uniqueName("E2E Return Recipe");
    const returnButton = page.getByRole("button", { name: "Return to Menu" });

    await createMenu(page, menuName, ORGANIZATIONS.coolOrg);
    const menuUrl = page.url();

    await createRecipe(page, recipeName);
    const recipeUrl = page.url();
    await addMealToMenu(page, ORGANIZATIONS.coolOrg, menuName);

    // The standalone recipe-edit page (not reached via a menu) must never show it.
    await page.goto(`${recipeUrl}/edit`);
    await expect(returnButton).toHaveCount(0);

    // Navigate menu -> meal detail, where the meal is a recipe instance in the menu.
    await page.goto(menuUrl);
    await page.getByText(recipeName, { exact: true }).click();
    await expect(page).toHaveURL(/\/menus\/\d+\/meal\/\d+$/);
    const mealUrl = page.url();

    await expect(returnButton).toBeVisible();

    // The meal's edit page only offers Cancel (back to this meal), not Return to Menu.
    await page.goto(`${mealUrl}/edit`);
    await expect(returnButton).toHaveCount(0);

    await page.goto(mealUrl);
    await returnButton.click();
    await expect(page).toHaveURL(menuUrl);

    // Removing the meal from the menu navigates away, so the button disappears with it.
    await page.goto(mealUrl);
    await page.getByRole("button", { name: "Remove from Menu" }).click();
    await page.getByRole("button", { name: "Confirm" }).click();
    await expect(page).toHaveURL(menuUrl);
    await expect(returnButton).toHaveCount(0);
  });
});
