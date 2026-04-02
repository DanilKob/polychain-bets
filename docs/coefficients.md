# Betting Coefficient Calculation

## Overview

Polychain Bets uses the **parimutuel** (pool betting) model. There are no fixed odds —
coefficients are determined entirely by how much money is bet on each outcome relative
to the total pool. The platform takes a fixed percentage margin from the pool before
distributing winnings.

---

## Core Formula

```
coefficient(outcome_i) = (totalPool × (1 − margin)) / outcomePool_i
```

| Variable | Description |
|---|---|
| `totalPool` | Sum of all coins bet across **all** outcomes |
| `outcomePool_i` | Coins bet on **this specific** outcome |
| `margin` | Platform fee as a fraction, e.g. `0.05` = 5% |

### Example

| Outcome | Pool | Coefficient |
|---|---|---|
| Team A wins | 600 | (1000 × 0.95) / 600 = **1.58x** |
| Team B wins | 400 | (1000 × 0.95) / 400 = **2.38x** |

Total pool = 1000. Platform keeps 50 (5%). 950 is distributed to winners.

A bettor who put 100 coins on Team A receives `100 × 1.58 = 158 coins` back (58 profit).

---

## Edge Cases

### 1. No bets placed yet

When `totalPool = 0` or `outcomePool = 0`, the coefficient cannot be calculated from
real data. We assume **equal probability** across all outcomes and apply the margin:

```
coefficient = outcomeCount × (1 − margin)
```

**Example** — 3 outcomes, 5% margin:

```
coefficient = 3 × 0.95 = 2.85x
```

All outcomes show the same coefficient until the first vote shifts the distribution.

> This is a display value only. It does not affect actual payouts.

---

### 2. All money on one outcome (`losingPool = 0`)

When every bettor picks the same outcome there are no losers to fund the winners.
The parimutuel formula would yield `(1 − margin)` which can be less than `1.00`.

We clamp to `1.00x` as a platform guarantee — bettors always receive at least their
stake back:

```
if (totalPool − outcomePool ≤ 0):
    coefficient = 1.00
```

The platform absorbs the loss in this scenario.

---

### 3. Coefficient floor

Regardless of pool distribution, the coefficient returned is always:

```
coefficient = max(calculated, 1.00)
```

A coefficient below `1.00` would mean a bettor loses money on a winning bet, which
is never allowed.

---

## Platform Margin

The margin is subtracted from the **total pool** before any payout is calculated.
It is not subtracted per-bettor.

```
netPool = totalPool × (1 − margin)
platformRevenue = totalPool × margin
```

**Example** — margin = 5%, totalPool = 10,000 coins:

```
netPool         = 10,000 × 0.95 = 9,500 coins  (distributed to winners)
platformRevenue = 10,000 × 0.05 =   500 coins  (kept by platform)
```

---

## Payout Calculation (at settlement)

When a wager is resolved the **final coefficient is locked** from `WagerStatsEntity`
at the moment `VOTE_CLOSED` is reached. Late votes do not affect settled coefficients.

```
winCoinsAmount = coinCentsAmount × lockedCoefficient
```

These values are written to `WagerOutcomeVoteEntity.winCoinCentsAmount` and
`WagerOutcomeVoteEntity.winCoefficient` during the `FINISH_WINNERS_CALCULATED` phase.

---

## Coefficient Storage

Coefficients are stored in `WagerStatsEntity` as **basis points** (`Long`) to avoid
floating-point precision issues in MongoDB:

```
coefficientBp = round(coefficient × 100)

// Examples
1.85x  →  185
2.38x  →  238
1.00x  →  100
```

The `WagerOutcomeStats.coefficient` property converts back to `Double` for application
code but is never persisted as a floating-point value.

---

## Implementation Reference

| Class | Responsibility |
|---|---|
| `CoefficientService` | Calculates coefficients given pool amounts and margin |
| `WagerStatsEntity` | Stores pre-computed pools, voter counts, and coefficients per wager |
| `WagerOutcomeVoteEntity` | Records individual votes; `winCoefficient` filled at settlement only |