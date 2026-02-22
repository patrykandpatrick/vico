---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/axis
---

# Axis

## Overview

[`Axis`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-axis/) draws an axis along one of a `CartesianChart`’s edges. There are two built-in implementations: [`HorizontalAxis`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/) and [`VerticalAxis`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/). Use [`VerticalAxis.rememberStart`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/-companion/remember-start), [`HorizontalAxis.rememberTop`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/-companion/remember-top), [`VerticalAxis.rememberEnd`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/-companion/remember-end), and [`HorizontalAxis.rememberBottom`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/-companion/remember-bottom) to instantiate these:

```kt
rememberCartesianChart(
    startAxis = VerticalAxis.rememberStart(/* ... */),
    topAxis = HorizontalAxis.rememberTop(/* ... */),
    endAxis = VerticalAxis.rememberEnd(/* ... */),
    bottomAxis = HorizontalAxis.rememberBottom(/* ... */),
    // ...
)
```

Numerous customization options are available; you can change the appearance of the labels, modify the axis lines, add titles, and more.

## `ItemPlacer`

[`HorizontalAxis.ItemPlacer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/-item-placer/) and [`VerticalAxis.ItemPlacer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/-item-placer/) let you customize for what _x_- and _y_-values labels and lines are displayed. Four factory functions are available:

* [`HorizontalAxis.ItemPlacer.aligned`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/-item-placer/-companion/aligned) (default)
* [`HorizontalAxis.ItemPlacer.segmented`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/-item-placer/-companion/segmented)
* [`VerticalAxis.ItemPlacer.step`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/-item-placer/-companion/step) (default)
* [`VerticalAxis.ItemPlacer.count`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/-item-placer/-companion/count)

Custom implementations can be created.

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
