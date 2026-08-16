---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/cartesiancharthost
---

# CartesianChartHost

[`CartesianChartHost`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart-host.html) is the entry point for Cartesian charts. It accepts [`CartesianChart`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart/) and [`CartesianChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model-producer/) instances, facilitates communication between them, and displays the chart. It also handles scroll and zoom and shows a placeholder when no data is available.

```kt
CartesianChartHost(rememberCartesianChart(/* ... */), modelProducer, /* ... */)
```

You can also use a manually created [`CartesianChartModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model/) instance instead of a model producer:

```kt
CartesianChartHost(rememberCartesianChart(/* ... */), model, /* ... */)
```

## Scroll

`CartesianChartHost` accepts [`VicoScrollState`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-vico-scroll-state/) instances, enabling scroll customization and programmatic scrolling. Use [`rememberVicoScrollState`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/remember-vico-scroll-state.html) to instantiate `VicoScrollState`:

```kt
val scrollState = rememberVicoScrollState(/* ... */)
CartesianChartHost(scrollState = scrollState, /* ... */)
```

[`Scroll`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/) represents scroll values—either absolute (from zero) or relative (from the current value). [`Scroll.Absolute`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-absolute/) and [`Scroll.Relative`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-relative/) cover the two cases. The following factory functions are available:

* [`Absolute.pixels`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-absolute/-companion/pixels.html)
* [`Absolute.x`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-absolute/-companion/x.html)
* [`Relative.pixels`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-relative/-companion/pixels.html)
* [`Relative.x`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-relative/-companion/x.html)

[`AutoScrollCondition`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-auto-scroll-condition/) defines when to scroll automatically. There are two singletons:

* [`Never`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-auto-scroll-condition/-companion/-never.html) (default)
* [`OnModelGrowth`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-auto-scroll-condition/-companion/-on-model-growth.html)

You can create custom implementations of `Scroll` and `AutoScrollCondition` for more specific behavior.

## Zoom

`CartesianChartHost` accepts [`VicoZoomState`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-vico-zoom-state/) instances, enabling zoom customization. Use [`rememberVicoZoomState`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/remember-vico-zoom-state.html) to instantiate `VicoZoomState`:

```kt
val zoomState = rememberVicoZoomState(/* ... */)
CartesianChartHost(zoomState = zoomState, /* ... */)
```

[`Zoom`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/) defines zoom factors. The following singletons and factory functions are available:

* [`Content`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/-content.html)
* [`max`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/max.html)
* [`min`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/min.html)
* [`fixed`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/fixed.html)
* [`x`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/x.html)

You can create custom implementations of `Zoom` for more specific behavior.
