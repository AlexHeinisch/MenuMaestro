/**
 * Generates a name that's unique per test run/worker, so specs can safely
 * create their own entities against the shared seeded Postgres instance
 * without colliding with previous runs or parallel workers.
 */
export function uniqueName(prefix: string): string {
  return `${prefix} ${Date.now()}-${Math.floor(Math.random() * 100000)}`;
}
