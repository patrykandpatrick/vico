---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/live-timeline-cartesian-chart
---

# Live timeline Cartesian chart

Use a **live timeline** chart when the horizontal axis is **wall-clock time** (often **epoch milliseconds**) and the viewport should keep the **current instant** in the **horizontal center** while **following**, with **no points past “now”** in the data you pass. For charts that only need to **stick to the latest sample** when the model grows, the usual **scroll-to-end** pattern on [`CartesianChartHost`](cartesiancharthost.md) is simpler.

Compose-only in this version: [`LiveTimelineCartesianChartHost`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-live-timeline-cartesian-chart-host) wires [`CartesianChartHost`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart-host), [`rememberLiveTimelineChartState`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/remember-live-timeline-chart-state), and [`LiveTimelineFollowController`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-live-timeline-follow-controller).

## Modes

* **Follow** — the viewport scrolls on each display frame so the center tracks [`LiveTimelineFollowController.nowProvider`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-live-timeline-follow-controller/now-provider) (subject to a small pixel deadband in [`LiveTimelineFollowConfig`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-live-timeline-follow-config)).
* **Browse** — a horizontal drag switches to browsing immediately; there is **no fling** on desktop or web for this host.
* **Pause follow** — set [`pauseFollow`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-live-timeline-follow-controller/pause-follow) on the controller to stop **automatic** scrolling; you can still update the model, and wall time from `nowProvider` keeps moving.
* **Go live** — [`goLive()`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-live-timeline-follow-controller/go-live) clears pause and recenters `now`.

After browsing, the chart **re-enters follow** when the viewport center time is close enough to `now`, using fractions of the **visible span** with **minimum and maximum caps** in milliseconds and **hysteresis** between inner and outer bands (see `LiveTimelineFollowConfig`).

## `nowProvider`, visible duration, pinch

* **`nowProvider`** — inject a lambda (for tests or server-aligned clocks). The library provides [`defaultLiveTimelineNowProvider`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/default-live-timeline-now-provider) for wall-clock epoch milliseconds on supported targets.
* **Visible duration** — [`rememberLiveTimelineChartState`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/remember-live-timeline-chart-state) takes `visibleDurationMillis` and applies [`Zoom.x`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/x) under the hood. **User pinch-zoom is disabled** for this state (`zoomEnabled` is false on the zoom handle).

## Constraints and data contract

* **Compose only** for this feature in the current version (no Android View equivalent).
* **No user pinch** and **no inertial fling** for the live host, as above.
* **Unbounded history** is not trimmed by the library; holding every sample in memory for a long session can cause **OOM** or heavy drawing. Plan retention or downsampling in the app if needed.
* Prefer samples with **_x_ ≤ `nowProvider()`** so the “no future data” rule holds. If device and data clocks disagree, decide in the app whether to **drop**, **clamp**, or **validate** points; the controller does not alter your model.

## Sample

The Compose sample app includes **Live time-series (simulated)** (`ComposeLiveTimeSeriesChart`), with **pause follow**, **Go live**, and **programmatic window** buttons.

## Basic usage

```kt
val state = rememberLiveTimelineChartState(visibleDurationMillis = 120_000L)
LiveTimelineCartesianChartHost(
  rememberCartesianChart(rememberLineCartesianLayer(), /* axes */),
  modelProducer,
  state,
)
// state.followController.pauseFollow = true
// state.followController.goLive()
```

Frame-driven follow runs **only while the composable is in composition** (stops when the host leaves the tree).
