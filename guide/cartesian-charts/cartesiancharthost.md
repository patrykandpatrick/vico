---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/cartesiancharthost
---

# CartesianChartHost

[`CartesianChartHost`][cartesian-chart-host] is the entry point for Cartesian charts. It accepts [`CartesianChart`][cartesian-chart] and [`CartesianChartModelProducer`][cartesian-chart-model-producer] instances, facilitates communication between them, and displays the chart. It also handles scroll and zoom and shows a placeholder when no data is available.

```kt
CartesianChartHost(rememberCartesianChart(/* ... */), modelProducer, /* ... */)
```

You can also use a manually created [`CartesianChartModel`][cartesian-chart-model] instance instead of a model producer:

```kt
CartesianChartHost(rememberCartesianChart(/* ... */), model, /* ... */)
```

## Scroll

`CartesianChartHost` accepts [`VicoScrollState`][vico-scroll-state] instances, enabling scroll customization and programmatic scrolling. Use [`rememberVicoScrollState`][remember-vico-scroll-state] to instantiate `VicoScrollState`:

```kt
val scrollState = rememberVicoScrollState(/* ... */)
CartesianChartHost(scrollState = scrollState, /* ... */)
```

[`Scroll`][scroll] represents scroll values—either absolute (from zero) or relative (from the current value). [`Scroll.Absolute`][scroll-absolute] and [`Scroll.Relative`][scroll-relative] cover the two cases. The following factory functions are available:

* [`Absolute.pixels`][absolute-pixels]
* [`Absolute.x`][absolute-x]
* [`Relative.pixels`][relative-pixels]
* [`Relative.x`][relative-x]

[`AutoScrollCondition`][auto-scroll-condition] defines when to scroll automatically. There are two singletons:

* [`Never`][never] (default)
* [`OnModelGrowth`][on-model-growth]

You can create custom implementations of `Scroll` and `AutoScrollCondition` for more specific behavior.

## Zoom

`CartesianChartHost` accepts [`VicoZoomState`][vico-zoom-state] instances, enabling zoom customization. Use [`rememberVicoZoomState`][remember-vico-zoom-state] to instantiate `VicoZoomState`:

```kt
val zoomState = rememberVicoZoomState(/* ... */)
CartesianChartHost(zoomState = zoomState, /* ... */)
```

[`Zoom`][zoom] defines zoom factors. The following singletons and factory functions are available:

* [`Content`][content]
* [`max`][max]
* [`min`][min]
* [`fixed`][fixed]
* [`x`][x]

You can create custom implementations of `Zoom` for more specific behavior.

[cartesian-chart-host]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart-host.html
[cartesian-chart]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart/
[cartesian-chart-model-producer]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model-producer/
[cartesian-chart-model]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model/
[vico-scroll-state]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-vico-scroll-state/
[remember-vico-scroll-state]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/remember-vico-scroll-state.html
[scroll]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/
[scroll-absolute]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-absolute/
[scroll-relative]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-relative/
[absolute-pixels]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-absolute/-companion/pixels.html
[absolute-x]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-absolute/-companion/x.html
[relative-pixels]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-relative/-companion/pixels.html
[relative-x]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-scroll/-relative/-companion/x.html
[auto-scroll-condition]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-auto-scroll-condition/
[never]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-auto-scroll-condition/-companion/-never.html
[on-model-growth]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-auto-scroll-condition/-companion/-on-model-growth.html
[vico-zoom-state]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-vico-zoom-state/
[remember-vico-zoom-state]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/remember-vico-zoom-state.html
[zoom]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/
[content]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/-content.html
[max]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/max.html
[min]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/min.html
[fixed]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/fixed.html
[x]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-zoom/-companion/x.html
