# ADR-005: HS256 over RS256 for JWT signing

## Status
Accepted — Week 1

## Context
JWTs must be signed to prevent tampering. There are two mainstream signing strategies
that are HMAC-based symmetric signing (HS256) and RSA-based asymmetric signing
(RS256). The choice affects key management complexity, verification architecture,
and operational overhead.

## Decision
Use HS256 — a shared secret signs and verifies all tokens. A single secret
key is configured via an environment variable.

## Alternatives Considered

**RS256 (asymmetric)**
- Private key signs tokens, public key verifies them
- Public key can be distributed freely — enables external services to verify
  tokens without access to the signing secret
- Necessary in multiservice architectures where different services need to
  verify tokens independently
- Requires key pair generation, rotation strategy, and JWTS endpoint exposure
- Significantly more operational complexity for no benefit in a monolith

**HS256 (chosen)**
- Single shared secret signs and verifies tokens
- Simple key management — one environment variable
- Verification requires access to the secret — acceptable when only one service
  verifies tokens
- Standard choice for monolithic architectures

## Rationale
RS256 earns its complexity in distributed systems where multiple independent
services verify tokens without sharing a secret. PayFlow is a monolith — the
same service that issues tokens also verifies them. There is no external
consumer of the JWT that needs the public key.

HS256 with a strong secret and short token TTL (see ADR-005) provides
adequate security for this architecture with significantly lower operational
overhead.

If PayFlow evolves into a multiservice architecture where separate services
need to verify tokens independently, migrating to RS256 with a JWTS endpoint
is the natural next step.

## Consequences
- JWT signing and verification happen within the same service — secret never
  leaves the backend
- Secret must be sufficiently long and random — enforced via environment
  variable, never hardcoded
- Key rotation requires reissuing all active tokens — acceptable given the
  15-minute access token TTL from ADR-005
- Migration to RS256 is straightforward if the architecture becomes
  multiservice