import { test, expect } from "@playwright/test";
import { uniqueName } from "../../fixtures/test-data";
import { createOrganization } from "../../fixtures/actions";
import { SEEDED_USERS } from "../../fixtures/seeded-users";
import { authFile } from "../../fixtures/auth";

test.use({ storageState: authFile("user1") });

// Uses a throwaway organization created by this spec (not the shared seeded
// CoolOrg/LonelyOrg) so invite/role-change/remove mutations never touch
// shared seed data.
test.describe("organization member management", () => {
  test("invites a member, changes their role once accepted, then removes them", async ({
    page,
    browser,
  }) => {
    const orgName = uniqueName("E2E Member Org");
    const orgUrl = await createOrganization(page, orgName);

    await page.getByRole("button", { name: "Invite member" }).click();
    await page.locator("#orgInviteMember").fill(SEEDED_USERS.user2.username);
    await page.getByText(SEEDED_USERS.user2.username, { exact: true }).click();
    await page.getByRole("button", { name: "Confirm" }).click();
    await expect(page.getByText(/Invitation send to user/)).toBeVisible();

    const memberRow = page.locator("tr", {
      hasText: SEEDED_USERS.user2.username,
    });
    await expect(memberRow.getByText("Invited", { exact: true })).toBeVisible();

    // Accept the invitation as user2, in a separate authenticated context -
    // the inviter (user1) and invitee (user2) need independent sessions.
    const user2Context = await browser.newContext({
      storageState: authFile("user2"),
    });
    const user2Page = await user2Context.newPage();
    await user2Page.goto("/organizations");
    const invitationCard = user2Page
      .locator("simple-card", { hasText: orgName })
      .first();
    await invitationCard
      .getByRole("button", { name: "Accept Invitation" })
      .click();
    await expect(user2Page.getByText("Invitation accepted.")).toBeVisible();
    await user2Context.close();

    // Back as the owner: the accepted member now shows as "Member" with an
    // editable role select (its label text isn't queryable via getByText -
    // <option> text isn't exposed by a collapsed <select> - so assert value
    // instead); change it to "Admin".
    await page.goto(orgUrl);
    const roleSelect = memberRow.locator("select");
    await expect(roleSelect).toHaveValue("member");
    await roleSelect.selectOption("admin");
    await page.getByRole("button", { name: "Confirm" }).click();
    await expect(
      page.getByText(`Successfully changed role of user "${SEEDED_USERS.user2.username}"`),
    ).toBeVisible();
    await expect(roleSelect).toHaveValue("admin");

    // Remove the member entirely.
    await memberRow
      .getByRole("button", { name: "Remove User from Organization" })
      .click();
    await page.getByRole("button", { name: "Confirm" }).click();
    await expect(
      page.getByText(`Successfully removed user "${SEEDED_USERS.user2.username}"`),
    ).toBeVisible();
    await expect(memberRow).not.toBeVisible();
  });
});
