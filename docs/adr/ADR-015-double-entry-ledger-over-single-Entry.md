# ADR-015: Double-Entry Ledger over Single-Entry

## Status
Accepted — Week 2

## Context
Every financial operation in PayFlow — deposit, withdrawal, transfer — must be
recorded in the ledger. The ledger is the source of truth for all balances
(ADR-003). The question is how many ledger entries to create per operation
and what each entry represents.

Two approaches exist: single-entry recording (one row per transaction) and
double-entry bookkeeping (two rows per transaction — one DEBIT, one CREDIT).

## Decision
Every financial operation produces two ledger entries: a DEBIT from the
source and a CREDIT to the destination. `wallet.current_balance` is updated
to reflect the net effect. The ledger is append-only — entries are never
updated or deleted.

For deposits: one CREDIT entry to the receiving wallet.
For withdrawals: one DEBIT entry from the sending wallet.
For transfers: one DEBIT from the source wallet and one CREDIT to the
destination wallet — always in the same transaction.

## Alternatives Considered

**Single-entry ledger**
- One ledger row per transaction recording the amount and direction
- Simpler schema — one INSERT per operation
- Balance derived as `SUM(amount) WHERE type = CREDIT - SUM(amount) WHERE type = DEBIT`
- No structural guarantee that a transfer's debit and credit are balanced —
  a bug could write the debit without the credit, leaving wallets out of sync
  with no way to detect it from the ledger alone
- Reconciliation requires cross-referencing transactions with ledger entries
  rather than verifying ledger balance internally

**Double-entry bookkeeping (chosen)**
- Every transfer produces exactly two entries: DEBIT fromWallet, CREDIT toWallet
- The sum of all DEBIT entries across all wallets always equals the sum of all
  CREDIT entries — a structural invariant that can be verified at any point
- A missing or mismatched entry is immediately detectable: debits ≠ credits
  indicates a bug or data corruption
- `balance_after` snapshot on each entry allows point-in-time balance
  reconstruction without replaying the entire ledger
- Standard accounting practice used in every production financial system —
  auditors and regulators expect it

## Rationale
Double-entry provides a self-verifying ledger. The accounting equation
(debits = credits) is a structural constraint that makes corruption detectable
without external reference. In a payment system where the ledger is the source
of truth, the ability to verify internal consistency is non-negotiable.

Single-entry is simpler but provides no structural guarantee that a transfer's
two sides are balanced. A bug that writes the DEBIT but not the CREDIT would
update `fromWallet.current_balance` correctly but leave `toWallet` unchanged —
money disappears. With double-entry, the missing CREDIT entry is detectable
immediately because debits no longer equal credits across the system.

The `balance_after` field on each entry is a deliberate denormalization that
enables point-in-time balance reconstruction — given any ledger entry, the
wallet balance at that moment is known without summing all prior entries.

## Consequences
- Every transfer produces exactly two `LedgerEntry` rows in the same
  `@Transactional(SERIALIZABLE)` block — atomicity guaranteed by PostgreSQL
- `LedgerService.createDebitEntry()` and `createCreditEntry()` are always
  called together for transfers — never one without the other
- Deposits produce one CREDIT entry only — no source wallet to DEBIT
- Withdrawals produce one DEBIT entry only — no destination wallet to CREDIT
- Nightly reconciliation (`ReconciliationService`, Week 3) verifies that
  total DEBITs equal total CREDITs across all ledger entries — any mismatch
  indicates a bug
- `balance_after` is computed by `LedgerService` as `currentBalance ± amount`
  before `wallet.debit()` / `wallet.credit()` mutates the cached balance —
  the ledger entry is written before the cached balance is updated, consistent
  with ADR-003