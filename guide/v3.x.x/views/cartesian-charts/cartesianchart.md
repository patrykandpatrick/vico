---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/core/cartesian-charts/cartesianchart
---

# CartesianChart

## Anatomy

[`CartesianChart`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart/) draws charts based on Cartesian coordinate planes. Any such chart includes one or more [`CartesianLayer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.layer/-cartesian-layer/)s. The `CartesianLayer`s are stacked, and each one has its own data.

## Ranges

A `CartesianChart`’s _x_- and _y_-ranges depend on those reported by its `CartesianLayer`s. The _x_-range is the narrowest one that includes all `CartesianLayer`s’ _x_-ranges. By default, there’s an analogously determined shared _y_-range, but two separate _y_-ranges can also be introduced.

_x_- and _y_-range customization is discussed in [a later subsection](cartesianlayer.md#cartesianlayerrangeprovider).

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
