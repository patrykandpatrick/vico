# PieChartModelProducer

[`PieChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie.data/-pie-chart-model-producer/) creates and updates [`PieChartModel`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie.data/-pie-chart-model/) instances for [`PieChartView`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie/-pie-chart-view/).

Use [`pieSeries`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie.data/pie-series) inside a transaction:

```kt
val modelProducer = PieChartModelProducer()
lifecycleScope.launch {
    modelProducer.runTransaction {
        pieSeries { series(60, 20, 20) }
    }
}
```

[`PieChartModel.Builder.series`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie.data/-pie-chart-model/-builder/series) accepts either a `vararg` list of values or an iterable collection. Values must be nonnegative.

For static data, you can bypass the model producer and provide a [`PieChartModel`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie.data/-pie-chart-model/) instance directly to `PieChartView`.
