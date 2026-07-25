import { test, expect } from "@playwright/test";
import { uniqueName } from "../../fixtures/test-data";
import { createRecipe } from "../../fixtures/actions";
import { authFile } from "../../fixtures/auth";

test.use({ storageState: authFile("user1") });

test.describe("recipe visibility", () => {
  test("a private recipe is hidden from a user outside the author's organizations, a public one is not", async ({
    page,
    browser,
  }) => {
    const privateRecipeName = uniqueName("E2E Private Recipe");
    const publicRecipeName = uniqueName("E2E Public Recipe");

    await createRecipe(page, privateRecipeName, "PRIVATE");
    const privateRecipeUrl = page.url();

    await createRecipe(page, publicRecipeName, "PUBLIC");
    const publicRecipeUrl = page.url();

    // user4 (LonelyOrg's owner) shares no organization with user1 (author of
    // both recipes, a CoolOrg member), so it can't fall back to
    // Organization-visibility access either.
    const outsiderContext = await browser.newContext({
      storageState: authFile("user4"),
    });
    const outsiderPage = await outsiderContext.newPage();

    await outsiderPage.goto(privateRecipeUrl);
    await expect(
      outsiderPage.getByText("Oops, this recipe doesn't exist."),
    ).toBeVisible();

    await outsiderPage.goto(publicRecipeUrl);
    await expect(
      outsiderPage.getByRole("heading", { name: publicRecipeName }),
    ).toBeVisible();

    await outsiderContext.close();
  });
});
