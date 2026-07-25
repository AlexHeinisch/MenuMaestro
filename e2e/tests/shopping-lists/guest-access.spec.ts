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

test.describe("shopping list guest/QR access", () => {
  test("a guest can open a shopping list via its share link and check off an item", async ({
    page,
    browser,
  }) => {
    const menuName = uniqueName("E2E Guest Menu");
    const recipeName = uniqueName("E2E Guest Recipe");
    const shoppingListName = uniqueName("E2E Guest Shopping List");

    await createMenu(page, menuName, ORGANIZATIONS.coolOrg);
    const menuUrl = page.url();
    await createRecipe(page, recipeName);
    await addMealToMenu(page, ORGANIZATIONS.coolOrg, menuName);
    await createShoppingListFromMenu(page, menuUrl, shoppingListName);

    await page.getByRole("button", { name: "Access" }).click();
    // The share link is the current page's URL with a `?token=...` query
    // param set (see DetailShoppingListComponent.getShareTokenWithLink()).
    const shareUrl = await page
      .locator('a[href*="token="]')
      .getAttribute("href");
    expect(shareUrl).toContain("token=");

    // Explicit empty storageState: without it, browser.newContext() inherits
    // this file's test.use({ storageState }) default instead of starting
    // blank, which would make this "guest" context still be logged in.
    const guestContext = await browser.newContext({
      storageState: { cookies: [], origins: [] },
    });
    const guestPage = await guestContext.newPage();
    await guestPage.goto(shareUrl!);

    await expect(guestPage).not.toHaveURL(/\/login$/);
    await expect(
      guestPage.getByRole("heading", { name: shoppingListName }),
    ).toBeVisible();
    // Guests never authenticate, so account-only actions must be absent.
    await expect(
      guestPage.getByRole("button", { name: "Access" }),
    ).toHaveCount(0);
    await expect(
      guestPage.getByRole("button", { name: "Close List" }),
    ).toHaveCount(0);

    const ingredientCheckbox = guestPage.locator("#ingredient-Apple");
    await expect(ingredientCheckbox).not.toBeChecked();
    await ingredientCheckbox.check();
    await expect(ingredientCheckbox).toBeChecked();

    await guestContext.close();
  });
});
