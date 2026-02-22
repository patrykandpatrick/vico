---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/columncartesianlayer
---

# ColumnCartesianLayer

## Overview

Use [`ColumnCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-column-cartesian-layer/) to create column charts. Instantiate `ColumnCartesianLayer` via [`rememberColumnCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/remember-column-cartesian-layer).

Each column corresponds to a [`LineComponent`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/-line-component/). The `LineComponent`s are provided by a [`ColumnCartesianLayer.ColumnProvider`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-column-cartesian-layer/-column-provider/). [`ColumnCartesianLayer.ColumnProvider.series`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-column-cartesian-layer/-column-provider/-companion/series) creates a `ColumnCartesianLayer.ColumnProvider` that uses one `LineComponent` per series. You can create your own implementation for custom behavior, including styling columns individually based on their _y_-values.

In addition to customizing the columns, at the `rememberColumnCartesianLayer` level, you can change their spacing. Data labels are supported. When multiple series are added, a `ColumnCartesianLayer`’s columns can be grouped horizontally or stacked.

## `Transaction.columnSeries`

`ColumnCartesianLayer`s use [`ColumnCartesianLayerModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-column-cartesian-layer-model/)s. When using a [`CartesianChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model-producer/), add `ColumnCartesianLayerModel`s via [`columnSeries`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/column-series):

```kt
cartesianChartModelProducer.runTransaction {
    columnSeries {
        series(1, 8, 3, 7)
        series(y = listOf(6, 1, 9, 3))
        series(x = listOf(1, 2, 3, 4), y = listOf(2, 5, 3, 4))
    }
    // ...
}
```

Each [`series`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-column-cartesian-layer-model/-builder-scope/series) invocation adds a series to the `ColumnCartesianLayerModel`. Above, three series are added. `series` has three overloads (each of which accepts all `Number` subtypes):

* a `vararg` overload that takes _y_-values and uses their indices as the _x_-values
* an overload that takes a `Collection` of _y_-values and uses their indices as the _x_-values
* an overload that takes a `Collection` of _x_-values and a `Collection` of _y_-values of the same size

## Manual `ColumnCartesianLayerModel` creation

When creating a [`CartesianChartModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model/) directly, you can add a `ColumnCartesianLayerModel` by using [`build`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-column-cartesian-layer-model/-companion/build). This function gives you access to the same DSL that `columnSeries` does.

```kt
CartesianChartModel(
    ColumnCartesianLayerModel.build {
        series(1, 8, 3, 7)
        series(y = listOf(6, 1, 9, 3))
        series(x = listOf(1, 2, 3, 4), y = listOf(2, 5, 3, 4))
    },
    // ...
)
```

## Sample charts

* [“Basic column chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/BasicColumnChart.kt)
* [“Basic combo chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/BasicComboChart.kt)
* [“Daily digital-media use (USA)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/DailyDigitalMediaUse.kt)
* [“Temperature anomalies (June)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/TemperatureAnomalies.kt)
* [“Rock–metal ratios”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/RockMetalRatios.kt)
