# PRD: Live time-series line chart (epoch X, center = now)

## GitHub issue

**Title:** Add live Cartesian line chart with center-anchored “now,” follow/browse modes, and programmatic zoom

**Body (paste below):**

---

## Problem Statement

Consumers need a **line chart whose horizontal axis is time in epoch milliseconds**, optimized for **live streams**: the viewport should **stay smoothly aligned so the current instant (`now`) remains at the horizontal center**, with **no data past `now`**. They also need to **pause automatic following** (without stopping ingestion), **inspect history** by dragging, **return to live** explicitly or by scrolling near `now`**, and **change the visible time window in code** (no pinch-zoom). Today, Vico’s scrolling model is **pixel-based** with patterns like scroll-to-end on model growth—not a **time-centered live timeline** with a small **state machine** (follow vs browse, pause vs follow-active).

## Solution

Provide a **small public API** (Compose Multiplatform) that composes existing **Cartesian chart, scroll, and zoom** building blocks with a **single controller** responsible for:

- Mapping **visible duration in ms** ↔ **scroll position** so **chart horizontal center ↔ `nowProvider()`**.
- Running a **vsync-aligned** update loop **only while following and not paused** and while the chart is **active**, with a **sub-pixel deadband** to avoid useless scroll updates.
- **Pausing follow**: stop programmatic scroll updates; **model updates continue** so new points accumulate off-screen if the user has panned away.
- **Browsing**: any **horizontal drag** immediately **exits follow**; **no fling / inertial** scrolling.
- **Go live**: jump scroll so **`now` is centered** again (optionally animated when the jump is large—implementation detail).
- **Re-entering follow** when the user scrolls **close to** centering `now`**, using **thresholds expressed as a fraction of the visible time span** with **minimum and maximum caps in milliseconds** and **hysteresis** between entering and exiting follow.

Callers supply **`nowProvider`**, defaulting to **system epoch millis** on supported platforms.

## User Stories

1. As an app developer, I want a **documented live timeline chart API**, so that I can ship streaming metrics without reimplementing scroll/time math.
2. As an end user, I want **`now` to stay at the center** while the chart is **following**, so that I can read “past on the left, empty future on the right” consistently.
3. As an end user, I want **smooth horizontal motion** as real time advances, so that the chart does not only move when new points arrive.
4. As an end user, I want **pause follow** to stop the **automatic centering** but **still show new data** when I return to live or pan, so that I can step away without losing samples.
5. As an end user, I want **drag** to **pan through history**, so that I can inspect earlier values.
6. As an end user, I want **drag** to **immediately stop live follow**, so that the chart never fights my intent while I explore.
7. As an end user, I want **no fling** after I release a drag, so that history browsing feels precise and predictable.
8. As an end user, I want **Go live**, so that I can **instantly** re-center on the current **`now`** after browsing or pausing follow.
9. As an end user, I want the chart to **re-enter follow** when I scroll **near** `now`**, so that I do not have to hit the exact center.
10. As an app developer, I want **hysteresis** on follow thresholds, so that follow mode does not flicker at the boundary.
11. As an app developer, I want **visible window width in time** to be set **programmatically**, so that zoom is under app control (no user pinch-zoom on this chart).
12. As an app developer, I want **`nowProvider` injectable**, so that tests are deterministic and server-aligned clocks are possible.
13. As an app developer, I want to use compose to keep up with the latest tech.
14. As an app developer, I want samples or demos showing **pause**, **Go live**, **browse**, and **programmatic window changes**, so that integration is obvious.
15. As an end user with **little history yet**, I want a **fixed-duration window centered on `now`**, so that the **left side may be empty** early on rather than rescaling oddly (v1 simplicity).
16. As an app developer, I accept **unbounded in-memory history in v1**, so that implementation stays simple (document **OOM / performance risk**).
17. As an app developer, I want **default Y-axis behavior** unchanged from existing Cartesian line charts in v1, so that I do not need custom vertical scaling rules yet.
18. As an app developer, I want the chart to **stop frame-driven follow updates** when **not visible / inactive**, so that battery and CPU are not wasted in background.
19. As an app developer, I want **only samples with x ≤ now** to be valid for this mode, so that the “no future data” invariant holds (exact handling of **clock skew** vs sample timestamps may be **filter, clamp, or assert**—see notes).
20. As a maintainer, I want **automated tests** for **threshold / hysteresis** and **mode transitions** where feasible without a device, so that regressions are caught early.

## Implementation Decisions

### Modules to build or modify

- **New deep module (public API):** a **timeline follow controller** (names TBD) that owns **mode** (following vs browsing), **follow-paused** flag, **`nowProvider`**, **visible duration (ms)**, **threshold parameters** (fraction of span + ms floor/ceiling + hysteresis), and **frame-driven** scroll updates with **deadband**. It should expose a **narrow surface** to hosts: e.g. observe **layout bounds / layer dimensions / max scroll** from existing measuring hooks, compute **target scroll**, and apply via existing **scroll APIs**.
- **Compose integration:** extend or wrap **`CartesianChartHost`** (or equivalent) so this chart type can use a **dedicated scroll/gesture path**: **drag without decay** on platforms that currently attach **fling** via **`animateDecay`** in desktop/web `Modifier` implementations.
- **No views integration:** The functionality is only for compose
- **Zoom:** reuse existing **programmatic zoom** facilities to implement **visible duration** changes; **no additional user pinch** for v1 beyond what global chart settings already do—this chart type should **disable** user pinch if the default host enables it.
- **Samples:** add **Compose** examples demonstrating **live updates**, **pause follow**, **Go live**, **browse**, and **programmatic window resize**.

### Public interfaces

- New **public** types for the **live timeline** controller and **factory/remember** functions mirroring existing scroll state patterns.
- **Explicit API mode** preserved (visibility modifiers on all public declarations).
- **Binary compatibility:** treated as **internal distribution** but still **public API**—follow normal deprecation discipline if iterating.

### Architecture

- **Separation of concerns:** **data path** (append/replace `CartesianChartModel`) stays caller-driven; **controller** maps **time domain** to **scroll** and **gesture policy**. Avoid leaking **pixel math** to callers.
- **Order of transforms:** align with how **zoom** and **scroll** already compose in Vico so **changing visible duration** and **centering `now`** do not fight each other.
- **Lifecycle:** frame loop **must not** run when **inactive**; respect platform **lifecycle** or Compose **composition** cancellation.

### Technical clarifications

- **Pause** means **pause programmatic follow updates** only; **wall `now` and model updates** continue.
- **Follow re-entry** uses **time at viewport center** derived from **scroll + visible mapping**, compared to **`nowProvider()`**, with **fraction-of-span** thresholds and **ms** floor/ceiling and **hysteresis**.
- **Sub-pixel deadband** skips scroll application when the delta maps below ~½ pixel.
- **v1:** **no eviction** of old points; **no custom Y constraints**.

### API contracts

- **`nowProvider: () -> Long`**, default **system epoch milliseconds** where applicable.
- Callers responsible for **x ≤ now** semantics for samples; library may **document** recommended validation for **skewed** clocks.

## Testing Decisions

- **Unit-test** the **pure** pieces: mapping between **scroll**, **visible span**, **center time**, and **`now`**, including **threshold enter/exit** with **hysteresis** and **deadband** (no Robolectric required for this core).
- **Platform tests** (where the repo already patterns them) optional for **gesture** (no fling) on Views; Compose desktop/web **drag-release** without decay may need **UI or integration** tests if feasible.
- **Avoid** asserting internal coroutine or frame scheduler details; assert **observable scroll behavior** and **mode** transitions given controlled **`nowProvider`** and fake dimensions.

## Out of Scope (v1)

- **Dropping, downsampling, or compressing** historical points.
- **Custom Y-axis** rules (e.g. visible-window autoscale only).
- **User pinch-zoom** for this chart type.
- **Fling / inertial** scroll.
- **Multiple series**-specific semantics (a single line is enough unless existing model is already multi-series—then document parity but no new legend requirements).
- **Accessibility** announcements for live/paused (follow-up).
- **Server–client clock skew policy** beyond documentation and a single chosen behavior in implementation.

## Further Notes

- **Risks:** **Unbounded memory** and **pathological draw cost** for long sessions; **frame-driven scroll** cost if deadband is ineffective.
- **Open implementation choice:** behavior when **`x > nowProvider()`** (drop sample, clamp, or debug assert)—should be **consistent** and **documented**.
- **Existing code reality:** Compose **desktop/web** use **`draggable` + `animateDecay`** for scroll; Views use **`OverScroller.fling`** on pointer up—**disabling fling** is a **deliberate change** for this chart mode, not the default for all charts.

---

## PRD (full document reference)

This file is the canonical PRD; the section above is suitable for GitHub issue creation.

### Codebase exploration summary (evidence)

- **Scroll state:** `VicoScrollState` (Compose) already exposes **programmatic scroll**, **max scroll**, and optional **auto-scroll on model change**—live follow is **orthogonal** and likely **custom** rather than `AutoScrollCondition` alone.
- **Gestures:** Horizontal **fling** exists on **Compose desktop/web** (`animateDecay` after drag). **Android/iOS** `extraPointerInput` may differ; integration must account for **per-platform** gesture wiring.
- **Samples:** Charts such as **Temperature Anomalies** demonstrate **`Scroll.Absolute.End`** patterns—not **time-centered** follow.

### Proposed major modules (deep vs shallow)

| Module | Responsibility | Depth |
|--------|----------------|--------|
| **Timeline follow controller** | Time ↔ scroll mapping, modes, pause, thresholds, hysteresis, deadband, frame tick gating | **Deep**—primary complexity here |
| **Compose host wiring** | Connect controller to `VicoScrollState`, lifecycle, optional animation on Go live; **no-fling** drag | Shallow adapter |
| **Sample apps** | End-to-end demo | Shallow |

### Test scope alignment

- **Heavy unit tests:** timeline follow controller (pure logic + fake measuring inputs).
- **Lighter / optional:** one integration test per stack proving **no fling** if the repo’s infrastructure allows.
