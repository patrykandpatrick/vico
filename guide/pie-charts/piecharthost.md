# PieChartHost

[`PieChartHost`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart-host/) is the entry point for pie charts. It accepts [`PieChart`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/) and [`PieChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model-producer/) instances, facilitates communication between them, and displays the chart. It also shows a placeholder when no data is available.

```kt
PieChartHost(rememberPieChart(/* ... */), modelProducer, /* ... */)
```

You can also use a manually created [`PieChartModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model/) instance instead of a model producer:

```kt
PieChartHost(rememberPieChart(/* ... */), model, /* ... */)
```
