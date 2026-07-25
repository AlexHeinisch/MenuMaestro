/**
 * Dev-seed accounts/orgs from application/src/main/resources/application.yaml
 * (menumaestro.initial-accounts / menumaestro.initial-organizations, enabled by
 * default). Never mutate these directly in a test - only log in as them and
 * create your own uniquely-named entities underneath (see test-data.ts).
 */
export const SEEDED_USERS = {
  admin: { username: "admin", password: "hallo123" },
  user1: { username: "user1", password: "hallo123" },
  user2: { username: "user2", password: "hallo123" },
  user3: { username: "user3", password: "hallo123" },
  user4: { username: "user4", password: "hallo123" },
} as const;

export const ORGANIZATIONS = {
  coolOrg: "CoolOrg",
  lonelyOrg: "LonelyOrg",
} as const;
