# PieChartHost

[`PieChartHost`][pie-chart-host] is the entry point for pie charts. It accepts [`PieChart`][pie-chart] and [`PieChartModelProducer`][pie-chart-model-producer] instances, facilitates communication between them, and displays the chart. It also shows a placeholder when no data is available.

```kt
PieChartHost(rememberPieChart(/* ... */), modelProducer, /* ... */)
```

You can also use a manually created [`PieChartModel`][pie-chart-model] instance instead of a model producer:

```kt
PieChartHost(rememberPieChart(/* ... */), model, /* ... */)
```

[pie-chart-host]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart-host.html
[pie-chart]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/
[pie-chart-model-producer]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model-producer/
[pie-chart-model]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model/
