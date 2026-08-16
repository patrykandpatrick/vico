# PieChartHost

[`PieChartHost`][pie-chart-host] is the entry point for pie charts. It accepts [`PieChart`][pie-chart] and [`PieChartModelProducer`][pie-chart-model-producer] instances, facilitates communication between them, and displays the chart. It also shows a placeholder when no data is available.

```kt
PieChartHost(rememberPieChart(/* ... */), modelProducer, /* ... */)
```

You can also use a manually created [`PieChartModel`][pie-chart-model] instance instead of a model producer:

```kt
PieChartHost(rememberPieChart(/* ... */), model, /* ... */)
```

## Sizing

The [`chartAreaHeight`][chart-area-height] parameter sets the default pie diameter. Its default value is 232 dp. Legends, outside labels, and other components add to this height instead of being fitted inside it.

This default applies only when the host’s height isn’t otherwise constrained. [`Modifier.height`][modifier-height] and similar modifiers set the total host height, within which the pie and its components are fitted.

[pie-chart-host]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart-host.html
[pie-chart]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/
[pie-chart-model-producer]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model-producer/
[pie-chart-model]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model/
[chart-area-height]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart-host.html
[modifier-height]: https://developer.android.com/develop/ui/compose/modifiers-list#Size
