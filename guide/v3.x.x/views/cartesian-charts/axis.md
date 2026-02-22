---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/core/cartesian-charts/axis
---

# Axis

## Overview

[`Axis`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-axis/) draws an axis along one of a [`CartesianChart`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart/)’s edges. There are two built-in implementations: [`HorizontalAxis`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-horizontal-axis/) and [`VerticalAxis`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-vertical-axis/). Numerous customization options are available—you can change the appearance of the labels, modify the axis lines, add titles, and more.

Use the XML attributes to create [`HorizontalAxis`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-horizontal-axis/) and [`VerticalAxis`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-vertical-axis/) instances:

```xml
<style name="ChartStyle">
    <item name="showStartAxis">true</item>
    <item name="showTopAxis">true</item>
    <item name="showEndAxis">true</item>
    <item name="showBottomAxis">true</item>
</style>
```

```xml
<com.patrykandpatrick.vico.views.cartesian.CartesianChartView
    app:chartStyle="@style/ChartStyle"
    <!-- ... --> />
```

Alternatively, use [`VerticalAxis.start`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-vertical-axis/-companion/start), [`HorizontalAxis.top`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-horizontal-axis/-companion/top), [`VerticalAxis.end`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-vertical-axis/-companion/end), and [`HorizontalAxis.bottom`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-horizontal-axis/-companion/bottom):

```kt
cartesianChartView.chart =
    CartesianChart(
        startAxis = VerticalAxis.start(/* ... */),
        topAxis = HorizontalAxis.top(/* ... */),
        endAxis = VerticalAxis.end(/* ... */),
        bottomAxis = HorizontalAxis.bottom(/* ... */),
        // ...
    )
```

## `ItemPlacer`

[`HorizontalAxis.ItemPlacer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-horizontal-axis/-item-placer/) and [`VerticalAxis.ItemPlacer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-vertical-axis/-item-placer/) let you customize for what _x_- and _y_-values labels and lines are displayed. Four factory functions are available:

* [`HorizontalAxis.ItemPlacer.aligned`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-horizontal-axis/-item-placer/-companion/aligned) (default)
* [`HorizontalAxis.ItemPlacer.segmented`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-horizontal-axis/-item-placer/-companion/segmented)
* [`VerticalAxis.ItemPlacer.step`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-vertical-axis/-item-placer/-companion/step) (default)
* [`VerticalAxis.ItemPlacer.count`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-vertical-axis/-item-placer/-companion/count)

Custom implementations can be created.

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
