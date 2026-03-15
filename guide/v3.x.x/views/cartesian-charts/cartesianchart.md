---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/core/cartesian-charts/cartesianchart
---

# CartesianChart

## Anatomy

[`CartesianChart`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart/) draws a chart based on a Cartesian coordinate plane. The chart includes one or more data layers, drawn by [`CartesianLayer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.layer/-cartesian-layer/) instances. Each layer has its own data.

## Ranges

A chart’s _x_- and _y_-ranges depend on those reported by its layers. The _x_-range is the narrowest one that includes all layers’ _x_-ranges. By default, there’s an analogously determined shared _y_-range, but two separate _y_-ranges can also be introduced.

_x_- and _y_-range customization is discussed in [a later subsection](cartesianlayer.md#cartesianlayerrangeprovider).

## _x_-step

The _x_-step is a reference _x_-increment. Together with `CartesianLayer` settings, it determines the intrinsic mapping between data and screen units (before zoom). Other components also use it for calibration; for instance, the built-in axis-item placers use it as the default label and line spacing. An _x_-value is considered _major_ if it’s a natural number of _x_-steps away from the minimum.

By default, the _x_-step is the greatest common divisor of the differences between consecutive _x_-values the flattened _x_-series. For example, if these _x_-values are $$\{0, 1, 2, 3\}$$, the _x_-step is 1; if the _x_ values are $$\{0, 2, 4, 6\}$$, the _x_-step is 2.

To customize the _x_-step, use `CartesianChart`’s `getXStep` constructor parameter, which accepts a `(CartesianChartModel) -> Double` lambda:

```kotlin
CartesianChart(getXStep = { 1.0 }, /* ... */)
```

Consider a daily line chart whose data points may arrive at arbitrary times within a day. Setting the _x_-step to one day means that one day’s worth of _x_-distance intrinsically maps to a specific amount of screen space, and the built-in axis-item placers use a one-day interval.

For a daily column chart that may have gaps in the data, locking the _x_-step to one day causes the chart to leave room for the missing days. Without this, the default would shrink the step based on the data actually present, and columns would be packed together as though no days were missing.

For line charts, both increasing and decreasing the _x_-step relative to the default can be useful. For column and candlestick charts, only increasing it is practical; the opposite could cause overlaps.

Note that the _x_-step is different from zoom. Zoom scales the entire chart—including column widths, for example—whereas the _x_-step changes the logical interpretation of the data’s spacing. The effects described above aren’t achievable with zoom alone.

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
