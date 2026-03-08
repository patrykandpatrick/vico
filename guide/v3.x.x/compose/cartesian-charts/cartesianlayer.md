---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/cartesianlayer
---

# CartesianLayer

## Implementations

There are three built-in [`CartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-cartesian-layer/) implementations:

* [`CandlestickCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-candlestick-cartesian-layer/)
* [`ColumnCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-column-cartesian-layer/)
* [`LineCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/)

These are discussed individually in the following sections.

## `CartesianLayerRangeProvider`

What _x_- and _y_-ranges a layer reports depends on its [`CartesianLayerRangeProvider`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-layer-range-provider/) instance. A layer passes its intrinsic _x_- and _y_-ranges—which depend on [`CartesianLayerModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-layer-model/)—to this instance, which returns the final ranges to report. The available singletons and factory functions are listed below. For more specific behavior, implement the interface.

* [`CartesianLayerRangeProvider.auto`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-layer-range-provider/-companion/auto) (default)
* [`CartesianLayerRangeProvider.fixed`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-layer-range-provider/-companion/fixed)
* [`CartesianLayerRangeProvider.Intrinsic`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-layer-range-provider/-companion/-intrinsic)

The default implementations of the `CartesianLayerRangeProvider` functions leave the _x_-range unchanged but do these two things:

* They ensure that the _y_-range includes zero.
* They apply a _y_-range of \[0, 1] if the minimum and maximum intrinsic _y_-values are both zero.

This also applies to the implementation returned by `CartesianLayerRangeProvider.auto`. Custom `CartesianLayerRangeProvider` implementations can override this behavior. With `CartesianLayerRangeProvider.fixed`, `minY` and `maxY` take precedence.

When using [`CartesianChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model-producer/), set the `CartesianLayerRangeProvider` instance for each layer only once. There are no restrictions on dynamic behavior, but it should be implemented as part of a single `CartesianLayerRangeProvider` instance, not by means of a mechanism that switches between `CartesianLayerRangeProvider` implementations. In particular, for charts powered by `CartesianChartModelProducer`, `CartesianLayerRangeProvider.fixed` should be used only for entirely static overrides.

When you need to perform calculations based on a layer’s intrinsic _x_- and _y_-ranges, use the values passed to the `CartesianLayerRangeProvider` functions. Beyond that, use extras if needed. These are important here not only for the usual synchronization reasons, but also because they’re updated via [`CartesianChartModelProducer.Transaction`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model-producer/-transaction/), and a transaction is required for a chart’s _x_- and _y_-ranges to be updated. A common use case for extras is switching between externally defined _x_- and _y_-ranges—both in synchronization with series updates and without series updates (for example, in response to changes in user-accessible range settings).

## `Axis.Position.Vertical`

A [`CartesianChart`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart/) can have two separate _y_-ranges, one for the start _y_-axis and one for the end _y_-axis. (The presence of two _y_-axes isn’t strictly necessary, but it’s generally needed for unambiguity.) You can assign each layer to an [`Axis.Position.Vertical`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-axis/-position/-vertical/) subclass—either [`Start`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-axis/-position/-vertical/-start/) or [`End`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-axis/-position/-vertical/-end/). The final _y_-range for either subclass is the narrowest range that includes the _y_-ranges of all linked layers. Thus, you get two independently scaled groups of layers, and the two _y_-axes are disconnected.

## Sample charts

* [“Basic column chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/BasicColumnChart.kt)
* [“Basic line chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/BasicLineChart.kt)
* [“Basic combo chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/BasicComboChart.kt)
* [“AI test scores”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/AITestScores.kt)
* [“Daily digital-media use (USA)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/DailyDigitalMediaUse.kt)
* [“Temperature anomalies (June)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/TemperatureAnomalies.kt)
* [“Electric-car sales (Norway)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/ElectricCarSales.kt)
* [“Rock–metal ratios”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/RockMetalRatios.kt)
* [“Gold prices (12/30/2024)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/GoldPrices.kt)
