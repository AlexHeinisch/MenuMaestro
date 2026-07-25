import { test, expect } from "@playwright/test";
import { uniqueName } from "../../fixtures/test-data";
import {
  addMealToMenu,
  createMenu,
  createRecipe,
  createShoppingListFromMenu,
} from "../../fixtures/actions";
import { ORGANIZATIONS } from "../../fixtures/seeded-users";
import { authFile } from "../../fixtures/auth";

test.use({ storageState: authFile("user1") });

test.describe("shopping list generation", () => {
  test("generates a shopping list from a menu and shows its ingredients", async ({
    page,
  }) => {
    const menuName = uniqueName("E2E Shopping Menu");
    const recipeName = uniqueName("E2E Shopping Recipe");
    const shoppingListName = uniqueName("E2E Shopping List");

    await createMenu(page, menuName, ORGANIZATIONS.coolOrg, 4);
    const menuUrl = page.url();

    await createRecipe(page, recipeName);
    await addMealToMenu(page, ORGANIZATIONS.coolOrg, menuName);

    await createShoppingListFromMenu(page, menuUrl, shoppingListName);

    await expect(page).toHaveURL(/\/shopping-lists\/\d+$/);
    await expect(
      page.getByRole("heading", { name: shoppingListName }),
    ).toBeVisible();
    await expect(page.locator("#ingredient-Apple")).toBeAttached();
  });
});
