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

test.describe("menu lifecycle", () => {
  test("closing a menu makes it read-only and closes its shopping lists", async ({
    page,
  }) => {
    const menuName = uniqueName("E2E Close Menu");
    const recipeName = uniqueName("E2E Close Recipe");
    const shoppingListName = uniqueName("E2E Close Shopping List");

    await createMenu(page, menuName, ORGANIZATIONS.coolOrg);
    const menuUrl = page.url();
    await createRecipe(page, recipeName);
    await addMealToMenu(page, ORGANIZATIONS.coolOrg, menuName);
    const shoppingListUrl = await createShoppingListFromMenu(
      page,
      menuUrl,
      shoppingListName,
    );

    await page.goto(menuUrl);
    await page.getByRole("button", { name: "Close Menu" }).click();
    await page.getByRole("button", { name: "Confirm" }).click();
    await expect(page).toHaveURL(/\/menus$/);

    // Closed menus route to /menus/:id/closed (see menu-overview's
    // routerLink) - the overview list is paginated with no reliable way to
    // find this specific menu once many exist, so construct the URL
    // directly from the id captured before closing rather than searching.
    await page.goto(`${menuUrl}/closed`);
    await expect(page).toHaveURL(/\/menus\/\d+\/closed$/);

    // Its shopping list is closed too, and loses its shopper-only actions.
    await page.goto(shoppingListUrl);
    await expect(page.getByText("Closed", { exact: true })).toBeVisible();
    await expect(page.getByRole("button", { name: "Access" })).toHaveCount(0);
    await expect(
      page.getByRole("button", { name: "Close List" }),
    ).toHaveCount(0);
  });
});
