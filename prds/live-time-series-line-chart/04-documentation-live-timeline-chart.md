# Issue: Documentation — live timeline chart (Compose)

**Title (for `gh issue create`):** Document live time-series Cartesian chart API

## Parent PRD

[live-time-series-line-chart.md](../live-time-series-line-chart.md)

## Type

**AFK**

## What to build

Add or extend **guide** documentation (follow **AGENTS.md** documentation rules: link first mentions to API reference, avoid nounified code names, etc.) covering:

- When to use the **live timeline** chart vs standard **scroll-to-end** patterns.
- **Follow / browse / pause follow / Go live** semantics.
- **`nowProvider`**, **visible duration**, **thresholds** (fraction + ms caps + hysteresis).
- **Constraints:** **Compose only** for this feature in v1; **no user pinch**; **no fling**; **unbounded history** risk.
- **Data contract:** **x ≤ now**; **clock skew** note aligned with implemented behavior.

## Acceptance criteria

- [ ] Guide page(s) published in the same doc system as existing Vico guides.
- [ ] First mention of public API types links to **API reference**.
- [ ] Cross-links to the **Compose sample** added in slice 03.

## Blocked by

- **02-compose-live-chart-host-integration.md** (API names and entry points must be stable enough to document — minor KDoc tweaks after merge OK).

## User stories addressed

1, 14 (integration discoverability), 19 (document skew), 16 (risk callout)
