# Issue: Compose live chart host — scroll, vsync follow, lifecycle, no-fling

**Title (for `gh issue create`):** Wire live timeline controller to Cartesian chart (Compose)

## Parent PRD

[live-time-series-line-chart.md](./live-time-series-line-chart.md)

## Type

**AFK**

## What to build

End-to-end **Compose** integration (per PRD: **Compose only, no Views**):

- Connect the **timeline follow controller** to **`VicoScrollState`** (or the chart host’s scroll pipeline): apply **target scroll** from the controller on **vsync / frame callbacks** while **following && !paused** and **composition active / visible** (PRD: no background ticking).
- **Programmatic visible duration** via existing **zoom** APIs — **disable user pinch-zoom** for this host/chart mode if the default enables it.
- **Gesture policy:** **horizontal drag only**; **no `animateDecay` / fling** on platforms that currently add it (e.g. desktop/web `Modifier`); Android/iOS paths must **not** introduce fling for this chart. **Drag** must notify controller to **exit follow** immediately.
- **Go live** and **pause follow** exposed to callers (e.g. callbacks or state handle on the remember function).

Deliver a **single obvious entry point** (e.g. `remember…` + composable host or documented modifier chain) so apps do not re-wire internals.

## Acceptance criteria

- [ ] One **documented** public integration API for **live time-series line** charts.
- [ ] While **following** and **not paused**, viewport **smoothly tracks** **`now`** (frame-aligned + deadband).
- [ ] **Pause follow** stops **automatic** scroll; **model updates** from caller still work; returning with **Go live** or threshold recenters as per controller rules.
- [ ] **User drag** → **browsing** immediately; **release** → **no inertial** scroll.
- [ ] **User pinch-zoom disabled** for this mode; **visible span** changeable from code only.
- [ ] Frame-driven updates **stopped** when not active (lifecycle / composition).

## Blocked by

- Controller + tests from **01-live-timeline-follow-controller-and-tests.md** (or the GitHub issue created from it).

## User stories addressed

2, 3, 4, 5, 6, 7, 8, 11, 13, 18
