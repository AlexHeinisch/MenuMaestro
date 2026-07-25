import { Page, expect } from "@playwright/test";
import { uniqueName } from "./test-data";

/** One of the default ingredients seeded via application/src/main/resources/data/default_ingredients.json. */
export const SEEDED_INGREDIENT = "Apple";

/**
 * Picks an option by exact text from a `search-input` component's dropdown
 * (see frontend/src/app/components/Input/SearchInput.ts). Options render as
 * plain <li> text with no distinguishing attribute, and some search-inputs
 * (ingredients) also render an 'Add "<term>"' custom option once a term is
 * typed - an exact-text match both avoids that collision and auto-waits past
 * the search's debounce, since Playwright's exact text matching normalizes
 * whitespace but still requires an exact (not substring) match.
 */
async function selectSearchOption(page: Page, exactText: string): Promise<void> {
  await page.getByText(exactText, { exact: true }).click();
}

/** Creates a minimal valid recipe via the UI and leaves the browser on its detail page. */
export async function createRecipe(
  page: Page,
  name: string,
  visibility?: "PUBLIC" | "PRIVATE" | "ORGANIZATION",
): Promise<void> {
  await page.goto("/recipes/create");
  await page.locator("#recipeName").fill(name);
  await page.locator("#servings").fill("4");
  if (visibility) {
    await page.locator("#visibility").selectOption(visibility);
  }

  await page.locator("#ingredient0").fill(SEEDED_INGREDIENT);
  await selectSearchOption(page, SEEDED_INGREDIENT);

  await page
    .locator("textarea")
    .fill("Peel and slice the ingredients, then serve.");

  await page.getByRole("button", { name: "Submit" }).click();
  await expect(page).toHaveURL(/\/recipes\/\d+$/);
}

/** Creates a menu in the given organization via the create-menu modal and leaves the browser on its detail page. */
export async function createMenu(
  page: Page,
  name: string,
  organization: string,
  numberOfPeople = 4,
): Promise<void> {
  await page.goto("/menus");
  await page.getByRole("button", { name: "Menu" }).click();

  await page.locator("#menu-name").fill(name);
  await page.locator("#menu-n-people").fill(String(numberOfPeople));
  await page.locator("#organization").fill(organization);
  await selectSearchOption(page, organization);

  await page.getByRole("button", { name: "Submit" }).click();
  await expect(page).toHaveURL(/\/menus\/\d+$/);
}

/**
 * Adds the recipe currently open on a recipe-detail page to a menu via the
 * "Add to Menu" modal. Must be called while `page` is on that recipe's
 * detail page; navigates to /recipes on success (the app's own behavior).
 */
export async function addMealToMenu(
  page: Page,
  organization: string,
  menuName: string,
): Promise<void> {
  await page.getByRole("button", { name: "Add to Menu" }).click();

  await page.locator("#organization").fill(organization);
  await selectSearchOption(page, organization);

  // The <option> label is reformatted for display (e.g. "E2E" -> "E2e"), but
  // its value attribute keeps the exact name - select by value, not label.
  await page.locator("#menuOptions").selectOption({ value: menuName });
  await page.getByRole("button", { name: "Confirm" }).click();
  await expect(page).toHaveURL(/\/recipes$/);
}

/** Creates an organization via the organizations overview modal and leaves the browser on its detail page. Returns that page's URL. */
export async function createOrganization(
  page: Page,
  name: string,
): Promise<string> {
  await page.goto("/organizations");
  await page.getByRole("button", { name: "Organization" }).click();

  // No [id] on this field, so its accessible name falls back to its
  // placeholder ("e.g. TU Wien") rather than its (unrelated) name="menu-name"
  // attribute, a copy/paste leftover from the menu-create modal.
  await page.getByRole("textbox", { name: "e.g. TU Wien" }).fill(name);

  // Unlike menu-create, this modal doesn't navigate anywhere on success, and
  // the overview list is paginated (5/page, oldest first) with no name
  // filter - a freshly created org can easily fall off page 1 once many
  // organizations exist. Read the new org's id straight from the POST
  // response instead of hunting for it in the list.
  const [response] = await Promise.all([
    page.waitForResponse(
      (res) =>
        res.request().method() === "POST" &&
        /\/organizations$/.test(new URL(res.url()).pathname),
    ),
    page.getByRole("button", { name: "Submit" }).click(),
  ]);
  const { id } = await response.json();

  await page.goto(new URL(`/ui/organizations/${id}`, page.url()).toString());
  await expect(page).toHaveURL(/\/organizations\/\d+$/);
  return page.url();
}

/**
 * Adds a meals-separator group to every meal currently on a menu, then
 * generates a shopping list from the (only) resulting snapshot group. Must
 * be called while `page` is on that menu's detail page with at least one
 * meal already added. Returns the shopping list's detail page URL.
 */
export async function createShoppingListFromMenu(
  page: Page,
  menuUrl: string,
  shoppingListName: string,
): Promise<string> {
  await page.goto(menuUrl);
  await page.locator("button", { hasText: "Meals Separator" }).click();
  await page.locator("#mealSeparatorName").fill(uniqueName("Group"));
  await page.getByRole("button", { name: "Confirm" }).click();

  await page.getByTestId("create-shopping-list-from-snapshot").click();
  await page.locator("#shoppingListName").fill(shoppingListName);
  await page.locator("#selectAll").check();
  await page.getByRole("button", { name: "Next" }).click();
  await page.getByRole("button", { name: "Submit" }).click();

  await expect(page).toHaveURL(/\/shopping-lists\/\d+$/);
  return page.url();
}
