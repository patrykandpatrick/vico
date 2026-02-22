---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/cartesiancharthost
---

# CartesianChartHost

## Overview

[`CartesianChartHost`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart-host) is the entry point for Cartesian charts. It accepts a `CartesianChart` and a `CartesianChartModelProducer`, facilitates their communication, and displays the `CartesianChart`. It also handles scroll and zoom and shows a placeholder when no data is available.

```kt
CartesianChartHost(rememberCartesianChart(/* ... */), modelProducer, /* ... */)
```

## Scroll

`CartesianChartHost` accepts instances of [`VicoScrollState`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-vico-scroll-state/), enabling scroll customization and programmatic scrolling. Use [`rememberVicoScrollState`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/remember-vico-scroll-state) to instantiate this class:

```kt
val scrollState = rememberVicoScrollState(/* ... */)
CartesianChartHost(scrollState = scrollState, /* ... */)
```

[`Scroll`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/), split into [`Scroll.Absolute`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-absolute/) and [`Scroll.Relative`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-relative/), is used to represent scroll values—either absolute (from zero) or relative (from the current value). The following factory functions are available:

* [`Scroll.Absolute.pixels`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-absolute/-companion/pixels)
* [`Scroll.Absolute.x`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-absolute/-companion/x)
* [`Scroll.Relative.pixels`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-relative/-companion/pixels)
* [`Scroll.Relative.x`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-relative/-companion/x)

[`AutoScrollCondition`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-auto-scroll-condition/) defines when to scroll automatically. There are two singletons:

* [`AutoScrollCondition.Never`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-auto-scroll-condition/-companion/-never) (default)
* [`AutoScrollCondition.OnModelGrowth`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-auto-scroll-condition/-companion/-on-model-growth)

You can create custom implementations of `Scroll` and `AutoScrollCondition` for more specific behavior.

## Zoom

`CartesianChartHost` accepts instances of [`VicoZoomState`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-vico-zoom-state/), enabling zoom customization. Use [`rememberVicoZoomState`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/remember-vico-zoom-state) to instantiate this class:

```kt
val zoomState = rememberVicoZoomState(/* ... */)
CartesianChartHost(zoomState = zoomState, /* ... */)
```

[`Zoom`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/) is used to define zoom factors. The following singletons and factory functions are available:

* [`Zoom.Content`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/-content)
* [`Zoom.max`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/max)
* [`Zoom.min`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/min)
* [`Zoom.fixed`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/fixed)
* [`Zoom.x`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/x)

You can create custom implementations of `Zoom` for more specific behavior.
