import path from "path";

/** Path to a seeded user's storageState file, produced by tests/auth.setup.ts. */
export function authFile(username: string): string {
  return path.join(__dirname, "..", ".auth", `${username}.json`);
}
