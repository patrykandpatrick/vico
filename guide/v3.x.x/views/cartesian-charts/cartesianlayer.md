---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/core/cartesian-charts/cartesianlayer
---

# CartesianLayer

## Implementations

There are three built-in [`CartesianLayer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.layer/-cartesian-layer/) implementations:

* [`CandlestickCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.layer/-candlestick-cartesian-layer/)
* [`ColumnCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.layer/-column-cartesian-layer/)
* [`LineCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.layer/-line-cartesian-layer/)

These are discussed individually in the following sections.

## [`CartesianLayerRangeProvider`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-layer-range-provider/)

What _x_- and _y_-ranges a `CartesianLayer` reports depends on its `CartesianLayerRangeProvider`. The `CartesianLayer` passes its intrinsic _x_- and _y_-ranges—which depend on the [`CartesianLayerModel`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-layer-model/)—to the `CartesianLayerRangeProvider`, which returns the final ranges for the `CartesianLayer` to report. The available singletons and factory functions are listed below. For more specific behavior, implement the interface.

* [`CartesianLayerRangeProvider.auto`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-layer-range-provider/-companion/auto) (default)
* [`CartesianLayerRangeProvider.fixed`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-layer-range-provider/-companion/fixed)
* [`CartesianLayerRangeProvider.Intrinsic`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-layer-range-provider/-companion/-intrinsic)

The default implementations of the `CartesianLayerRangeProvider` functions leave the _x_-range unchanged but do these two things:

* They ensure that the _y_-range includes zero.
* They apply a _y_-range of \[0, 1] if the minimum and maximum intrinsic _y_-values are both zero.

This also applies to the `CartesianLayerRangeProvider` implementation returned by `CartesianLayerRangeProvider.auto`. Custom `CartesianLayerRangeProvider` implementations can override this behavior. With `CartesianLayerRangeProvider.fixed`, `minY` and `maxY` take precedence.

When using [`CartesianChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/), set each `CartesianLayer`’s `CartesianLayerRangeProvider` only once. There are no restrictions on dynamic behavior, but it should be implemented as part of a single `CartesianLayerRangeProvider`, not by means of an `CartesianLayerRangeProvider`-switching mechanism. In particular, this means that `CartesianChartModelProducer`-powered charts, `CartesianLayerRangeProvider.fixed` should be used only for entirely static overrides.

When you need to perform calculations based on the `CartesianLayer`’s intrinsic _x_- and _y_-ranges, use the values passed to the `CartesianLayerRangeProvider` functions. Beyond that, use extras if needed. These are important here not only for the usual synchronization reasons, but also because they’re updated via [`CartesianChartModelProducer.Transaction`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/-transaction/)s, and a `CartesianChartModelProducer.Transaction` is required for a `CartesianChart`’s _x_- and _y_-ranges to be updated. A common use case for extras is switching between externally defined _x_- and _y_-ranges—both in synchronization with series updates and without series updates (for example, in response to changes in user-accessible range settings).

## `Axis.Position.Vertical`

A `CartesianChart` can have two separate _y_-ranges, one for the start _y_-axis and one for the end _y_-axis. (The presence of two _y_-axes isn’t strictly necessary, but it’s generally needed for unambiguity.) You can assign each `CartesianLayer` an [`Axis.Position.Vertical`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-axis/-position/-vertical/) subclass—either [`Start`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-axis/-position/-vertical/-start/) or [`End`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-axis/-position/-vertical/-end/). The final _y_-range for either `Axis.Position.Vertical` subclass is the narrowest range that includes the _y_-ranges of all `CartesianLayer`s linked to that subclass. Thus, you get two independently scaled groups of `CartesianLayer`s, and the two _y_-axes are disconnected.

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
