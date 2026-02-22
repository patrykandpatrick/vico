---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/core/cartesian-charts/cartesianchart
---

# CartesianChart

## Anatomy

[`CartesianChart`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart/) draws charts based on Cartesian coordinate planes. Any such chart includes one or more [`CartesianLayer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.layer/-cartesian-layer/)s. The `CartesianLayer`s are stacked, and each one has its own data.

## Hosts

A `CartesianChart` is used with a host—a composable or view that passes it data, draws it, and handles framework-specific matters, such as gesture detection.

Learn about the hosts [for Compose](/broken/pages/7XZxRPhJwQ5aNcX0pxT1#host) and [for views](/broken/pages/J4GNY8nSjqueSsO7u6Wh#host).

## Ranges

A `CartesianChart`’s _x_- and _y_-ranges depend on those reported by its `CartesianLayer`s. The _x_-range is the narrowest one that includes all `CartesianLayer`s’ _x_-ranges. By default, there’s an analogously determined shared _y_-range, but two separate _y_-ranges can also be introduced.

_x_- and _y_-range customization is discussed in [a later subsection](cartesianlayer.md#cartesianlayerrangeprovider).

## Scroll and zoom

While scroll and zoom are handled at the framework level, `core` includes some shared high-level contracts.

[`Scroll`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/), split into [`Scroll.Absolute`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-absolute/) and [`Scroll.Relative`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-relative/), is used to represent scroll values—either absolute (from zero) or relative (from the current value). The following factory functions are available:

* [`Scroll.Absolute.pixels`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-absolute/-companion/pixels)
* [`Scroll.Absolute.x`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-absolute/-companion/x)
* [`Scroll.Relative.pixels`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-relative/-companion/pixels)
* [`Scroll.Relative.x`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-relative/-companion/x)

[`AutoScrollCondition`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-auto-scroll-condition/) defines when to scroll automatically. There are two singletons:

* [`AutoScrollCondition.Never`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-auto-scroll-condition/-companion/-never) (default)
* [`AutoScrollCondition.OnModelGrowth`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-auto-scroll-condition/-companion/-on-model-growth)

[`Zoom`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/) is used to define zoom factors. The following singletons and factory functions are available:

* [`Zoom.Content`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/-companion/-content)
* [`Zoom.max`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/-companion/max)
* [`Zoom.min`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/-companion/min)
* [`Zoom.fixed`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/-companion/fixed)
* [`Zoom.x`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/-companion/x)

While the built-in implementations cover the majority of use cases, you can create your own for advanced behavior.

## Sample charts

* [“Basic column chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/BasicColumnChart.kt)
* [“Basic line chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/BasicLineChart.kt)
* [“Basic combo chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/BasicComboChart.kt)
* [“AI test scores”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/AITestScores.kt)
* [“Daily digital-media use (USA)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/DailyDigitalMediaUse.kt)
* [“Temperature anomalies (June)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/TemperatureAnomalies.kt)
* [“Electric-car sales (Norway)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/ElectricCarSales.kt)
* [“Rock–metal ratios”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/RockMetalRatios.kt)
* [“Gold prices (12/30/2024)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/GoldPrices.kt)
