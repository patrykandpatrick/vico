---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/core/cartesian-charts/cartesianvalueformatter
---

# CartesianValueFormatter

## Overview

_x_- and _y_-values are numerical. You can use [`CartesianValueFormatter`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-value-formatter/) to format them for display. They can remain numbers, or they can be transformed to dates, category names, and so on.

There are two `CartesianValueFormatter` factory functions: [`CartesianValueFormatter.decimal`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-value-formatter/-companion/decimal) and [`CartesianValueFormatter.yPercent`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-value-formatter/-companion/y-percent). For more complex use cases, create custom implementations can be created. `CartesianValueFormatter`s are most commonly used with [`HorizontalAxis`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-horizontal-axis/) and [`VerticalAxis`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.axis/-vertical-axis/); see the `valueFormatter` parameters and properties. However, these aren’t the only APIs that accept `CartesianValueFormatter` instances.

When the values remain numerical, formatting is straightforward. `DecimalFormat`, which is used by `CartesianValueFormatter.decimal`, is typically involved. Thus, on this page, we focus on formatting with nonnumerical results. The aim in such cases is to find a predictable mapping that doesn’t involve overly large values (as these can lead to precision loss). The optimal approach depends on the use case. Some common situations are discussed below.

## Categories

A chart’s domain can be a list of categories. An easy way to implement this pattern is to use _x_-values that serve as indices. As previously discussed, the series-creating functions have overloads that add such _x_-values automatically.

```kt
val data = mapOf("A" to 8f, "B" to 4f, "C" to 6f)
```

```kt
val labelListKey = ExtraStore.Key<List<String>>()
```

```kt
cartesianChartModelProducer.runTransaction {
    columnSeries { series(data.values) }
    updateExtras { it[labelListKey] = data.keys.toList() }
}
```

```kt
CartesianValueFormatter { context, x, _ ->
    context.model.extraStore[labelListKey][x.toInt()]
}
```

## Dates

Another common use case is mapping dates to _y_-values.

The dates will be spaced out proportionally. If you need nonproportional spacing, use the approach from the previous subsection. This is also worth considering if there are no gaps in your data, in which case there’s no distinction between proportional and nonproportional spacing—the category approach will be simpler.

```kt
val data =
    mapOf(
        LocalDate.parse("2022-07-01") to 2f,
        LocalDate.parse("2022-07-02") to 6f,
        LocalDate.parse("2022-07-04") to 4f,
    )
```

```kt
val xToDateMapKey = ExtraStore.Key<Map<Float, LocalDate>>()
```

```kt
val xToDates = data.keys.associateBy { it.toEpochDay().toFloat() }
cartesianChartModelProducer.runTransaction {
    lineSeries { series(xToDates.keys, data.values) }
    updateExtras { it[xToDateMapKey] = xToDates }
}
```

```kt
val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM")
```

```kt
CartesianValueFormatter { context, x, _ ->
    (context.model.extraStore[xToDateMapKey][x] ?: LocalDate.ofEpochDay(x.toLong()))
        .format(dateTimeFormatter)
}
```

## Sample charts

* [“AI test scores”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/AITestScores.kt)
* [“Daily digital-media use (USA)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/DailyDigitalMediaUse.kt)
* [“Temperature anomalies (June)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/TemperatureAnomalies.kt)
* [“Electric-car sales (Norway)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/ElectricCarSales.kt)
* [“Rock–metal ratios”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/RockMetalRatios.kt)
* [“Gold prices (12/30/2024)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/GoldPrices.kt)
