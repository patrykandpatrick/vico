---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/linecartesianlayer
---

# LineCartesianLayer

## Overview

Use [`LineCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/) to create line charts. Instantiate `LineCartesianLayer` via [`rememberLineCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/remember-line-cartesian-layer).

Each line is associated with a [`LineCartesianLayer.Line`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-line/). Create these via [`LineCartesianLayer.rememberLine`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/remember-line). The `LineCartesianLayer.Line`s are provided by a [`LineCartesianLayer.LineProvider`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-line-provider/). A base implementation of this interface can be instantiated via [`LineCartesianLayer.LineProvider.series`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-line-provider/-companion/series). You can customize lines’ fills, backgrounds, shapes, and other properties. You can also add data labels and points.

Line strokes are customized via [`LineCartesianLayer.LineStroke`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-line-stroke/), which has two implementations:

* [`LineCartesianLayer.LineStroke.Continuous`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-line-stroke/-continuous/)
* [`LineCartesianLayer.LineStroke.Dashed`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-line-stroke/-dashed/)

Line fills are customized via [`LineCartesianLayer.LineFill`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-line-fill/), which has two factory functions:

* [`LineCartesianLayer.LineFill.single`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-line-fill/-companion/single)
* [`LineCartesianLayer.LineFill.double`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-line-fill/-companion/double)

Area fills, which are optional, are customized via [`LineCartesianLayer.AreaFill`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-area-fill/). This has similar factory functions to `LineCartesianLayer.LineFill`:

* [`LineCartesianLayer.AreaFill.single`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-area-fill/-companion/single)
* [`LineCartesianLayer.AreaFill.double`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-area-fill/-companion/double)

These cover most use cases. You can use both `Color`s and `Brush`es, and you can apply split styling—enabling you to create a line that’s green for positive values and red for negative values, for instance. You can, however, also create your own `LineCartesianLayer.LineFill` and `LineCartesianLayer.AreaFill` implementations.

To add points, use [`LineCartesianLayer.PointProvider`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-point-provider/). [`LineCartesianLayer.PointProvider.single`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-point-provider/-companion/single) instantiates a base implementation that adds a point for each entry and uses a shared point style. Once again, custom implementations can be created. A common use case for this is styling points individually based on their _y_-values.

## `Transaction.lineSeries`

`LineCartesianLayer`s use [`LineCartesianLayerModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-line-cartesian-layer-model/)s. When using a [`CartesianChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model-producer/), add `LineCartesianLayerModel`s via [`lineSeries`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/line-series):

```kt
cartesianChartModelProducer.runTransaction {
    lineSeries {
        series(1, 8, 3, 7)
        series(y = listOf(6, 1, 9, 3))
        series(x = listOf(1, 2, 3, 4), y = listOf(2, 5, 3, 4))
    }
    // ...
}
```

Each [`series`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-line-cartesian-layer-model/-builder-scope/series) invocation adds a series to the `LineCartesianLayerModel`. Above, three series are added. `series` has three overloads (each of which accepts all `Number` subtypes):

* a `vararg` overload that takes _y_-values and uses their indices as the _x_-values
* an overload that takes a `Collection` of _y_-values and uses their indices as the _x_-values
* an overload that takes a `Collection` of _x_-values and a `Collection` of _y_-values of the same size

## Manual `LineCartesianLayerModel` creation

When creating a [`CartesianChartModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model/) directly, you can add a `LineCartesianLayerModel` by using [`build`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-line-cartesian-layer-model/-companion/build). This function gives you access to the same DSL that `lineSeries` does.

```kt
CartesianChartModel(
    LineCartesianLayerModel.build {
        series(1, 8, 3, 7)
        series(y = listOf(6, 1, 9, 3))
        series(x = listOf(1, 2, 3, 4), y = listOf(2, 5, 3, 4))
    },
    // ...
)
```

## Sample charts

* [“Basic line chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/BasicLineChart.kt)
* [“Basic combo chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/BasicComboChart.kt)
* [“AI test scores”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/AITestScores.kt)
* [“Electric-car sales (Norway)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/ElectricCarSales.kt)
