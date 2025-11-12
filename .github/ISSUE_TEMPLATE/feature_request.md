## Title
Add user email verification flow

## Context
Currently users can sign up without verifying their email address, leading to 
fake accounts. Need to implement email verification following our existing 
auth patterns (see /auth/magic-link.ts as reference).

## Acceptance Criteria
- [ ] Send verification email on signup
- [ ] Email contains time-limited verification link (24h expiry)
- [ ] Clicking link marks email as verified in database
- [ ] Unverified users see banner prompting verification
- [ ] Resend verification email option available

## Technical Details
Files to modify:
- /api/auth/signup.ts
- /api/auth/verify-email.ts (new)
- /components/VerificationBanner.tsx (new)

Follow pattern from: /auth/magic-link.ts

## Testing
- Unit tests for email sending
- Integration test for full verification flow
- Test expired link behavior
- Test already-verified user scenario
