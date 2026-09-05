# PieChartModelProducer

[`PieChartModelProducer`][pie-chart-model-producer] creates and updates [`PieChartModel`][pie-chart-model] instances for [`PieChartHost`][pie-chart-host].

Use [`pieModel`][pie-model] inside a transaction:

```kt
val modelProducer = remember { PieChartModelProducer() }
LaunchedEffect(Unit) {
    modelProducer.runTransaction {
        pieModel { series(60, 20, 20) }
    }
}
```

[`PieChartModel.Builder.series`][pie-chart-model-builder-series] accepts either a `vararg` list of values or an iterable collection. Values must be nonnegative.

For static data, you can bypass the model producer and provide a [`PieChartModel`][pie-chart-model] instance directly to `PieChartHost`.

[pie-chart-model-producer]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model-producer/
[pie-chart-model]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model/
[pie-chart-host]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart-host.html
[pie-model]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/pie-model.html
[pie-chart-model-builder-series]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model/-builder/series.html
