import { test, expect } from "@playwright/test";
import { uniqueName } from "../../fixtures/test-data";
import { addMealToMenu, createMenu, createRecipe } from "../../fixtures/actions";
import { ORGANIZATIONS } from "../../fixtures/seeded-users";
import { authFile } from "../../fixtures/auth";

test.use({ storageState: authFile("user1") });

// user2 is seeded as a plain Member of CoolOrg (see application.yaml's
// menumaestro.initial-organizations) - a lower-permission role than user1's
// Admin. These assertions are read-only against a menu created by this spec,
// so CoolOrg's own seed data is never mutated.
test.describe("permission restrictions for lower-privilege users", () => {
  test("a CoolOrg Member cannot see admin-only organization or menu actions", async ({
    page,
    browser,
  }) => {
    const menuName = uniqueName("E2E Permission Menu");
    const recipeName = uniqueName("E2E Permission Recipe");

    await createMenu(page, menuName, ORGANIZATIONS.coolOrg);
    const menuUrl = page.url();
    await createRecipe(page, recipeName);
    await addMealToMenu(page, ORGANIZATIONS.coolOrg, menuName);

    const user2Context = await browser.newContext({
      storageState: authFile("user2"),
    });
    const user2Page = await user2Context.newPage();

    // Organization: user2 is only a Member of CoolOrg, not Admin/Owner.
    await user2Page.goto("/organizations");
    await user2Page.getByText(ORGANIZATIONS.coolOrg, { exact: true }).click();
    await expect(
      user2Page.getByRole("heading", { name: ORGANIZATIONS.coolOrg }),
    ).toBeVisible();
    await expect(
      user2Page.getByRole("button", { name: "Invite member" }),
    ).toHaveCount(0);
    await expect(user2Page.getByRole("button", { name: "Edit" })).toHaveCount(
      0,
    );
    await expect(
      user2Page.getByRole("button", { name: "Delete Organization" }),
    ).toHaveCount(0);

    // Menu: user2 can view it (Member has read access) but not administer it.
    await user2Page.goto(menuUrl);
    await expect(user2Page.getByText(menuName)).toBeVisible();
    await expect(
      user2Page.getByRole("button", { name: "Delete Menu" }),
    ).toHaveCount(0);
    await expect(
      user2Page.getByRole("button", { name: "Close Menu" }),
    ).toHaveCount(0);
    await expect(
      user2Page.getByRole("button", { name: "View Stash" }),
    ).toHaveCount(0);
    await expect(
      user2Page.locator("button", { hasText: "Meals Separator" }),
    ).toHaveCount(0);

    await user2Context.close();
  });
});
