# Issue: Live timeline follow controller + unit tests

**Title (for `gh issue create`):** Add live timeline follow controller with unit tests

## Parent PRD

[live-time-series-line-chart.md](./live-time-series-line-chart.md)  
*(Optional: link a parent GitHub tracking issue here when created.)*

## Type

**AFK** — implementable without external sign-off.

## What to build

Introduce a **deep, test-first module** (package TBD under `vico/compose` or shared Kotlin source set) that encapsulates **time ↔ scroll** mapping for a **fixed visible duration (ms)** centered on **`nowProvider()`**, plus the **state machine**:

- Modes: **following** vs **browsing** (user drag exits follow immediately).
- **Pause follow**: stops programmatic scroll updates; does not affect **`now`** or data ingestion (caller-side).
- **Go live**: compute scroll so **`now`** is at horizontal center.
- **Re-enter follow** when viewport center time is within threshold of **`now`**: **fraction of visible span** with **ms floor/ceiling** and **hysteresis** (enter vs exit).
- **Sub-pixel deadband** (~½ px) before emitting scroll deltas.

Expose a **narrow public API** suitable for Compose to drive **`VicoScrollState`** (or equivalent): e.g. inputs = **scroll value, max scroll, bounds/layer dimensions**, outputs = **target scroll** or **apply** callback — exact shape is up to implementation but **keep pixel math inside**.

Inject **`nowProvider: () -> Long`** (default system epoch ms where the factory runs).

Cover **edge cases** from the PRD: **fixed window** when little history (empty left OK), **x ≤ now** invariant documented; pick one **skew** behavior (filter, clamp, or assert) and document in KDoc.

## Acceptance criteria

- [ ] Public API with **explicit visibility** on all exported declarations.
- [ ] **`nowProvider`** injectable; default uses **epoch milliseconds** on supported targets.
- [ ] Unit tests for: center-time from scroll, inverse mapping, **Go live** scroll target, **threshold + hysteresis** enter/exit follow, **deadband**, mode transitions (**drag → browsing** simulated as API calls), **pause follow** gating of “should apply follow scroll.”
- [ ] No dependency on Android View framework; **Compose-ready** from common/Kotlin tests.

## Blocked by

**None — can start immediately.**

## User stories addressed

1, 2, 3, 4, 6, 8, 9, 10, 12, 15, 16, 17, 19, 20
