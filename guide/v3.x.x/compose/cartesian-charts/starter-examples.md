---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/starter-examples
---

# Starter examples

## Column chart

The following has been adapted from the [“Basic column chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/BasicColumnChart.kt) sample chart.

```kt
val modelProducer = remember { CartesianChartModelProducer() }
LaunchedEffect(Unit) {
    modelProducer.runTransaction {
        columnSeries { series(5, 6, 5, 2, 11, 8, 5, 2, 15, 11, 8, 13, 12, 10, 2, 7) }
    }
}
CartesianChartHost(
    rememberCartesianChart(
        rememberColumnCartesianLayer(),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis = HorizontalAxis.rememberBottom(),
    ),
    modelProducer,
)
```

<figure><img src="../../.gitbook/assets/image.png" alt="" width="375"><figcaption></figcaption></figure>

## Line chart

The following has been adapted from the [“Basic line chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/BasicLineChart.kt) sample chart.

```kt
val modelProducer = remember { CartesianChartModelProducer() }
LaunchedEffect(Unit) {
    modelProducer.runTransaction {
        lineSeries { series(13, 8, 7, 12, 0, 1, 15, 14, 0, 11, 6, 12, 0, 11, 12, 11) }
    }
}
CartesianChartHost(
    rememberCartesianChart(
        rememberLineCartesianLayer(),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis = HorizontalAxis.rememberBottom(),
    ),
    modelProducer,
)
```

<figure><img src="../../.gitbook/assets/image (1).png" alt="" width="375"><figcaption></figcaption></figure>

## More

For more examples, refer to [the sample app](../../learning-resources.md#sample-app).
