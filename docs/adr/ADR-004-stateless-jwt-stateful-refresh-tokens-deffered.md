# ADR-004: Stateless JWT, stateful refresh tokens deferred to Week 3

## Status
Accepted — Week 1 (stateful refresh token implementation deferred to Week 3)

## Context
Authentication requires issuing tokens that prove identity on subsequent
requests. Two separate but related concerns could be used here:
the access token strategy and the refresh token strategy.

Access tokens can be stateless (self-contained, verified by signature alone)
or stateful (verified against a server-side store on every request). Refresh
tokens can be single-use stateful (stored, invalidated on use) or stateless
(long-lived JWTs with no server-side record).

The choice affects security guarantees, infrastructure complexity, and the
ability to revoke tokens without a server-side store.

## Decision
Issue stateless JWTs as access tokens. Refresh tokens are also stateless in
Week 1 — no server-side store. Stateful refresh tokens with a database-backed
revocation mechanism are deferred to Week 3.

## Alternatives Considered

**Stateful access tokens**
- Every request hits a server-side store  to validate the token
- Instant revocation — invalidate the record, token is dead immediately
- Adds a network hop and infrastructure dependency to every authenticated request
- Overkill for access tokens with short TTLs

**Stateless JWT access tokens (chosen)**
- Self-contained — signature verification only, no server-side lookup
- Cannot be revoked before expiry — mitigated by keeping TTL short (15 minutes)
- No infrastructure dependency on the hot authentication path
- Standard approach for access tokens in stateless REST APIs

**Stateful refresh tokens (deferred)**
- Stored in the database, single-use, invalidated on rotation
- Enables logout, token family detection, and revocation
- Correct long-term security posture for a payment system
- Requires a `refresh_tokens` table and rotation logic — deferred to Week 3

## Rationale
Stateless JWTs are the correct choice for access tokens. A 15-minute TTL
limits the damage window of a leaked token without requiring infrastructure
to support revocation on the hot path.

Stateful refresh tokens are the correct long-term choice but introduce
meaningful implementation complexity — token rotation, family invalidation,
and secure storage. Deferring this to Week 3 keeps Week 1 focused on core
authentication flow without compromising the access token security model.

The Week 1 stateless refresh token is a known temporary compromise, not
a permanent decision.

## Consequences
- Access tokens expire in 15 minutes — clients must refresh proactively
- No logout invalidation in Week 1 — tokens remain valid until expiry
- No token revocation on compromise in Week 1 — acceptable given short TTL
- Week 3 introduces a `refresh_tokens` table, single-use rotation, and
  token family invalidation to replace the stateless refresh approach
