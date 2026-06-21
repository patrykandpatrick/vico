# PieChart

[`PieChart`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/) draws a pie chart.

## Creation

Instantiate `PieChart` via [`rememberPieChart`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/remember-pie-chart).

```kt
val chart = rememberPieChart(/* ... */)
```

`PieChart` accepts a [`PieChart.SliceProvider`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/-slice-provider/) instance, which supplies [`PieChart.Slice`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/-slice/) instances for successive entries. Use [`PieChart.SliceProvider.series`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/-slice-provider/-companion/series) for the standard repeating-series setup.

Each pie-slice style can define fill, stroke, an offset from the center, and an optional [`PieChart.SliceLabel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/-slice-label/). There are two built-in label types:

* [`PieChart.SliceLabel.Inside`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/-slice-label/-inside/)
* [`PieChart.SliceLabel.Outside`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/-slice-label/-outside/)

Use [`PieValueFormatter`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-value-formatter/) to format slice labels.

You can also customize slice spacing, the start angle, and the inner and outer sizes. A nonzero inner size produces a donut chart.

## Sample charts

* [“Basic pie chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/BasicPieChart.kt)
